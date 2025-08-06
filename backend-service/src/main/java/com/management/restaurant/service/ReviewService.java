package com.management.restaurant.service;

import com.management.restaurant.dto.review.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    // Basic CRUD operations
    ReviewDTO createReview(ReviewDTO reviewDTO, String ipAddress);
    ReviewDTO getReviewById(Long id);
    List<ReviewDTO> getAllReviews();
    ReviewDTO updateReview(Long id, ReviewDTO reviewDTO);
    void deleteReview(Long id);

    // Dish-specific operations
    List<ReviewDTO> getReviewsByDishId(Long dishId);
    Page<ReviewDTO> getReviewsByDishId(Long dishId, Pageable pageable);
    Page<ReviewDTO> getReviewsByDishIdAndRating(Long dishId, Integer rating, Pageable pageable);

    // Statistics and analytics
    Double getAverageRatingByDishId(Long dishId);
    Long getTotalReviewsByDishId(Long dishId);
    Map<Integer, Long> getRatingDistributionByDishId(Long dishId);

    // Review moderation
    ReviewDTO verifyReview(Long id);
    ReviewDTO unverifyReview(Long id);
    ReviewDTO activateReview(Long id);
    ReviewDTO deactivateReview(Long id);

    // Advanced filtering
    Page<ReviewDTO> getReviewsWithFilters(Long dishId, Integer rating, Boolean isVerified, Pageable pageable);

    // Spam prevention
    boolean canUserReview(String customerEmail, Long dishId);
    boolean canIpReview(String ipAddress, Long dishId);

    // Bulk operations
    void updateDishRatingStats(Long dishId);
    void updateAllDishRatingStats();

    // Admin operations
    Page<ReviewDTO> getUnverifiedReviews(Pageable pageable);
    Page<ReviewDTO> getInactiveReviews(Pageable pageable);
    List<ReviewDTO> getRecentReviews(int limit);

    // Content moderation
    List<ReviewDTO> findReviewsWithKeyword(String keyword);
    void bulkVerifyReviews(List<Long> reviewIds);
    void bulkDeleteReviews(List<Long> reviewIds);
}