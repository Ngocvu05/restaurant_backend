package com.management.api_gateway.jwt;

import com.management.api_gateway.service.RateLimitService;
import com.management.api_gateway.util.FilterCommonUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Set;

/**
 * JWT Authentication Filter for API Gateway
 * Uses RS256 with Public Key verification
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitService rateLimitService;
    private final JwtService jwtService;

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/users/api/v1/auth/login",
            "/users/api/v1/auth/register",
            "/users/api/v1/auth/oauth2/login",
            "/users/api/v1/auth/oauth2/refresh-token",
            "/users/api/v1/auth/refresh-token",
            "/users/api/v1/home",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/actuator/health",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/chat/ws",
            "/chat/ws/**",
            "/chat/api/v1/guest",
            "/chat/api/v1/guest/**"
    );

    public JwtAuthenticationFilter(RedisTemplate<String, String> redisTemplate,
                                   RateLimitService rateLimitService,
                                   JwtService jwtService) {
        this.redisTemplate = redisTemplate;
        this.rateLimitService = rateLimitService;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        String clientIp = FilterCommonUtils.getClientIpAddress(request);

        log.info("Processing request: {} {} from IP: {}", method, path, clientIp);

        // 1. Rate Limiting Check
        if (!rateLimitService.isAllowed(clientIp)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            return FilterCommonUtils.handleUnauthorized(exchange, "Rate limit exceeded");
        }

        // 2. Always allow OPTIONS requests (CORS preflight)
        if (request.getMethod() == HttpMethod.OPTIONS) {
            log.info("OPTIONS request detected, allowing without JWT validation");
            return chain.filter(exchange);
        }

        // 3. Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.info("Public endpoint detected, skipping JWT validation: {}", path);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .headers(headers -> {
                        headers.remove("Authorization");
                        headers.remove("authorization");
                    })
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }

        // 4. Extract and validate JWT token
        String token = extractToken(request);
        if (token == null) {
            log.warn("Missing authentication token for path: {}", path);
            return FilterCommonUtils.handleUnauthorized(exchange, "Missing authentication token");
        }

        return validateTokenAsync(token)
                .flatMap(claims -> {
                    log.info("JWT validation successful for user: {}", claims.getSubject());

                    // 5. Check if token is blacklisted
                    return checkTokenBlacklist(token)
                            .flatMap(isBlacklisted -> {
                                if (isBlacklisted) {
                                    log.warn("Blacklisted token used by user: {}", claims.getSubject());
                                    return FilterCommonUtils.handleUnauthorized(exchange, "Token has been revoked");
                                }

                                // 6. Add user info to request headers
                                ServerHttpRequest mutatedRequest = request.mutate()
                                        .header("X-User-Id", getClaimAsString(claims, "id"))
                                        .header("X-User-Subject", claims.getSubject())
                                        .header("X-User-Email", getClaimAsString(claims, "email"))
                                        .header("X-User-Role", getClaimAsString(claims, "role"))
                                        .header("X-User-Roles", getClaimAsString(claims, "roles"))
                                        .build();

                                return chain.filter(exchange.mutate().request(mutatedRequest).build());
                            });
                })
                .onErrorResume(throwable -> {
                    log.warn("Token validation failed for path {}: {}", path, throwable.getMessage());
                    String errorMessage = getErrorMessage(throwable);
                    return FilterCommonUtils.handleUnauthorized(exchange, errorMessage);
                });
    }

    /**
     * Validate token asynchronously using JwtService (RS256)
     */
    private Mono<Claims> validateTokenAsync(String token) {
        return Mono.fromCallable(() -> {
            // Validate token using public key
            if (!jwtService.validateToken(token)) {
                throw new RuntimeException("Invalid JWT token");
            }

            // Check token age (max 24 hours)
            if (jwtService.isTokenTooOld(token, 1440)) {
                throw new RuntimeException("Token too old");
            }

            return jwtService.getClaims(token);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Check if token is blacklisted in Redis
     */
    private Mono<Boolean> checkTokenBlacklist(String token) {
        return Mono.fromCallable(() -> {
            String tokenHash = DigestUtils.sha256Hex(token);
            Boolean hasKey = redisTemplate.hasKey("blacklist:token:" + tokenHash);
            return hasKey != null && hasKey;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Extract Bearer token from Authorization header
     */
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Check if path matches public endpoints
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(endpoint ->
                path.equals(endpoint) ||
                        (endpoint.endsWith("/**") && path.startsWith(endpoint.substring(0, endpoint.length() - 3)))
        );
    }

    /**
     * Safely extract claim as string
     */
    private String getClaimAsString(Claims claims, String claimName) {
        Object claim = claims.get(claimName);
        return claim != null ? String.valueOf(claim) : "";
    }

    /**
     * Get user-friendly error message
     */
    private String getErrorMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message != null) {
            if (message.contains("expired")) return "Token has expired";
            if (message.contains("signature")) return "Invalid token signature";
            if (message.contains("malformed")) return "Invalid token format";
            if (message.contains("old")) return "Token is too old";
        }
        return "Invalid authentication token";
    }

    @Override
    public int getOrder() {
        return -100;
    }
}