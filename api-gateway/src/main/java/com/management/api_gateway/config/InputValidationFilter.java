package com.management.api_gateway.config;

import com.management.api_gateway.util.FilterCommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@Slf4j
public class InputValidationFilter implements GlobalFilter, Ordered {
    private static final int MAX_REQUEST_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            ".*\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION|SCRIPT)\\b.*",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern XSS_PATTERN = Pattern.compile(
            ".*<\\s*script\\b[^<]*(?:(?!</script>)<[^<]*)*</script>.*|.*javascript:.*|.*on\\w+\\s*=.*",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. Check request size
        String contentLength = request.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
        if (contentLength != null) {
            try {
                long size = Long.parseLong(contentLength);
                if (size > MAX_REQUEST_SIZE) {
                    return handleBadRequest(exchange, "Request size too large");
                }
            } catch (NumberFormatException e) {
                return handleBadRequest(exchange, "Invalid content length");
            }
        }

        // 2. Validate query parameters
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            for (String value : entry.getValue()) {
                if (containsMaliciousContent(value)) {
                    log.warn("Malicious content detected in query param {}: {}", entry.getKey(), value);
                    return handleBadRequest(exchange, "Invalid request parameters");
                }
            }
        }

        // 3. Validate headers
        HttpHeaders headers = request.getHeaders();
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            if (isUserSuppliedHeader(header.getKey())) {
                for (String value : header.getValue()) {
                    if (containsMaliciousContent(value)) {
                        log.warn("Malicious content detected in header {}: {}", header.getKey(), value);
                        return FilterCommonUtils.handleBadRequest(exchange, "Invalid request headers");
                    }
                }
            }
        }

        return chain.filter(exchange);
    }

    private boolean containsMaliciousContent(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        return SQL_INJECTION_PATTERN.matcher(input).matches() ||
                XSS_PATTERN.matcher(input).matches();
    }

    private boolean isUserSuppliedHeader(String headerName) {
        String lowerCaseName = headerName.toLowerCase();
        return !lowerCaseName.startsWith("x-forwarded-") &&
                !lowerCaseName.equals("host") &&
                !lowerCaseName.equals("user-agent") &&
                !lowerCaseName.equals("accept") &&
                !lowerCaseName.equals("content-type") &&
                !lowerCaseName.equals("authorization");
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
