package com.management.restaurant.service.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Audit Service for OAuth2 Operations
 * Logs all OAuth2-related activities for security tracking
 */
@Slf4j
@Service
public class AuditService {
    /**
     * Log successful OAuth2 login
     */
    public void logOAuth2Success(String provider, String email) {
        log.info("✅ AUDIT: OAuth2 login SUCCESS - Provider: {}, Email: {}, Timestamp: {}",
                provider, maskEmail(email), LocalDateTime.now());
    }

    /**
     * Log failed OAuth2 login
     */
    public void logOAuth2Failure(String provider, String email, String error) {
        log.warn("❌ AUDIT: OAuth2 login FAILURE - Provider: {}, Email: {}, Error: {}, Timestamp: {}",
                provider, maskEmail(email), error, LocalDateTime.now());
    }

    /**
     * Log OAuth2 account linking
     */
    public void logOAuth2Link(Long userId, String provider, String email) {
        log.info("🔗 AUDIT: OAuth2 account LINKED - UserId: {}, Provider: {}, Email: {}, Timestamp: {}",
                userId, provider, maskEmail(email), LocalDateTime.now());
    }

    /**
     * Log OAuth2 account unlinking
     */
    public void logOAuth2Unlink(Long userId, String provider) {
        log.info("🔓 AUDIT: OAuth2 account UNLINKED - UserId: {}, Provider: {}, Timestamp: {}",
                userId, provider, LocalDateTime.now());
    }

    /**
     * Log OAuth2 token refresh
     */
    public void logOAuth2TokenRefresh(Long userId, String provider) {
        log.info("🔄 AUDIT: OAuth2 token REFRESHED - UserId: {}, Provider: {}, Timestamp: {}",
                userId, provider, LocalDateTime.now());
    }

    /**
     * Log OAuth2 primary provider change
     */
    public void logOAuth2PrimaryChange(Long userId, String oldProvider, String newProvider) {
        log.info("⭐ AUDIT: OAuth2 primary provider CHANGED - UserId: {}, From: {}, To: {}, Timestamp: {}",
                userId, oldProvider, newProvider, LocalDateTime.now());
    }

    /**
     * Log OAuth2 account creation via social login
     */
    public void logOAuth2AccountCreation(String provider, String email) {
        log.info("👤 AUDIT: New account CREATED via OAuth2 - Provider: {}, Email: {}, Timestamp: {}",
                provider, maskEmail(email), LocalDateTime.now());
    }

    /**
     * Log OAuth2 token expiry/cleanup
     */
    public void logOAuth2TokenCleanup(int tokensCount) {
        log.info("🧹 AUDIT: OAuth2 expired tokens CLEANED UP - Count: {}, Timestamp: {}",
                tokensCount, LocalDateTime.now());
    }

    /**
     * Log security event (suspicious activity)
     */
    public void logOAuth2SecurityEvent(String provider, String email, String event, String details) {
        log.warn("⚠️ AUDIT: OAuth2 SECURITY EVENT - Provider: {}, Email: {}, Event: {}, Details: {}, Timestamp: {}",
                provider, maskEmail(email), event, details, LocalDateTime.now());
    }

    /**
     * Log OAuth2 rate limit hit
     */
    public void logOAuth2RateLimitHit(String provider, String identifier) {
        log.warn("⏱️ AUDIT: OAuth2 RATE LIMIT HIT - Provider: {}, Identifier: {}, Timestamp: {}",
                provider, maskEmail(identifier), LocalDateTime.now());
    }

    /**
     * Mask email for privacy in logs
     * Example: john.doe@example.com → j******e@example.com
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "INVALID_EMAIL";
        }

        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            // For very short usernames (1-2 chars)
            return username.charAt(0) + "*@" + domain;
        } else {
            // For longer usernames: first char + stars + last char
            int maskedLength = username.length() - 2;
            return username.charAt(0) +
                    "*".repeat(maskedLength) +
                    username.charAt(username.length() - 1) +
                    "@" + domain;
        }
    }

    /**
     * Mask provider user ID for logs
     * Example: 1234567890 → 123*****890
     */
    private String maskProviderId(String providerId) {
        if (providerId == null || providerId.length() <= 6) {
            return "***";
        }

        int visibleChars = 3;
        int maskedLength = providerId.length() - (visibleChars * 2);

        return providerId.substring(0, visibleChars) +
                "*".repeat(maskedLength) +
                providerId.substring(providerId.length() - visibleChars);
    }

    /**
     * Log detailed OAuth2 operation for debugging
     * Only logged in DEBUG level
     */
    public void logOAuth2Debug(String operation, String details) {
        log.debug("🔍 AUDIT DEBUG: OAuth2 {} - Details: {}", operation, details);
    }

    /**
     * Generate audit report summary
     */
    public void logOAuth2AuditSummary(String period, int totalLogins, int failedLogins,
                                      int newLinks, int unlinks) {
        log.info("📊 AUDIT SUMMARY: OAuth2 {} Report - Total Logins: {}, Failed: {}, " +
                        "New Links: {}, Unlinks: {}, Timestamp: {}",
                period, totalLogins, failedLogins, newLinks, unlinks, LocalDateTime.now());
    }
}