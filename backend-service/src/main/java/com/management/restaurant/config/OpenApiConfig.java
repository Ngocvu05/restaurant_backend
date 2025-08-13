package com.management.restaurant.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI restaurantApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Restaurant Management API")
                        .description("API for managing restaurant bookings, orders, and payments")
                        .version("v1.0"));
    }
}