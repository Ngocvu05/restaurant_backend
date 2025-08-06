package com.management.restaurant.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewStatsDTO {
    private Double averageRating;
    private Integer totalReviews;
    private RatingDistributionDTO ratingDistribution;
}
