package com.management.api_gateway.jwt;

import com.management.api_gateway.service.RateLimitService;
import com.management.api_gateway.util.FilterCommonUtils;
import io.jsonwebtoken.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
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

import java.security.Key;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitService rateLimitService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    // Using Set for better performance on lookup operations
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/users/api/v1/auth/login",
            "/users/api/v1/auth/register",
            "/users/api/v1/auth/oauth2/login",
            "/users/api/v1/auth/oauth2/refresh-token",
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
                                   RateLimitService rateLimitService) {
        this.redisTemplate = redisTemplate;
        this.rateLimitService = rateLimitService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        String clientIp = FilterCommonUtils.getClientIpAddress(request);

        log.debug("Processing request: {} {} from IP: {}", method, path, clientIp);

        // 1. Rate Limiting Check
        if (!rateLimitService.isAllowed(clientIp)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            return FilterCommonUtils.handleUnauthorized(exchange, "Rate limit exceeded");
        }

        // 2. Always allow OPTIONS requests (CORS preflight)
        if (request.getMethod() == HttpMethod.OPTIONS) {
            log.debug("OPTIONS request detected, allowing without JWT validation");
            return chain.filter(exchange);
        }

        // 3. Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("Public endpoint detected, skipping JWT validation: {}", path);

            // Remove Authorization headers before forwarding to downstream service
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

        return validateToken(token)
                .flatMap(claims -> {
                    log.debug("JWT validation successful for user: {}", claims.getSubject());

                    // 5. Check if token is blacklisted (logout)
                    return checkTokenBlacklist(token)
                            .flatMap(isBlacklisted -> {
                                if (isBlacklisted) {
                                    log.warn("Blacklisted token used by user: {}", claims.getSubject());
                                    return FilterCommonUtils.handleUnauthorized(exchange, "Token has been revoked");
                                }

                                // 6. Add comprehensive user info to request headers
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

    private Mono<Claims> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Key signingKey = JwtKeyUtil.getSigningKey(jwtSecret);
                return Jwts.parserBuilder()
                        .setSigningKey(signingKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

            } catch (ExpiredJwtException ex) {
                throw new RuntimeException("JWT expired", ex);
            } catch (UnsupportedJwtException ex) {
                throw new RuntimeException("Unsupported JWT", ex);
            } catch (MalformedJwtException ex) {
                throw new RuntimeException("Malformed JWT", ex);
            } catch (SecurityException ex) {
                throw new RuntimeException("Invalid JWT signature", ex);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("JWT is empty", ex);
            } catch (JwtException ex) {
                throw new RuntimeException("Invalid JWT", ex);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Boolean> checkTokenBlacklist(String token) {
        return Mono.fromCallable(() -> {
            String tokenHash = DigestUtils.sha256Hex(token);
            return redisTemplate.hasKey("blacklist:token:" + tokenHash);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(endpoint ->
                path.equals(endpoint) ||
                        (endpoint.endsWith("/**") && path.startsWith(endpoint.substring(0, endpoint.length() - 3)))
        );
    }

    /**
     * Safely extract claim value as string, handling null values
     */
    private String getClaimAsString(Claims claims, String claimName) {
        Object claim = claims.get(claimName);
        return claim != null ? String.valueOf(claim) : "";
    }

    /**
     * Get user-friendly error message based on exception type
     */
    private String getErrorMessage(Throwable throwable) {
        if (throwable instanceof SecurityException) {
            return throwable.getMessage();
        }

        String message = throwable.getMessage();
        if (message != null) {
            if (message.contains("expired")) {
                return "Token has expired";
            }
            if (message.contains("signature")) {
                return "Invalid token signature";
            }
            if (message.contains("malformed")) {
                return "Invalid token format";
            }
        }
        return "Invalid authentication token";
    }

    @Override
    public int getOrder() {
        return -100; // Execute before other filters
    }
}