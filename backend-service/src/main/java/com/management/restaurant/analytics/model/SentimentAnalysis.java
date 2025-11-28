package com.management.restaurant.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentAnalysis {
    private String sentimentType; // POSITIVE, NEUTRAL, NEGATIVE

    private Double confidenceScore; // 0.0 - 1.0

    private List<String> positiveWords;

    private List<String> negativeWords;
}
