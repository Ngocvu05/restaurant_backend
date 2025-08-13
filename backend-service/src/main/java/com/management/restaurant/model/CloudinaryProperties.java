package com.management.restaurant.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloudinary")
@Data
public class CloudinaryProperties {
    private String cloudName;
    private String apiKey;
    private String apiSecret;
}