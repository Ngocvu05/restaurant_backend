package com.management.restaurant.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingDistribution {
    private int oneStar = 0;
    private int twoStar = 0;
    private int threeStar = 0;
    private int fourStar = 0;
    private int fiveStar = 0;
}