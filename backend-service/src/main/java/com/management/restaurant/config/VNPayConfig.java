package com.management.restaurant.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class VNPayConfig {
    // Getters
    @Value("${VNPAY_TMN_CODE}")
    private String tmnCode;

    @Value("${VNPAY_HASH_SECRET}")
    private String hashSecret;

    @Value("${VNPAY_API_URL}")
    private String apiUrl;

    @Value("${VNPAY_RETURN_URL}")
    private String returnUrl;

    @Override
    public String toString() {
        return "VNPayConfig{" +
                "tmnCode='" + tmnCode + '\'' +
                ", hashSecret='***HIDDEN***'" +
                ", apiUrl='" + apiUrl + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                '}';
    }

    @Bean
    public VNPayConfig vnPayConfig() {
        return new VNPayConfig();
    }
}