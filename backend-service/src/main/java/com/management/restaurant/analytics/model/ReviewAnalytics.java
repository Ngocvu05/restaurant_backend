package com.management.restaurant.analytics.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "review_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAnalytics {
    @Id
    private String id;

    @Indexed
    private Long reviewId;

    @Indexed
    private Long dishId;

    private Integer rating;

    private String comment;

    private SentimentAnalysis sentiment;

    private List<String> keywords;

    private Integer wordCount;

    @Indexed
    private LocalDateTime analyzedAt;
}