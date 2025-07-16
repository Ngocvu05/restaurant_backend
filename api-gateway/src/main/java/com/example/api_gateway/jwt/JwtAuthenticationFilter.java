package com.example.api_gateway.jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final List<String> PUBLIC_URLS = List.of(
            "/users/api/v1/auth/login",
            "/users/api/v1/auth/register",
            "/users/api/v1/home",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/actuator/health",
            "/chat/ws/**",
            "/chat/api/v1/messages/**",
            "/chat/api/v1/rooms/**"
    );

    //  Use shared key generation logic
    private Key getSignKey() {
        return JwtKeyUtil.getSigningKey(jwtSecret);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.info(">>> JWT Filter - Path: {}", path);

        // Check if the path is a public endpoint
        boolean isPublicEndpoint = PUBLIC_URLS.stream()
                .anyMatch(publicUrl -> path.equals(publicUrl) || path.startsWith(publicUrl + "/"));

        if (isPublicEndpoint) {
            log.info(">>> Public endpoint detected, skipping JWT validation: {}", path);
            // Remove the Authorization header before forwarding to downstream service
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .headers(headers -> headers.remove("Authorization"))
                    .headers(headers -> headers.remove("authorization") )
                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            return chain.filter(modifiedExchange);
        }

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        log.info(">>> JWT Filter - Authorization header present: {}", authHeader != null);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn(">>> Missing or invalid Authorization header");
            return handleUnauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        log.info(">>> JWT Filter - Token length: {}", token.length());

        try {
            // Use the same key generation logic as JwtService
            Key signingKey = getSignKey();

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Check token expiration
            if (claims.getExpiration().before(new Date())) {
                log.warn(">>> Token expired for user: {}", claims.getSubject());
                return handleUnauthorized(exchange, "Token expired");
            }

            log.info(">>> JWT validation successful for user: {}", claims.getSubject());
            log.info(">>> JWT claims - ID: {}, Role: {}", claims.get("id"), claims.get("role"));

            // Add user info to request headers for downstream services
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-User-Id", String.valueOf(claims.get("id")))
                            .header("X-User-Role", String.valueOf(claims.get("role")))
                            .header("X-User-Subject", claims.getSubject())
                    )
                    .build();

            return chain.filter(mutatedExchange);

        } catch (io.jsonwebtoken.security.SignatureException ex) {
            log.error(">>> JWT signature validation failed: {}", ex.getMessage());
            log.error(">>> This usually means the signing key is different between services");
            return handleUnauthorized(exchange, "Invalid token signature");
        } catch (MalformedJwtException ex) {
            log.warn(">>> Malformed JWT token: {}", ex.getMessage());
            return handleUnauthorized(exchange, "Malformed token");
        } catch (ExpiredJwtException ex) {
            log.warn(">>> JWT token expired: {}", ex.getMessage());
            return handleUnauthorized(exchange, "Token expired");
        } catch (UnsupportedJwtException ex) {
            log.warn(">>> Unsupported JWT token: {}", ex.getMessage());
            return handleUnauthorized(exchange, "Unsupported token");
        } catch (IllegalArgumentException ex) {
            log.warn(">>> JWT claims string is empty: {}", ex.getMessage());
            return handleUnauthorized(exchange, "Invalid token");
        } catch (Exception ex) {
            log.error(">>> Unexpected error during JWT validation: {}", ex.getMessage(), ex);
            return handleInternalServerError(exchange);
        }
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> handleInternalServerError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().add("Content-Type", "application/json");

        String body = "{\"error\":\"Internal Server Error\",\"message\":\"Token validation failed\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }
}