package com.management.restaurant.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", insertable = false, updatable = false)
    @JsonBackReference
    private Dish dish;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

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
}