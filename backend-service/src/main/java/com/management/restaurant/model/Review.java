package com.management.restaurant.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.management.restaurant.model.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Review Entity with Auditing & Soft Delete
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reviews", indexes = {
        @Index(name = "idx_dish_id", columnList = "dish_id"),
        @Index(name = "idx_customer_email", columnList = "customer_email"),
        @Index(name = "idx_rating", columnList = "rating"),
        @Index(name = "idx_is_active", columnList = "is_active")
})
@EqualsAndHashCode(callSuper = true, exclude = {"dish"})
@ToString(exclude = {"dish"})
public class Review extends SoftDeletableEntity{
    @Column(name = "dish_id", nullable = false)
    private Long dishId;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Column(name = "customer_avatar", length = 500)
    private String customerAvatar;

    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", insertable = false, updatable = false)
    @JsonBackReference
    private Dish dish;

    // Validation methods
    public boolean isValidRating() {
        return rating != null && rating >= 1 && rating <= 5;
    }

    public boolean isValidComment() {
        return comment != null && !comment.trim().isEmpty() && comment.length() <= 1000;
    }

    public boolean isValidCustomerName() {
        return customerName != null && !customerName.trim().isEmpty() && customerName.length() <= 100;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void verify() {
        this.isVerified = true;
    }
}