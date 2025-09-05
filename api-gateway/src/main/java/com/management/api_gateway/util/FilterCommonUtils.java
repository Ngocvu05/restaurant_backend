package com.management.api_gateway.util;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Common utility class for Gateway Filters
 * Contains shared functionality used across multiple filter classes
 */
public class FilterCommonUtils {
    /**
     * Handle Bad Request (400) error response with JSON format
     *
     * @param exchange ServerWebExchange
     * @param message Error message to include in response
     * @return Mono<Void>
     */
    public static Mono<Void> handleBadRequest(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String body = """
            {
                "error": "Bad Request",
                "message": "%s",
                "timestamp": "%s"
            }
            """.formatted(message, Instant.now().toString());

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Handle Unauthorized (401) error response with JSON format and CORS headers
     *
     * @param exchange ServerWebExchange
     * @param message Error message to include in response
     * @return Mono<Void>
     */
    public static Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        // Add CORS headers to error response
        response.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:3000");
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Handle Internal Server Error (500) response with JSON format and CORS headers
     *
     * @param exchange ServerWebExchange
     * @return Mono<Void>
     */
    public static Mono<Void> handleInternalServerError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

        // Add CORS headers to error response
        response.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:3000");
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Content-Type", "application/json");

        String body = "{\"error\":\"Internal Server Error\",\"message\":\"Token validation failed\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Handle Internal Server Error (500) response with custom message
     *
     * @param exchange ServerWebExchange
     * @param message Custom error message
     * @return Mono<Void>
     */
    public static Mono<Void> handleInternalServerError(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

        // Add CORS headers to error response
        response.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:3000");
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\":\"Internal Server Error\",\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Extract client IP address from request headers
     * Checks X-Forwarded-For, X-Real-IP headers, and remote address
     *
     * @param request ServerHttpRequest
     * @return Client IP address as String
     */
    public static String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /**
     * Create a standardized error response body with timestamp
     *
     * @param error Error type/category
     * @param message Error message
     * @return JSON formatted error response string
     */
    public static String createErrorResponseBody(String error, String message) {
        return """
            {
                "error": "%s",
                "message": "%s",
                "timestamp": "%s"
            }
            """.formatted(error, message, Instant.now().toString());
    }

    /**
     * Add standard CORS headers to response
     *
     * @param response ServerHttpResponse to add headers to
     */
    public static void addCorsHeaders(ServerHttpResponse response) {
        response.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:3000");
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Content-Type", "application/json");
    }
}