package com.example.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

// API Gateway - CorsGlobalConfiguration.java
@Configuration
public class CorsGlobalConfiguration {

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ Cho phép credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // ✅ Cấu hình origins cụ thể
        config.setAllowedOriginPatterns(List.of("http://localhost:3000", "http://127.0.0.1:3000"));

        // ✅ Cho phép tất cả methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));

        // ✅ Cấu hình headers cụ thể
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // ✅ Expose headers cần thiết
        config.setExposedHeaders(List.of("Authorization", "Content-Type", "X-Total-Count"));

        // ✅ Thêm max age để tránh preflight requests liên tục
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
