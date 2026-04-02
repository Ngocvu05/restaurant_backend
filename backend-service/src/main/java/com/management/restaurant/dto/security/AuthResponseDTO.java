package com.management.restaurant.dto.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; /**
 * Enhanced AuthResponse with security info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
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

    // Additional info
    private LocalDateTime lastLoginAt;
}
