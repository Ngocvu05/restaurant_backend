package com.management.chat_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                /*registry.addMapping("/**")
                        .allowedOriginPatterns("http://localhost:3000") //Frontend Origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*") // hoặc: .allowedHeaders("Authorization", "X-User-Id", ...)
                        .exposedHeaders("X-User-Id", "X-User-Role")
                        .allowCredentials(true); // nếu bạn dùng cookie*/
            }
        };
    }
}
