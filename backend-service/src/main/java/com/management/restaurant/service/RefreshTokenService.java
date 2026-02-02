package com.management.restaurant.service;

import com.management.restaurant.model.RefreshToken;
import com.management.restaurant.model.User;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenService {
    /**
     * Find token by token string
     */
    Optional<RefreshToken> findByToken(String token);
    /**
     * Find all tokens for a user
     */
    List<RefreshToken> findByUser(User user);
    /**
     * Find active (valid) tokens for a user
     */
    List<RefreshToken> findActiveTokensByUser(User user);
    /**
     * Get all tokens in a token family (for rotation tracking)
     */
    List<RefreshToken> findByTokenFamily(String tokenFamily);
    /**
     * Revoke a specific token
     */
    void revokeToken(String token, String revokedBy, String reason);
    /**
     * Revoke all tokens for a user
     */
    int revokeAllUserTokens(User user, String reason);

    /**
     * Revoke all tokens in a token family
     * (Useful when detecting suspicious rotation)
     */
    int revokeTokenFamily(String tokenFamily, String reason);

    /**
     * Revoke tokens from suspicious IP
     */
    int revokeTokensByIp(String ipAddress, String reason);

    /**
     * Revoke all tokens except current device
     */
    int revokeOtherDevices(User user, String currentDeviceId);

    /**
     * Soft delete expired tokens (runs daily at 2 AM)
     */
    void cleanupExpiredTokens();
    /**
     * Hard delete old soft-deleted tokens (runs weekly on Sunday at 3 AM)
     */
    void purgeOldDeletedTokens();

    /**
     * Check if token usage is suspicious
     */
    boolean isSuspiciousUsage(RefreshToken token);
}