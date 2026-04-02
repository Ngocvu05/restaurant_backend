package com.management.restaurant.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token", indexes = {
        @Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
        @Index(name = "idx_refresh_token_expiry_date", columnList = "expiry_date"),
        @Index(name = "idx_refresh_token_revoked", columnList = "revoked"),
        @Index(name = "idx_refresh_token_deleted_at", columnList = "deleted_at")
})
@SQLDelete(sql = "UPDATE refresh_token SET deleted_at = NOW(), deleted_by = 'system' WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 767)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "created_by", length = 100)
    private String createdBy = "system";

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Soft delete
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    // Token status
    @Builder.Default
    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_by", length = 100)
    private String revokedBy;

    @Column(name = "revoked_reason")
    private String revokedReason;

    // Security tracking
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    // Token rotation
    @Column(name = "token_family", length = 100)
    private String tokenFamily;

    @Column(name = "parent_token_id")
    private Long parentTokenId;

    // Usage tracking
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Builder.Default
    @Column(name = "use_count", nullable = false)
    private int useCount = 0;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (createdBy == null) {
            createdBy = "system";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void revoke(String revokedBy, String reason) {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
        this.revokedBy = revokedBy;
        this.revokedReason = reason;
        this.updatedBy = revokedBy;
    }

    /**
     * Increment use count and update last used timestamp
     */
    public void incrementUseCount() {
        this.useCount++;
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    /**
     * Check if token is valid (not expired, not revoked, not deleted)
     */
    public boolean isValid() {
        return !isExpired() && !revoked && deletedAt == null;
    }

    /**
     * Soft delete this token
     */
    public void softDelete(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}