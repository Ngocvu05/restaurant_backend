package com.management.restaurant.service.implement;

import com.management.restaurant.model.RefreshToken;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.RefreshTokenRepository;
import com.management.restaurant.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Refresh Token Management Service
 *
 * Features:
 * - Token lifecycle management
 * - Automatic cleanup of expired tokens
 * - Security monitoring
 * - Device tracking
 * - Usage analytics
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    /**
     * Find token by token string
     *
     * @param token
     */
    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Find all tokens for a user
     *
     * @param user
     */
    @Override
    public List<RefreshToken> findByUser(User user) {
        return refreshTokenRepository.findByUser(user);
    }

    /**
     * Find active (valid) tokens for a user
     *
     * @param user
     */
    @Override
    public List<RefreshToken> findActiveTokensByUser(User user) {
        return refreshTokenRepository.findByUser(user).stream()
                .filter(RefreshToken::isValid)
                .toList();
    }

    /**
     * Get all tokens in a token family (for rotation tracking)
     *
     * @param tokenFamily
     */
    @Override
    public List<RefreshToken> findByTokenFamily(String tokenFamily) {
        return refreshTokenRepository.findByTokenFamily(tokenFamily);
    }

    /**
     * Revoke a specific token
     *
     * @param token
     * @param revokedBy
     * @param reason
     */
    @Override
    @Transactional
    public void revokeToken(String token, String revokedBy, String reason) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(tokenEntity -> {
                    tokenEntity.revoke(revokedBy, reason);
                    refreshTokenRepository.save(tokenEntity);
                    log.info("✅ Token revoked: {} (reason: {})", token.substring(0, 20) + "...", reason);
                });
    }

    /**
     * Revoke all tokens for a user
     *
     * @param user
     * @param reason
     */
    @Override
    @Transactional
    public int revokeAllUserTokens(User user, String reason) {
        List<RefreshToken> tokens = findActiveTokensByUser(user);
        tokens.forEach(token -> token.revoke("system", reason));
        refreshTokenRepository.saveAll(tokens);

        log.info("✅ Revoked {} tokens for user: {}", tokens.size(), user.getUsername());
        return tokens.size();
    }

    /**
     * Revoke all tokens in a token family
     * (Useful when detecting suspicious rotation)
     *
     * @param tokenFamily
     * @param reason
     */
    @Override
    @Transactional
    public int revokeTokenFamily(String tokenFamily, String reason) {
        List<RefreshToken> tokens = findByTokenFamily(tokenFamily);
        tokens.forEach(token -> token.revoke("system", reason));
        refreshTokenRepository.saveAll(tokens);

        log.warn("⚠️ Revoked entire token family: {} ({} tokens)", tokenFamily, tokens.size());
        return tokens.size();
    }

    /**
     * Revoke tokens from suspicious IP
     *
     * @param ipAddress
     * @param reason
     */
    @Override
    @Transactional
    public int revokeTokensByIp(String ipAddress, String reason) {
        List<RefreshToken> tokens = refreshTokenRepository.findByIpAddress(ipAddress);
        tokens.forEach(token -> token.revoke("system", reason));
        refreshTokenRepository.saveAll(tokens);

        log.warn("⚠️ Revoked {} tokens from IP: {}", tokens.size(), ipAddress);
        return tokens.size();
    }

    /**
     * Revoke all tokens except current device
     *
     * @param user
     * @param currentDeviceId
     */
    @Override
    @Transactional
    public int revokeOtherDevices(User user, String currentDeviceId) {
        List<RefreshToken> tokens = findActiveTokensByUser(user).stream()
                .filter(token -> !currentDeviceId.equals(token.getDeviceId()))
                .toList();

        tokens.forEach(token -> token.revoke(user.getUsername(), "Revoked from other device"));
        refreshTokenRepository.saveAll(tokens);

        log.info("✅ Revoked {} tokens from other devices for user: {}", tokens.size(), user.getUsername());
        return tokens.size();
    }

    /**
     * Soft delete expired tokens (runs daily at 2 AM)
     */
    @Override
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("🧹 Starting cleanup of expired refresh tokens...");

        List<RefreshToken> expiredTokens = refreshTokenRepository.findExpiredTokens(LocalDateTime.now());

        expiredTokens.forEach(token -> {
            token.softDelete("system");
            token.setUpdatedBy("cleanup-job");
        });

        refreshTokenRepository.saveAll(expiredTokens);

        log.info("✅ Cleaned up {} expired tokens", expiredTokens.size());
    }

    /**
     * Hard delete old soft-deleted tokens (runs weekly on Sunday at 3 AM)
     */
    @Override
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void purgeOldDeletedTokens() {
        log.info("🗑️ Starting purge of old deleted tokens...");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        int deleted = refreshTokenRepository.deleteOldDeletedTokens(cutoffDate);

        log.info("✅ Purged {} tokens older than 90 days", deleted);
    }

    /**
     * Check if token usage is suspicious
     *
     * @param token
     */
    @Override
    public boolean isSuspiciousUsage(RefreshToken token) {
        if (token.getUseCount() > 100) {
            log.warn("⚠️ Suspicious token usage detected: {} uses", token.getUseCount());
            return true;
        }
        // Token used from multiple IPs quickly
        if (token.getLastUsedAt() != null) {
            long hoursSinceLastUse = java.time.Duration.between(
                    token.getLastUsedAt(),
                    LocalDateTime.now()
            ).toHours();

            if (hoursSinceLastUse < 1 && token.getUseCount() > 10) {
                log.warn("⚠️ Suspicious rapid token reuse detected");
                return true;
            }
        }
        return false;
    }
}