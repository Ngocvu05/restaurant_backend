package com.management.restaurant.analytics.repository;

import com.management.restaurant.analytics.model.ReviewAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewAnalyticsMongoRepository extends MongoRepository<ReviewAnalytics, String> {
    List<ReviewAnalytics> findByDishId(Long dishId);

    List<ReviewAnalytics> findByReviewId(Long reviewId);

    List<ReviewAnalytics> findByDishIdAndAnalyzedAtBetween(
            Long dishId, LocalDateTime start, LocalDateTime end
    );

    @Query("{ 'sentiment.sentimentType': ?0 }")
    List<ReviewAnalytics> findBySentimentType(String sentimentType);

    @Query("{ 'dishId': ?0, 'keywords': { $in: ?1 } }")
    List<ReviewAnalytics> findByDishIdAndKeywords(Long dishId, List<String> keywords);

    // Aggregation để tìm trending keywords
    @Query(value = "{ 'dishId': ?0 }", fields = "{ 'keywords': 1 }")
    List<ReviewAnalytics> findKeywordsByDishId(Long dishId);
}
