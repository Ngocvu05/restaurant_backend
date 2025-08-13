package com.management.restaurant.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.management.restaurant.dto.review.RatingDistribution;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "dishes")
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private BigDecimal price;
    @Column(name = "available")
    private Boolean isAvailable;
    private String category;

    @Builder.Default
    @JsonManagedReference
    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();

    @Builder.Default
    @JsonManagedReference
    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private int orderCount = 0;

    @Builder.Default
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_reviews", nullable = false)
    private int totalReviews = 0;

    public void updateRatingStats() {
        if (reviews == null || reviews.isEmpty()) {
            this.averageRating = BigDecimal.ZERO;
            this.totalReviews = 0;
            return;
        }

        List<Review> activeReviews = reviews.stream()
                .filter(Review::getIsActive)
                .toList();

        this.totalReviews = activeReviews.size();

        if (this.totalReviews > 0) {
            double avgRating = activeReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            this.averageRating = BigDecimal.valueOf(Math.round(avgRating * 100.0) / 100.0);
        } else {
            this.averageRating = BigDecimal.ZERO;
        }
    }

    // Get rating distribution
    public RatingDistribution getRatingDistribution() {
        if (reviews == null || reviews.isEmpty()) {
            return new RatingDistribution();
        }

        List<Review> activeReviews = reviews.stream()
                .filter(Review::getIsActive)
                .toList();

        RatingDistribution distribution = new RatingDistribution();
        for (Review review : activeReviews) {
            switch (review.getRating()) {
                case 1 -> distribution.setOneStar(distribution.getOneStar() + 1);
                case 2 -> distribution.setTwoStar(distribution.getTwoStar() + 1);
                case 3 -> distribution.setThreeStar(distribution.getThreeStar() + 1);
                case 4 -> distribution.setFourStar(distribution.getFourStar() + 1);
                case 5 -> distribution.setFiveStar(distribution.getFiveStar() + 1);
            }
        }
        return distribution;
    }
}