package com.management.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OAuth2LoginRequest {
    private String provider; // "google" or "facebook"
    private String accessToken;
    private String sessionId;
    private String email;
    private String name;
    private String picture;
    private String providerId;
    private String userAgent;
    private String clientIp;
}