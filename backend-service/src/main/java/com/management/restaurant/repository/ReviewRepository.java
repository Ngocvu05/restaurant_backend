package com.management.restaurant.repository;

import com.management.restaurant.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Find reviews by dish ID
    List<Review> findByDishIdAndIsActiveTrue(Long dishId);

    Page<Review> findByDishIdAndIsActiveTrue(Long dishId, Pageable pageable);

    // Find reviews by dish ID with specific rating
    List<Review> findByDishIdAndRatingAndIsActiveTrue(Long dishId, Integer rating);

    Page<Review> findByDishIdAndRatingAndIsActiveTrue(Long dishId, Integer rating, Pageable pageable);

    // Find reviews by dish ID and verified status
    Page<Review> findByDishIdAndIsActiveTrueAndIsVerified(Long dishId, Boolean isVerified, Pageable pageable);

    // Count reviews by dish ID
    long countByDishIdAndIsActiveTrue(Long dishId);

    // Count reviews by dish ID and rating
    long countByDishIdAndRatingAndIsActiveTrue(Long dishId, Integer rating);

    // Calculate average rating for a dish
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.dishId = :dishId AND r.isActive = true")
    Optional<Double> findAverageRatingByDishId(@Param("dishId") Long dishId);

    // Get rating distribution for a dish
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.dishId = :dishId AND r.isActive = true GROUP BY r.rating")
    List<Object[]> findRatingDistributionByDishId(@Param("dishId") Long dishId);

    // Find recent reviews
    List<Review> findTop10ByIsActiveTrueOrderByCreatedAtDesc();

    // Find reviews by customer email (to prevent spam)
    List<Review> findByCustomerEmailAndDishIdAndIsActiveTrue(String customerEmail, Long dishId);

    // Find reviews by IP address (to prevent spam)
    List<Review> findByIpAddressAndDishIdAndCreatedAtAfter(String ipAddress, Long dishId, LocalDateTime after);

    // Admin queries
    Page<Review> findByIsVerified(Boolean isVerified, Pageable pageable);

    Page<Review> findByIsActive(Boolean isActive, Pageable pageable);

    // Find reviews containing specific keywords (for moderation)
    @Query("SELECT r FROM Review r WHERE LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) AND r.isActive = true")
    List<Review> findByCommentContainingIgnoreCase(@Param("keyword") String keyword);

    // Custom query for complex filtering
    @Query("SELECT r FROM Review r WHERE " +
            "(:dishId IS NULL OR r.dishId = :dishId) AND " +
            "(:rating IS NULL OR r.rating = :rating) AND " +
            "(:isVerified IS NULL OR r.isVerified = :isVerified) AND " +
            "r.isActive = true " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findReviewsWithFilters(
            @Param("dishId") Long dishId,
            @Param("rating") Integer rating,
            @Param("isVerified") Boolean isVerified,
            Pageable pageable
    );

    // Statistics queries
    @Query("SELECT COUNT(r) FROM Review r WHERE r.dishId = :dishId AND r.isActive = true AND r.createdAt >= :fromDate")
    long countRecentReviewsByDishId(@Param("dishId") Long dishId, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT r.dishId, COUNT(r), AVG(r.rating) FROM Review r WHERE r.isActive = true GROUP BY r.dishId HAVING COUNT(r) >= :minReviews ORDER BY AVG(r.rating) DESC")
    List<Object[]> findTopRatedDishesWithMinReviews(@Param("minReviews") long minReviews);
}