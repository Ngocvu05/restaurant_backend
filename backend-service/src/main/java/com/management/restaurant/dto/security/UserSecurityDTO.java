package com.management.restaurant.dto.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user security information
 * Used by admins to view security status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurityDTO {
    private Long userId;
    private String username;

    // Security tracking
    private Integer failedLoginAttempts;
    private LocalDateTime accountLockedUntil;
    private Boolean isAccountLocked;
    private LocalDateTime lastLoginAt;

    // Verification status
    private Boolean emailVerified;
    private Boolean twoFactorEnabled;

    // Password management
    private LocalDateTime passwordChangedAt;
    private Boolean passwordExpired;

    // Additional info
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

