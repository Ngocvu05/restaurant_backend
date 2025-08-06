package com.management.restaurant.controller;

import com.management.restaurant.dto.review.ReviewDTO;
import com.management.restaurant.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;

    // ====== Public Endpoints for Customer Reviews ======

    /**
     * Create a new review for a dish
     */
    @PostMapping("/dishes/{dishId}/reviews")
    public ResponseEntity<Map<String, Object>> createReview(
            @PathVariable Long dishId,
            @Valid @RequestBody ReviewDTO reviewDTO,
            HttpServletRequest request) {

        log.info("Creating review for dish ID: {}", dishId);
        reviewDTO.setDishId(dishId);

        String ipAddress = getClientIpAddress(request);
        ReviewDTO createdReview = reviewService.createReview(reviewDTO, ipAddress);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Review created successfully");
        response.put("data", createdReview);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all reviews for a specific dish with pagination
     */
    @GetMapping("/dishes/{dishId}/reviews")
    public ResponseEntity<Map<String, Object>> getDishReviews(
            @PathVariable Long dishId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean verified) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ReviewDTO> reviews;
        if (rating != null) {
            reviews = reviewService.getReviewsByDishIdAndRating(dishId, rating, pageable);
        } else {
            reviews = reviewService.getReviewsWithFilters(dishId, null, verified, pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", reviews.getContent());
        response.put("pagination", createPaginationInfo(reviews));

        return ResponseEntity.ok(response);
    }

    /**
     * Get review statistics for a dish
     */
    @GetMapping("/dishes/{dishId}/reviews/stats")
    public ResponseEntity<Map<String, Object>> getDishReviewStats(@PathVariable Long dishId) {
        Double averageRating = reviewService.getAverageRatingByDishId(dishId);
        Long totalReviews = reviewService.getTotalReviewsByDishId(dishId);
        Map<Integer, Long> ratingDistribution = reviewService.getRatingDistributionByDishId(dishId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", averageRating);
        stats.put("totalReviews", totalReviews);
        stats.put("ratingDistribution", ratingDistribution);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);

        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific review by ID
     */
    @GetMapping("/reviews/{id}")
    public ResponseEntity<Map<String, Object>> getReview(@PathVariable Long id) {
        ReviewDTO review = reviewService.getReviewById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", review);

        return ResponseEntity.ok(response);
    }

    // ====== Admin Endpoints for Review Management ======

    /**
     * Get all reviews with advanced filtering (Admin only)
     */
    @GetMapping("/admin/reviews")
    public ResponseEntity<Map<String, Object>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long dishId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean verified) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ReviewDTO> reviews = reviewService.getReviewsWithFilters(dishId, rating, verified, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", reviews.getContent());
        response.put("pagination", createPaginationInfo(reviews));

        return ResponseEntity.ok(response);
    }

    /**
     * Update a review (Admin only)
     */
    @PutMapping("/admin/reviews/{id}")
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDTO reviewDTO) {

        ReviewDTO updatedReview = reviewService.updateReview(id, reviewDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Review updated successfully");
        response.put("data", updatedReview);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a review (Admin only)
     */
    @DeleteMapping("/admin/reviews/{id}")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Review deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Verify a review (Admin only)
     */
    @PutMapping("/admin/reviews/{id}/verify")
    public ResponseEntity<Map<String, Object>> verifyReview(@PathVariable Long id) {
        ReviewDTO verifiedReview = reviewService.verifyReview(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Review verified successfully");
        response.put("data", verifiedReview);

        return ResponseEntity.ok(response);
    }

    /**
     * Unverify a review (Admin only)
     */
    @PutMapping("/admin/reviews/{id}/unverify")
    public ResponseEntity<Map<String, Object>> unverifyReview(@PathVariable Long id) {
        ReviewDTO unverifiedReview = reviewService.unverifyReview(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Review unverified successfully");
        response.put("data", unverifiedReview);

        return ResponseEntity.ok(response);
    }

    /**
     * Activate a review (Admin only)
     */
    @PutMapping("/admin/reviews/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateReview(@PathVariable Long id) {
        ReviewDTO activatedReview = reviewService.activateReview(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Review activated successfully");
        response.put("data", activatedReview);

        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a review (Admin only)
     */
    @PutMapping("/admin/reviews/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateReview(@PathVariable Long id) {
        ReviewDTO deactivatedReview = reviewService.deactivateReview(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Review deactivated successfully");
        response.put("data", deactivatedReview);

        return ResponseEntity.ok(response);
    }

    /**
     * Get unverified reviews (Admin only)
     */
    @GetMapping("/admin/reviews/unverified")
    public ResponseEntity<Map<String, Object>> getUnverifiedReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewDTO> reviews = reviewService.getUnverifiedReviews(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", reviews.getContent());
        response.put("pagination", createPaginationInfo(reviews));

        return ResponseEntity.ok(response);
    }

    /**
     * Get recent reviews (Admin only)
     */
    @GetMapping("/admin/reviews/recent")
    public ResponseEntity<Map<String, Object>> getRecentReviews(
            @RequestParam(defaultValue = "10") int limit) {

        List<ReviewDTO> reviews = reviewService.getRecentReviews(limit);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", reviews);

        return ResponseEntity.ok(response);
    }

    /**
     * Search reviews by keyword (Admin only)
     */
    @GetMapping("/admin/reviews/search")
    public ResponseEntity<Map<String, Object>> searchReviews(
            @RequestParam String keyword) {

        List<ReviewDTO> reviews = reviewService.findReviewsWithKeyword(keyword);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", reviews);
        response.put("total", reviews.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk verify reviews (Admin only)
     */
    @PutMapping("/admin/reviews/bulk-verify")
    public ResponseEntity<Map<String, Object>> bulkVerifyReviews(
            @RequestBody Map<String, List<Long>> requestBody) {

        List<Long> reviewIds = requestBody.get("reviewIds");
        reviewService.bulkVerifyReviews(reviewIds);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Reviews verified successfully");
        response.put("processedCount", reviewIds.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk delete reviews (Admin only)
     */
    @DeleteMapping("/admin/reviews/bulk-delete")
    public ResponseEntity<Map<String, Object>> bulkDeleteReviews(
            @RequestBody Map<String, List<Long>> requestBody) {

        List<Long> reviewIds = requestBody.get("reviewIds");
        reviewService.bulkDeleteReviews(reviewIds);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Reviews deleted successfully");
        response.put("processedCount", reviewIds.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Update all dish rating statistics (Admin only)
     */
    @PostMapping("/admin/reviews/update-stats")
    public ResponseEntity<Map<String, Object>> updateAllDishStats() {
        reviewService.updateAllDishRatingStats();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All dish rating statistics updated successfully");

        return ResponseEntity.ok(response);
    }

    // ====== Helper Methods ======

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    private Map<String, Object> createPaginationInfo(Page<?> page) {
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page.getNumber());
        pagination.put("totalPages", page.getTotalPages());
        pagination.put("totalItems", page.getTotalElements());
        pagination.put("itemsPerPage", page.getSize());
        pagination.put("hasNext", page.hasNext());
        pagination.put("hasPrev", page.hasPrevious());
        return pagination;
    }
}