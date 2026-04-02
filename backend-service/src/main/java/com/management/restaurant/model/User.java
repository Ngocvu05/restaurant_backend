package com.management.restaurant.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.management.restaurant.contains.UserStatus;
import com.management.restaurant.model.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced User Entity with Complete Security & Tracking
 * <p>
 * Security Features:   </br>
 * - Failed login tracking & auto-locking   </br>
 * - Password reset with tokens     </br>
 * - Email verification     </br>
 * - Two-factor authentication (2FA)    </br>
 * - Force password change
 * <p>
 * Tracking Features:   </br>
 * - Last login timestamp   </br>
 * - Last login IP address  </br>
 * - Last login device & user agent </br>
 * - Total login counter    </br>
 * - Password change history
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
@EqualsAndHashCode(callSuper = true, exclude = {"images", "refreshTokens"})
@ToString(exclude = {"images", "refreshTokens"})
public class User extends SoftDeletableEntity {
    // ===== BASIC INFO =====

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phone_number;

    @Column(length = 255)
    private String address;

    // ===== ROLE & STATUS =====

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    // ===== SECURITY FEATURES =====

    /**
     * Number of consecutive failed login attempts  </br>
     * Reset to 0 on successful login
     */
    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    /**
     * Account locked until this timestamp  </br>
     * Null if not locked
     */
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    /**
     * Token for password reset </br>
     * Should be random, unique, and expire after use
     */
    @Column(name = "reset_password_token", length = 100)
    private String resetPasswordToken;

    /**
     * Expiry time for password reset token </br>
     * Typically 1-24 hours from generation
     */
    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    /**
     * Email verification status
     */
    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    /**
     * Email verification token
     */
    @Column(name = "email_verification_token", length = 100)
    private String emailVerificationToken;

    /**
     * Token expiry for email verification
     */
    @Column(name = "email_verification_expiry")
    private LocalDateTime emailVerificationExpiry;

    /**
     * Two-factor authentication enabled
     */
    @Column(name = "two_factor_enabled")
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    /**
     * Two-factor authentication secret </br>
     * For TOTP (Google Authenticator, etc.)
     */
    @Column(name = "two_factor_secret", length = 32)
    private String twoFactorSecret;

    /**
     * Backup codes for 2FA recovery (comma-separated)
     */
    @Column(name = "backup_codes", length = 500)
    private String backupCodes;

    /**
     * Last password change timestamp   </br>
     * For enforcing password rotation policies
     */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    /**
     * Force user to change password on next login  </br>
     * Admin can set this flag for security reasons
     */
    @Column(name = "force_password_change")
    @Builder.Default
    private Boolean forcePasswordChange = false;

    // ===== LOGIN TRACKING =====

    /**
     * Last successful login timestamp  </br>
     * Useful for security auditing
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * IP address from last login   </br>
     * Supports both IPv4 and IPv6
     */
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    /**
     * Device name/type from last login </br>
     * e.g., "Chrome on Windows", "Mobile Safari on iPhone"
     */
    @Column(name = "last_login_device", length = 255)
    private String lastLoginDevice;

    /**
     * Full user agent string from last login   </br>
     * Contains browser, OS, and device information
     */
    @Column(name = "last_login_user_agent", length = 500)
    private String lastLoginUserAgent;

    /**
     * Total number of successful logins    </br>
     * Incremented on each successful login
     */
    @Column(name = "login_count")
    @Builder.Default
    private Integer loginCount = 0;

    // ===== RELATIONSHIPS =====

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Image> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    // ===== BUSINESS LOGIC METHODS =====

    /**
     * Check if user is active and not locked
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE
                && !isDeleted()
                && !isAccountLocked();
    }

    /**
     * Check if account is currently locked
     */
    public boolean isAccountLocked() {
        if (accountLockedUntil == null) {
            return false;
        }

        // Check if lock period has expired
        if (LocalDateTime.now().isAfter(accountLockedUntil)) {
            // Auto-unlock
            this.accountLockedUntil = null;
            this.failedLoginAttempts = 0;
            return false;
        }

        return true;
    }

    /**
     * Record failed login attempt  </br>
     * Lock account after configured threshold
     *
     * @param maxAttempts Maximum allowed attempts before locking
     * @param lockDurationMinutes How long to lock account
     */
    public void recordFailedLogin(int maxAttempts, int lockDurationMinutes) {
        this.failedLoginAttempts++;

        if (this.failedLoginAttempts >= maxAttempts) {
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
        }
    }

    /**
     * Reset failed login attempts on successful login  </br>
     * Also update login tracking information
     *
     * @param ipAddress IP address of login
     * @param device Device name/type
     * @param userAgent Full user agent string
     */
    public void recordSuccessfulLogin(String ipAddress, String device, String userAgent) {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        this.lastLoginDevice = device;
        this.lastLoginUserAgent = userAgent;
        this.loginCount = (this.loginCount == null ? 0 : this.loginCount) + 1;
    }

