package com.management.restaurant.model;

import com.management.restaurant.model.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * OAuth2 Account Link Entity
 * <p>
 * Allows users to link multiple OAuth2 providers to their account
 * Example: Link both Google and Facebook to same account
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "oauth2_links",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "provider"},
                name = "uk_user_provider"
        ))
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "user")
public class OAuth2Link extends SoftDeletableEntity {
    /**
     * User who owns this OAuth2 link
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * OAuth2 provider name
     * <p>Examples: "google", "facebook", "github", "apple"</p>
     */
    @Column(nullable = false, length = 50)
    private String provider;

    /**
     * OAuth2 provider's user ID
     * The unique ID from the OAuth2 provider
     */
    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    /**
     * Email from OAuth2 provider
     */
    @Column(name = "provider_email", length = 255)
    private String providerEmail;

    /**
     * Display name from OAuth2 provider
     */
    @Column(name = "provider_display_name", length = 255)
    private String providerDisplayName;

    /**
     * Profile picture URL from OAuth2 provider
     */
    @Column(name = "provider_picture_url", length = 500)
    private String providerPictureUrl;

    /**
     * OAuth2 access token (encrypted or hashed)
     * <p>Optional: Store if you need to call provider APIs</p>
     */
    @Column(name = "access_token", length = 1000)
    private String accessToken;

    /**
     * OAuth2 refresh token (encrypted)
     * <p>Optional: For refreshing access tokens</p>
     */
    @Column(name = "refresh_token", length = 1000)
    private String refreshToken;

    /**
     * Token expiry time
     */
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    /**
     * OAuth2 scopes granted
     */
    @Column(name = "scopes", length = 500)
    private String scopes;

    /**
     * When this OAuth2 link was first created
     */
    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;

    /**
     * Last time this OAuth2 provider was used to login
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * Is this the primary OAuth2 provider?
     */
    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    /**
     * Additional metadata from provider (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // ===== BUSINESS LOGIC METHODS =====

    /**
     * Update last used timestamp
     */
    public void recordUsage() {
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Update access token
     */
    public void updateToken(String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = expiresAt;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired() {
        if (tokenExpiresAt == null) {
            return false; // No expiry set
        }
        return LocalDateTime.now().isAfter(tokenExpiresAt);
    }

    /**
     * Set as primary provider
     */
    public void setAsPrimary() {
        this.isPrimary = true;
    }

    /**
     * Remove primary status
     */
    public void removeAsPrimary() {
        this.isPrimary = false;
    }

    /**
     * Check if this is the primary provider
     */
    public boolean isPrimaryProvider() {
        return Boolean.TRUE.equals(isPrimary);
    }

    /**
     * Soft delete this OAuth2 link
     * Marks the link as deleted without removing from database
     * <p>
     * This is useful for:
     * - Audit trail (know when/who unlinked)
     * - Potential restore functionality
     * - Security tracking
     *
     * @param deletedBy Username of person who unlinked this OAuth2 account
     */
    public void softDelete(String deletedBy) {
        this.setDeletedAt(LocalDateTime.now());
        this.setDeletedBy(deletedBy);

        // Clear sensitive token data on soft delete
        this.accessToken = null;
        this.refreshToken = null;
        this.tokenExpiresAt = null;

        // Remove primary status if this was primary
        this.isPrimary = false;
    }

    /**
     * Restore a soft deleted OAuth2 link
     * Allows re-linking a previously unlinked provider
     */
    public void restore() {
        this.setDeletedAt(null);
        this.setDeletedBy(null);
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Clear all OAuth2 tokens
     * For security - clear tokens when they're revoked or expired
     */
    public void clearTokens() {
        this.accessToken = null;
        this.refreshToken = null;
        this.tokenExpiresAt = null;
    }

    /**
     * Check if link needs token refresh
     * Returns true if token expires within next hour
     */
    public boolean needsTokenRefresh() {
        if (tokenExpiresAt == null) {
            return false;
        }

        LocalDateTime oneHourFromNow = LocalDateTime.now().plusHours(1);
        return tokenExpiresAt.isBefore(oneHourFromNow);
    }

    /**
     * Get provider display name or fallback to provider
     */
    public String getDisplayName() {
        if (providerDisplayName != null && !providerDisplayName.isEmpty()) {
            return providerDisplayName;
        }
        return provider.substring(0, 1).toUpperCase() + provider.substring(1);
    }

    /**
     * Check if link is still valid
     * Valid = not deleted and not expired
     */
    public boolean isValid() {
        return !isDeleted() && !isTokenExpired();
    }

    /**
     * Create new OAuth2 link
     */
    public static OAuth2Link createLink(User user, String provider, String providerUserId,
                                        String providerEmail) {
        return OAuth2Link.builder()
                .user(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .providerEmail(providerEmail)
                .linkedAt(LocalDateTime.now())
                .isPrimary(false)
                .build();
    }
}