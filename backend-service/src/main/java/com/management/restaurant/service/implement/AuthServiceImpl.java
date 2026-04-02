package com.management.restaurant.service.implement;

import com.management.restaurant.analytics.help.RequestHelper;
import com.management.restaurant.contains.RoleName;
import com.management.restaurant.dto.AuthResponse;
import com.management.restaurant.dto.LoginRequest;
import com.management.restaurant.dto.RegisterRequest;
import com.management.restaurant.event.ChatEventProducer;
import com.management.restaurant.model.Image;
import com.management.restaurant.model.RefreshToken;
import com.management.restaurant.model.User;
import com.management.restaurant.model.UserRole;
import com.management.restaurant.repository.ImageRepository;
import com.management.restaurant.repository.RefreshTokenRepository;
import com.management.restaurant.repository.UserRepository;
import com.management.restaurant.repository.UserRoleRepository;
import com.management.restaurant.security.JwtService;
import com.management.restaurant.service.AuthService;
import com.management.restaurant.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Enhanced Authentication Service with security improvements
 * <p>
 * Security features:   </br>
 * - Short-lived access tokens  </br>
 * - Secure refresh token rotation  </br>
 * - Token blacklisting on logout   </br>
 * - All tokens revocation on password change   </br>
 * - Login attempt tracking (implement rate limiting separately)
 * <p>
 *  New Features:   </br>
 *  - IP address and User Agent tracking    </br>
 *  - Device identification and tracking    </br>
 *  - Token family for refresh token rotation   </br>
 *  - Usage tracking (use_count, last_used_at)  </br>
 *  - Security audit trail  </br>
 *  - Automatic token cleanup
 */

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    // Security Configuration
    private static final Long REFRESH_TTL_DAYS = 7L;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_DURATION_MINUTES = 30;
    private static final int PASSWORD_RESET_TOKEN_EXPIRY_HOURS = 24;
    private static final int EMAIL_VERIFICATION_EXPIRY_HOURS = 48;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RequestHelper requestHelper;

    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private ChatEventProducer chatEventProducer;

    @Autowired(required = false)
    private EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Enhanced registration with email verification
     */
    @Override
    public AuthResponse register(RegisterRequest request, MultipartFile avatarFile) {
        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException(" Username is already in use");
        }

        // Validate email uniqueness
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        // Create user with security defaults
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone_number(request.getPhone_number())
                .address(request.getAddress())
                .role(userRoleRepository.findByName(RoleName.CUSTOMER)
                        .orElseThrow(() -> new RuntimeException("Role not found")))
                .failedLoginAttempts(0)
                .emailVerified(false)
                .twoFactorEnabled(false)
                .passwordChangedAt(LocalDateTime.now())
                .build();

        // Generate email verification token
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            String verificationToken = UUID.randomUUID().toString();
            user.setEmailVerificationToken(verificationToken, EMAIL_VERIFICATION_EXPIRY_HOURS);

            // Send verification email (async)
            if (emailService != null) {
                emailService.sendVerificationEmail(user, verificationToken);
            }
        }

        userRepository.save(user);

        // Handle avatar upload
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String url = fileStorageService.saveAvatar(avatarFile);

            Image avatar = new Image();
            avatar.setUrl(url);
            avatar.setAvatar(true);
            avatar.setUser(user);
            imageRepository.save(avatar);
        }

        // Generate tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = issueRefreshToken(user, null);

        log.info("✅ New user registered: {} (Email verification: {})",
                user.getUsername(), user.getEmail() != null ? "sent" : "skipped");

        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getRole().getName().name())
                .avatarUrl(getAvatarUrl(user))
                .email(user.getEmail())
                .fullName(user.getFullName())
                .emailVerified(user.hasVerifiedEmail())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return login(request, null);
    }

    /**
     * Enhanced login with security tracking
     */
    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Security Check 1: Account locked?
        if (user.isAccountLocked()) {
            log.warn("⚠️ Login attempt for locked account: {} (locked until: {})",
                    user.getUsername(), user.getAccountLockedUntil());
            throw new RuntimeException("Account is temporarily locked due to multiple failed login attempts. " +
                    "Please try again later or reset your password.");
        }

        // Security Check 2: Account active?
        if (!user.isActive()) {
            log.warn("⚠️ Login attempt for inactive account: {}", user.getUsername());
            throw new RuntimeException("Account is not active. Please contact support.");
        }

        // Security Check 3: Password match?
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Record failed attempt
            user.recordFailedLogin(MAX_LOGIN_ATTEMPTS, ACCOUNT_LOCK_DURATION_MINUTES);
            userRepository.save(user);

            int remainingAttempts = MAX_LOGIN_ATTEMPTS - user.getFailedLoginAttempts();

            log.warn("❌ Failed login attempt for user: {} (Remaining attempts: {})",
                    request.getUsername(), remainingAttempts);

            if (remainingAttempts <= 0) {
                throw new RuntimeException("Account locked due to too many failed login attempts. " +
                        "Please try again in " + ACCOUNT_LOCK_DURATION_MINUTES + " minutes.");
            }

            throw new RuntimeException("Invalid credentials. " +
                    remainingAttempts + " attempts remaining.");
        }

        // Extract tracking information from request
        String ipAddress = requestHelper.getCurrentIpAddress();
        String device = requestHelper.getDeviceInfoString();
        String userAgent = requestHelper.getUserAgent();

        // Successful login - record with tracking
        user.recordSuccessfulLogin(ipAddress, device, userAgent);
        userRepository.save(user);

        // Generate tokens with security tracking
        String accessToken = jwtService.generateToken(user);
        String refreshToken = issueRefreshToken(user, httpRequest);

        // Send chat session conversion event
        if (request.getSessionId() != null) {
            chatEventProducer.sendSessionConversion(request.getSessionId(), user.getId());
        }

        // Enhanced logging with tracking info
        log.info("✅ User logged in successfully: {} | IP: {} | Device: {} | Login #{}",
                user.getUsername(),
                ipAddress,
                device,
                user.getLoginCount());

        // Check if password change required
        boolean requiresPasswordChange = user.needsPasswordChange() ||
                user.isPasswordExpired(90); // 90 days policy

        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getRole().getName().name())
                .avatarUrl(getAvatarUrl(user))
                .email(user.getEmail())
                .fullName(user.getFullName())
                .emailVerified(user.hasVerifiedEmail())
                .twoFactorEnabled(user.hasTwoFactorEnabled())
                .requiresPasswordChange(requiresPasswordChange)
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    /**
     * Verify email with token
     */
    @Override
    public boolean verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (user.verifyEmail(token)) {
            userRepository.save(user);
            log.info("✅ Email verified for user: {}", user.getUsername());
            return true;
        }

        log.warn("❌ Email verification failed for user: {} (expired or invalid)", user.getUsername());
        return false;
    }

    /**
     * Resend email verification
     */
    @Override
    public void resendEmailVerification(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.hasVerifiedEmail()) {
            throw new RuntimeException("Email already verified");
        }

        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken, EMAIL_VERIFICATION_EXPIRY_HOURS);
        userRepository.save(user);

        if (emailService != null) {
            emailService.sendVerificationEmail(user, verificationToken);
        }

        log.info("✅ Verification email resent to: {}", user.getEmail());
    }

    /**
     * Request password reset
     */
    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken, PASSWORD_RESET_TOKEN_EXPIRY_HOURS);
        userRepository.save(user);

        // Send reset email
        if (emailService != null) {
            emailService.sendPasswordResetEmail(user, resetToken);
        }

        log.info("✅ Password reset requested for: {} ({})", user.getUsername(), email);
    }

    /**
     * Reset password with token
     */
    @Override
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (!user.isResetTokenValid(token)) {
            throw new RuntimeException("Invalid or expired reset token");
        }

        // Update password
        user.updatePassword(passwordEncoder.encode(newPassword));

        // Revoke all existing refresh tokens for security
        revokeAllUserTokens(user.getId());

        userRepository.save(user);
        log.info("✅ Password reset successfully for user: {}", user.getUsername());
    }

    /**
     * Change password (authenticated user)
     */
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.updatePassword(passwordEncoder.encode(newPassword));

        // Revoke all existing tokens for security
        revokeAllUserTokens(userId);

        userRepository.save(user);

        log.info("✅ Password changed for user: {}", user.getUsername());
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        return refreshToken(refreshToken, null);
    }

    /**
     * Enhanced refresh token with usage tracking
     */
    @Override
    public AuthResponse refreshToken(String refreshToken, HttpServletRequest httpRequest) {
        // Validate JWT refresh token
        if (!jwtService.validateRefreshToken(refreshToken)) {
            log.warn("❌ Invalid or expired refresh token");
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // Verify token exists in database
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found in database"));

        // Check if token is valid
        if (!tokenEntity.isValid()) {
            log.warn("❌ Token is not valid - expired: {}, revoked: {}, deleted: {}",
                    tokenEntity.isExpired(),
                    tokenEntity.isRevoked(),
                    tokenEntity.getDeletedAt() != null);
            refreshTokenRepository.delete(tokenEntity);
            throw new RuntimeException("Refresh token is invalid");
        }

        User user = tokenEntity.getUser();

        // Check if user is still active
        if (!user.isActive()) {
            throw new RuntimeException("User account is not active");
        }

        // Track usage
        tokenEntity.incrementUseCount();
        refreshTokenRepository.save(tokenEntity);

        // Rotate refresh token for security
        String newRefreshToken = rotateRefreshToken(tokenEntity, user, httpRequest);

        // Generate new access token
        String newAccessToken = jwtService.generateToken(user);

        log.info("✅ Token refreshed successfully for user: {}", user.getUsername());

        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .username(user.getUsername())
                .role(user.getRole().getName().name())
                .avatarUrl(getAvatarUrl(user))
                .email(user.getEmail())
                .fullName(user.getFullName())
                .emailVerified(user.hasVerifiedEmail())
                .build();
    }

    /**
     * Logout user by revoking refresh token
     */
    @Override
    public void logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);

                // Get username from token
                String username = jwtService.extractUsername(accessToken);

                // Find and revoke all user's refresh tokens
                User user = userRepository.findByUsername(username)
                        .orElse(null);

                if (user != null) {
                    List<RefreshToken> userTokens = refreshTokenRepository.findByUser(user);
                    for (RefreshToken token : userTokens) {
                        token.revoke(username, "User logout");
                    }
                    refreshTokenRepository.saveAll(userTokens);

                    log.info("✅ User logged out successfully: {}", username);
                }

                // Clear SecurityContext
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            log.error("❌ Logout failed: {}", e.getMessage());
        }
    }

    /**
     *  @param userId   </br>
     *  Revoke all refresh tokens for a user    </br>
     *  Useful for password change or security breach
     */
    @Override
    public void revokeAllUserTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);
        for (RefreshToken token : tokens) {
            token.revoke("system", "All tokens revoked - security action");
        }
        refreshTokenRepository.saveAll(tokens);

        log.info("✅ All tokens revoked for user ID: {}", userId);
    }

    /**
     * Revoke specific refresh token
     */
    @Override
    public void revokeToken(String token, String reason) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(tokenEntity -> {
                    tokenEntity.revoke("system", reason);
                    refreshTokenRepository.save(tokenEntity);
                    log.info("✅ Token revoked: {}", reason);
                });
    }

    @Override
    public String getAvatarUrl(User user) {
        List<Image> images = imageRepository.findImageByUser(user);
        return images.stream()
                .filter(Image::isAvatar)
                .map(Image::getUrl)
                .findFirst()
                .orElse(null);
    }

    private UserRole getRole(RoleName roleName) {
        return userRoleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
    }

    /**
     * Issue new refresh token with full security tracking
     */
    @Override
    public String issueRefreshToken(User user, HttpServletRequest request) {
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

        // Extract and set security information if request is available
        if (request != null) {
            tokenEntity.setIpAddress(requestHelper.getCurrentIpAddress());
            tokenEntity.setUserAgent(requestHelper.getUserAgent());
            tokenEntity.setDeviceId(requestHelper.getDeviceInfoString());
            tokenEntity.setDeviceName(requestHelper.extractDeviceName(request));
        }

        refreshTokenRepository.save(tokenEntity);

        log.info("✅ Refresh token issued for user: {} (IP: {}, Device: {})",
                user.getUsername(),
                tokenEntity.getIpAddress(),
                tokenEntity.getDeviceName());

        return jwtRefreshToken;
    }

    /**
     * Rotate refresh token (generate new one and invalidate old)
     *
     * @param oldToken Old token entity to invalidate
     * @param user User to issue new token for
     * @return New JWT refresh token string
     */
    @Override
    public String rotateRefreshToken(RefreshToken oldToken, User user, HttpServletRequest request) {
        // Generate new JWT refresh token
        String newJwtToken = jwtService.generateRefreshToken(user);

        // Create new token maintaining the token family
        RefreshToken newToken = RefreshToken.builder()
                .token(newJwtToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(REFRESH_TTL_DAYS))
                .createdBy(user.getUsername())
                .tokenFamily(oldToken.getTokenFamily())     // Keep same family
                .parentTokenId(oldToken.getId())            // Track parent
                .build();

        // Copy security info from request or old token
        if (request != null) {
            newToken.setIpAddress(requestHelper.getCurrentIpAddress());
            newToken.setUserAgent(requestHelper.getUserAgent());
            newToken.setDeviceId(requestHelper.getDeviceInfoString());
            newToken.setDeviceName(requestHelper.extractDeviceName(request));
        } else {
            // Inherit from old token
            newToken.setIpAddress(oldToken.getIpAddress());
            newToken.setUserAgent(oldToken.getUserAgent());
            newToken.setDeviceId(oldToken.getDeviceId());
            newToken.setDeviceName(oldToken.getDeviceName());
        }

        // Soft delete old token instead of hard delete
        oldToken.softDelete(user.getUsername());

        // Save both tokens
        refreshTokenRepository.save(oldToken);
        refreshTokenRepository.save(newToken);

        log.info("✅ Refresh token rotated for user: {} (family: {})",
                user.getUsername(),
                newToken.getTokenFamily());

        return newJwtToken;
    }
}