package com.management.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuthResponse {
    private Long userId;
    private String accessToken;
    private String username;
    private String role;
    private String avatarUrl;
    private String email;
    private String refreshToken;
    private String fullName;
    private boolean emailVerified;
    private boolean twoFactorEnabled;
}