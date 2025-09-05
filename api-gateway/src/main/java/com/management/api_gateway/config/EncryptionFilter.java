package com.management.api_gateway.config;

import com.management.api_gateway.util.AESUtil;
import com.management.api_gateway.util.FilterCommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@Slf4j
public class EncryptionFilter implements GlobalFilter, Ordered {
    @Value("${app.encryption.enabled:true}")
    private boolean encryptionEnabled;

    @Value("${app.encryption.secret}")
    private String encryptionSecret;

    private final AESUtil aesUtil;

    public EncryptionFilter(AESUtil aesUtil) {
        this.aesUtil = aesUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!encryptionEnabled) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);

        // Only encrypt JSON payloads
        if (contentType != null && contentType.contains("application/json")) {
            return handleEncryptedRequest(exchange, chain);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> handleEncryptedRequest(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Check if request is encrypted
        String encryptedHeader = request.getHeaders().getFirst("X-Encrypted");
        if ("true".equals(encryptedHeader)) {
            // Decrypt request body
            return DataBufferUtils.join(request.getBody())
                    .flatMap(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);

                        try {
                            String encryptedBody = new String(bytes, StandardCharsets.UTF_8);
                            String decryptedBody = aesUtil.decrypt(encryptedBody);

                            // Create new request with decrypted body
                            DataBuffer buffer = exchange.getResponse().bufferFactory()
                                    .wrap(decryptedBody.getBytes(StandardCharsets.UTF_8));

                            ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
                                @Override
                                public Flux<DataBuffer> getBody() {
                                    return Flux.just(buffer);
                                }

                                @Override
                                public HttpHeaders getHeaders() {
                                    HttpHeaders headers = new HttpHeaders();
                                    headers.putAll(super.getHeaders());
                                    headers.remove("X-Encrypted");
                                    headers.setContentLength(decryptedBody.length());
                                    return headers;
                                }
                            };

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());

                        } catch (Exception e) {
                            log.error("Failed to decrypt request", e);
                            return FilterCommonUtils.handleBadRequest(exchange, "Invalid encrypted payload");
                        }
                    })
                    .switchIfEmpty(chain.filter(exchange));
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
