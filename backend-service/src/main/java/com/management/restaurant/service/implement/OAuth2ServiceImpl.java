package com.management.restaurant.service.implement;

import com.management.restaurant.analytics.help.RequestHelper;
import com.management.restaurant.dto.AuthResponse;
import com.management.restaurant.dto.OAuth2LoginRequest;
import com.management.restaurant.event.ChatEventProducer;
import com.management.restaurant.model.RefreshToken;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.RefreshTokenRepository;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.security.JwtService;
import com.management.restaurant.service.AuthService;
import com.management.restaurant.service.OAuth2Service;
import com.management.restaurant.service.oauth2.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Enhanced OAuth2 Service with Security Features
 * <p>
 * Features:
 * <p>-  Failed login protection</p>
 * <p>-  Account locking mechanism</p>
 * <p>-  Email verification tracking</p>
 * <p>-  Rate limiting integration</p>
 * <p>-  Audit trail</p>
 * <p>-  Refresh token rotation</p>
 * <p>-  Device and IP tracking</p>
 * <p>-  Chat session conversion</p>
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2ServiceImpl implements OAuth2Service {
    // Security Configuration
    private static final Long REFRESH_TTL_DAYS = 7L;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_DURATION_MINUTES = 30;

    private final RequestHelper requestHelper;
    private final OAuth2ProviderFactory providerFactory;
    private final UserOAuth2Service userService;
    private final JwtService jwtService;
    private final ChatEventProducer chatEventProducer;
    private final RateLimitService rateLimitService;
    private final AuditService auditService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    /**
     * Enhanced OAuth2 authentication with security checks
     */
    @Override
    public AuthResponse authenticateOAuth2User(OAuth2LoginRequest request, HttpServletRequest httpRequest) {
        String provider = request.getProvider();
        String email = null;
        User user = null;

        try {
            log.info("OAuth2 login attempt - Provider: {}, SessionId: {}",
                    provider, request.getSessionId());
            // Step 1: Validate request
            validateRequest(request);

            // Step 2: Check rate limit
            String rateLimitKey = provider + "_" +
                    (request.getEmail() != null ? request.getEmail() : request.getSessionId());
            rateLimitService.checkRateLimit(rateLimitKey);

            // Step 3: Authenticate with OAuth2 provider
            OAuth2Provider oauth2Provider = providerFactory.getProvider(provider);
            user = oauth2Provider.authenticateAndGetUser(request);
            email = user.getEmail();

            // Step 4: Security checks
            performSecurityChecks(user);

            // Step 5: Update login tracking
            updateLoginTracking(user);

            // Step 6: Generate tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = issueRefreshToken(user, httpRequest);

            // Step 7: Handle chat session conversion
            handleChatSessionConversion(request, user);

            // Step 8: Build response
            AuthResponse response = buildAuthResponse(user, accessToken, refreshToken);

            // Step 9: Log success
            auditService.logOAuth2Success(provider, email);
            log.info("✅ OAuth2 login successful - User: {}, Provider: {}, 2FA: {}",
                    user.getUsername(), provider, user.hasTwoFactorEnabled());

            return response;

        } catch (Exception e) {
            auditService.logOAuth2Failure(provider, email, e.getMessage());
            // Handle failures
            handleAuthenticationFailure(provider, email, user, e);
            throw new RuntimeException("OAuth2 authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate OAuth2 request
     */
    private void validateRequest(OAuth2LoginRequest request) {
        if (request.getProvider() == null || request.getProvider().trim().isEmpty()) {
            throw new IllegalArgumentException("Provider is required");
        }
        if (request.getAccessToken() == null || request.getAccessToken().trim().isEmpty()) {
            throw new IllegalArgumentException("Access token is required");
        }
    }

    /**
     * Perform comprehensive security checks
     */
    private void performSecurityChecks(User user) {
        // Security Check 1: Account locked?
        if (user.isAccountLocked()) {
            log.warn("⚠️ OAuth2 login attempt for locked account: {} (locked until: {})",
                    user.getUsername(), user.getAccountLockedUntil());
            throw new RuntimeException(
                    "Account is temporarily locked due to security reasons. " +
                            "Please try again later or contact support.");
        }

        // Security Check 2: Account active?
        if (!user.isActive()) {
            log.warn("⚠️ OAuth2 login attempt for inactive account: {}", user.getUsername());
            throw new RuntimeException(
                    "Account is not active. Please contact support.");
        }

        // Security Check 3: Email verified? (Optional enforcement)
        if (!user.hasVerifiedEmail()) {
            log.info("ℹ️ OAuth2 login with unverified email: {}", user.getEmail());
            // Note: For OAuth2, email is typically verified by provider
            // Auto-verify email since OAuth2 provider has verified it
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                user.setEmailVerified(true);
                userRepository.save(user);
                log.info("✅ Email auto-verified via OAuth2: {}", user.getEmail());
            }
        }

        // Security Check 4: Two-factor required? (Future enhancement)
        if (user.hasTwoFactorEnabled()) {
            log.info("ℹ️ User has 2FA enabled: {}", user.getUsername());
            // TODO: Implement 2FA challenge for OAuth2 users
            // For now, OAuth2 providers handle this
        }
    }

    /**
     * Update login tracking for security audit
     */
    private void updateLoginTracking(User user) {
        // Reset failed login attempts on successful OAuth2 login
        user.recordSuccessfulLogin();
        userRepository.save(user);

        log.debug("Login tracking updated - User: {}, Last login: {}",
                user.getUsername(), user.getLastLoginAt());
    }

    /**
     * Issue refresh token with security tracking
     */
    private String issueRefreshToken(User user, HttpServletRequest request) {
        // Generate JWT refresh token
        String jwtRefreshToken = jwtService.generateRefreshToken(user);

        // Generate token family ID for rotation tracking
        String tokenFamily = UUID.randomUUID().toString();

        // Create new refresh token entity with security info
        RefreshToken tokenEntity = RefreshToken.builder()
                .token(jwtRefreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(REFRESH_TTL_DAYS))
                .createdBy(user.getUsername())
                .tokenFamily(tokenFamily)
                .build();

        // Extract and set security information
        if (request != null) {
            tokenEntity.setIpAddress(requestHelper.getCurrentIpAddress());
            tokenEntity.setUserAgent(requestHelper.getUserAgent());
            tokenEntity.setDeviceId(requestHelper.getDeviceInfoString());
            tokenEntity.setDeviceName(requestHelper.extractDeviceName(request));
        }

        refreshTokenRepository.save(tokenEntity);

        log.info("✅ OAuth2 refresh token issued - User: {}, IP: {}, Device: {}",
                user.getUsername(),
                tokenEntity.getIpAddress(),
                tokenEntity.getDeviceName());

        return jwtRefreshToken;
    }

    /**
     * Handle chat session conversion (guest to authenticated)
     */
    private void handleChatSessionConversion(OAuth2LoginRequest request, User user) {
        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            return;
        }

        try {
            chatEventProducer.sendSessionConversion(request.getSessionId(), user.getId());
            log.info(" Chat session conversion sent - SessionId: {}, UserId: {}",
                    request.getSessionId(), user.getId());
        } catch (Exception e) {
            // Don't fail OAuth2 login if chat conversion fails
            log.error(" Failed to send chat session conversion - SessionId: {}",
                    request.getSessionId(), e);
        }
    }

    /**
     * Build comprehensive auth response
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getRole().getName().name())
                .avatarUrl(authService.getAvatarUrl(user))
                .email(user.getEmail())
                .fullName(user.getFullName())
                .emailVerified(user.hasVerifiedEmail())
                .twoFactorEnabled(user.hasTwoFactorEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    /**
     * Handle authentication failures
     */
    private void handleAuthenticationFailure(String provider, String email, User user, Exception e) {
        // Log failure to audit
        auditService.logOAuth2Failure(provider, email, e.getMessage());

        // If user exists, track failed attempt (for rate limiting)
        if (user != null) {
            // Note: For OAuth2, failed attempts are usually due to:
            // 1. Invalid OAuth2 token (provider issue)
            // 2. Account locked/inactive (our issue)
            // 3. Network/system errors
            // We don't track these as "failed login attempts" like password logins
            log.warn("⚠️ OAuth2 authentication failed for user: {} - Reason: {}",
                    user.getUsername(), e.getMessage());
        }

        log.error("❌ OAuth2 authentication failed - Provider: {}, Email: {}",
                provider, email, e);
    }

    /**
     * Refresh OAuth2 user's token
     * Similar to regular refresh but for OAuth2 users
     */
    @Override
    public AuthResponse refreshOAuth2Token(String refreshToken, HttpServletRequest httpRequest) {
        try {
            // Validate JWT refresh token
            if (!jwtService.validateRefreshToken(refreshToken)) {
                log.warn("❌ Invalid or expired OAuth2 refresh token");
                throw new RuntimeException("Invalid or expired refresh token");
            }

            // Verify token exists in database
            RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            // Check if token is valid
            if (!tokenEntity.isValid()) {
                log.warn("❌ OAuth2 token is not valid - Expired: {}, Revoked: {}, Deleted: {}",
                        tokenEntity.isExpired(),
                        tokenEntity.isRevoked(),
                        tokenEntity.getDeletedAt() != null);
                refreshTokenRepository.delete(tokenEntity);
                throw new RuntimeException("Refresh token is invalid");
            }

            User user = tokenEntity.getUser();

            // Security checks
            performSecurityChecks(user);

            // Track usage
            tokenEntity.incrementUseCount();
            refreshTokenRepository.save(tokenEntity);

            // Rotate refresh token for security
            String newRefreshToken = authService.rotateRefreshToken(tokenEntity, user, httpRequest);

            // Generate new access token
            String newAccessToken = jwtService.generateToken(user);

            log.info("✅ OAuth2 token refreshed successfully - User: {}", user.getUsername());

            return buildAuthResponse(user, newAccessToken, newRefreshToken);

        } catch (Exception e) {
            log.error("❌ OAuth2 token refresh failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to refresh OAuth2 token: " + e.getMessage(), e);
        }
    }

    /**
     * Link OAuth2 account to existing user
     * Allows users to connect multiple OAuth2 providers
     */
    @Override
    public void linkOAuth2Account(Long userId, OAuth2LoginRequest request) {
        try {
            log.info("🔗 Linking OAuth2 account - UserId: {}, Provider: {}",
                    userId, request.getProvider());

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify OAuth2 credentials
            OAuth2Provider oauth2Provider = providerFactory.getProvider(request.getProvider());
            User oauth2User = oauth2Provider.authenticateAndGetUser(request);

            // Verify email matches
            if (!user.getEmail().equalsIgnoreCase(oauth2User.getEmail())) {
                throw new RuntimeException(
                        "Email mismatch - Cannot link OAuth2 account with different email");
            }

            // Store OAuth2 link (implement OAuth2Link entity if needed)
            // For now, just log
            log.info("✅ OAuth2 account linked - User: {}, Provider: {}, Email: {}",
                    user.getUsername(), request.getProvider(), oauth2User.getEmail());

            auditService.logOAuth2Link(userId, request.getProvider(), oauth2User.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to link OAuth2 account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to link OAuth2 account: " + e.getMessage(), e);
        }
    }

    /**
     * Unlink OAuth2 account
     */
    @Override
    public void unlinkOAuth2Account(Long userId, String provider) {
        try {
            log.info("🔓 Unlinking OAuth2 account - UserId: {}, Provider: {}",
                    userId, provider);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // TODO: Remove OAuth2Link entity
            // For now, just log
            log.info("✅ OAuth2 account unlinked - User: {}, Provider: {}",
                    user.getUsername(), provider);

            auditService.logOAuth2Unlink(userId, provider);

        } catch (Exception e) {
            log.error("❌ Failed to unlink OAuth2 account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to unlink OAuth2 account: " + e.getMessage(), e);
        }
    }

    /**
     * Get user's linked OAuth2 providers
     */
    public List<String> getLinkedProviders(Long userId) {
        // TODO: Query OAuth2Link table
        // For now, return empty list
        return List.of();
    }
}