    /**
     * Overloaded method for backward compatibility
     */
    public void recordSuccessfulLogin() {
        recordSuccessfulLogin(null, null, null);
    }

    /**
     * Manually lock account (admin action or security breach)
     *
     * @param durationMinutes How long to lock (0 = indefinite)
     */
    public void lockAccount(int durationMinutes) {
        if (durationMinutes > 0) {
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(durationMinutes);
        } else {
            // Indefinite lock (far future date)
            this.accountLockedUntil = LocalDateTime.now().plusYears(100);
        }
    }

    /**
     * Manually unlock account
     */
    public void unlockAccount() {
        this.accountLockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    /**
     * Generate and set password reset token
     *
     * @param token Random token (should be UUID or secure random)
     * @param expiryHours Hours until token expires
     */
    public void setPasswordResetToken(String token, int expiryHours) {
        this.resetPasswordToken = token;
        this.resetTokenExpiry = LocalDateTime.now().plusHours(expiryHours);
    }

    /**
     * Check if password reset token is valid
     */
    public boolean isResetTokenValid(String token) {
        if (this.resetPasswordToken == null || token == null) {
            return false;
        }

        if (!this.resetPasswordToken.equals(token)) {
            return false;
        }

        return this.resetTokenExpiry != null && !LocalDateTime.now().isAfter(this.resetTokenExpiry);
    }

    /**
     * Clear password reset token after use
     */
    public void clearPasswordResetToken() {
        this.resetPasswordToken = null;
        this.resetTokenExpiry = null;
    }

    /**
     * Set email verification token
     */
    public void setEmailVerificationToken(String token, int expiryHours) {
        this.emailVerificationToken = token;
        this.emailVerificationExpiry = LocalDateTime.now().plusHours(expiryHours);
    }

    /**
     * Verify email with token
     */
    public boolean verifyEmail(String token) {
        if (this.emailVerificationToken == null || token == null) {
            return false;
        }

        if (!this.emailVerificationToken.equals(token)) {
            return false;
        }

        if (this.emailVerificationExpiry == null ||
                LocalDateTime.now().isAfter(this.emailVerificationExpiry)) {
            return false;
        }

        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationExpiry = null;
        return true;
    }

    /**
     * Check if password needs to be changed (rotation policy)
     *
     * @param maxPasswordAgeDays Maximum days before password must be changed
     */
    public boolean isPasswordExpired(int maxPasswordAgeDays) {
        if (passwordChangedAt == null) {
            return false; // No policy for existing users without timestamp
        }

        LocalDateTime expiryDate = passwordChangedAt.plusDays(maxPasswordAgeDays);
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Update password and record timestamp
     */
    public void updatePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.forcePasswordChange = false; // Clear force flag
        // Clear reset token if any
        this.clearPasswordResetToken();
    }

    /**
     * Force user to change password on next login
     */
    public void requirePasswordChange() {
        this.forcePasswordChange = true;
    }

    /**
     * Check if user needs to change password
     */
    public boolean needsPasswordChange() {
        return Boolean.TRUE.equals(this.forcePasswordChange);
    }

    /**
     * Enable two-factor authentication
     */
    public void enableTwoFactor(String secret, List<String> backupCodesList) {
        this.twoFactorEnabled = true;
        this.twoFactorSecret = secret;
        this.backupCodes = String.join(",", backupCodesList);
    }

    /**
     * Disable two-factor authentication
     */
    public void disableTwoFactor() {
        this.twoFactorEnabled = false;
        this.twoFactorSecret = null;
        this.backupCodes = null;
    }

    /**
     * Get backup codes as list
     */
    public List<String> getBackupCodesList() {
        if (backupCodes == null || backupCodes.isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(backupCodes.split(","));
    }

    /**
     * Activate user account
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Deactivate user account (without deleting)
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    /**
     * Add image to user
     */
    public void addImage(Image image) {
        images.add(image);
        image.setUser(this);
    }

    /**
     * Remove image from user
     */
    public void removeImage(Image image) {
        images.remove(image);
        image.setUser(null);
    }

    /**
     * Get avatar URL
     */
    public String getAvatarUrl() {
        return images.stream()
                .filter(Image::isAvatar)
                .map(Image::getUrl)
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if user has verified email
     */
    public boolean hasVerifiedEmail() {
        return Boolean.TRUE.equals(emailVerified);
    }

    /**
     * Check if 2FA is enabled
     */
    public boolean hasTwoFactorEnabled() {
        return Boolean.TRUE.equals(twoFactorEnabled);
    }

    /**
     * Get login activity summary
     */
    public String getLoginActivitySummary() {
        return String.format(
                "Last login: %s from IP: %s, Device: %s, Total logins: %d",
                lastLoginAt != null ? lastLoginAt.toString() : "Never",
                lastLoginIp != null ? lastLoginIp : "Unknown",
                lastLoginDevice != null ? lastLoginDevice : "Unknown",
                loginCount != null ? loginCount : 0
        );
    }
}