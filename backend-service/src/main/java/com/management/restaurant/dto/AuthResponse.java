package com.management.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuthResponse {
    // User info
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String avatarUrl;

    // Tokens
    private String accessToken;
    private String refreshToken;

    // Security status
    private Boolean emailVerified;
    private Boolean twoFactorEnabled;
    private Boolean requiresPasswordChange;

    // OAuth2 info
    private String authMethod;  // "password", "google", "facebook", etc.
    private List<String> linkedProviders;
    private Boolean hasPassword;

    // Additional info
    private LocalDateTime lastLoginAt;
}