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
 * User Entity with Auditing & Soft Delete
 * <p>
 * Changes from original:
 * - Extends SoftDeletableEntity (has audit fields + soft delete)
 * - Removed manual createdAt (now from BaseEntity)
 * - Removed manual ID (now from BaseEntity)
 * - Added @EqualsAndHashCode, @ToString to prevent circular references
 * <p>
 * New Security Features:
 * - Failed login attempt tracking
 * - Account locking mechanism
 * - Password reset token management
 * - Last login tracking
 * - Email verification
 * - Two-factor authentication support
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
     * Number of consecutive failed login attempts
     * Reset to 0 on successful login
     */
    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    /**
     * Account locked until this timestamp
     * Null if not locked
     */
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    /**
     * Token for password reset
     * Should be random, unique, and expire after use
     */
    @Column(name = "reset_password_token", length = 100)
    private String resetPasswordToken;

    /**
     * Expiry time for password reset token
     * Typically 1-24 hours from generation
     */
    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    /**
     * Last successful login timestamp
     * Useful for security auditing
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

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
     * Two-factor authentication secret
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
     * Last password change timestamp
     * For enforcing password rotation policies
     */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;


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
     * Record failed login attempt
     * Lock account after configured threshold (e.g., 5 attempts)
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
     * Reset failed login attempts on successful login
     */
    public void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
        this.lastLoginAt = LocalDateTime.now();
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

        if (this.resetTokenExpiry == null || LocalDateTime.now().isAfter(this.resetTokenExpiry)) {
            return false;
        }

        return true;
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
        // Clear reset token if any
        this.clearPasswordResetToken();
    }

    /**
     * Enable two-factor authentication
     */
    public void enableTwoFactor(String secret, List<String> backupCodes) {
        this.twoFactorEnabled = true;
        this.twoFactorSecret = secret;
        this.backupCodes = String.join(",", backupCodes);
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
}