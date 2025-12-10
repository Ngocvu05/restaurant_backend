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
 *
 * Changes from original:
 * - Extends SoftDeletableEntity (has audit fields + soft delete)
 * - Removed manual createdAt (now from BaseEntity)
 * - Removed manual ID (now from BaseEntity)
 * - Added @EqualsAndHashCode, @ToString to prevent circular references
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
@EqualsAndHashCode(callSuper = true, exclude = {"images"})
@ToString(exclude = {"images"})
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Image> images = new ArrayList<>();

    // ===== BUSINESS LOGIC METHODS =====

    /**
     * Check if user is active
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE && !isDeleted();
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
}