package com.management.restaurant.service.implement;

import com.management.restaurant.dto.review.ReviewDTO;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.exception.ValidationException;
import com.management.restaurant.mapper.ReviewMapper;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.Review;
import com.management.restaurant.repository.DishRepository;
import com.management.restaurant.repository.ReviewRepository;
import com.management.restaurant.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final DishRepository dishRepository;
    private final ReviewMapper reviewMapper;

    // Spam prevention constants
    private static final int MAX_REVIEWS_PER_EMAIL_PER_DISH = 1;
    private static final int MAX_REVIEWS_PER_IP_PER_HOUR = 3;
    private static final int REVIEW_COOLDOWN_HOURS = 1;

    @Override
    public ReviewDTO createReview(ReviewDTO reviewDTO, String ipAddress) {
        log.info("Creating review for dish ID: {}", reviewDTO.getDishId());

        // Validate dish exists
        Dish dish = dishRepository.findById(reviewDTO.getDishId())
                .orElseThrow(() -> new NotFoundException("Dish not found with id: " + reviewDTO.getDishId()));

        // Validate review data
        validateReviewData(reviewDTO);

        // Check spam prevention
        if (!canUserReview(reviewDTO.getCustomerEmail(), reviewDTO.getDishId())) {
            throw new ValidationException("You have already reviewed this dish");
        }

        if (!canIpReview(ipAddress, reviewDTO.getDishId())) {
            throw new ValidationException("Too many reviews from this location. Please try again later.");
        }

        // Create review entity
        Review review = reviewMapper.toEntity(reviewDTO);
        review.setIpAddress(ipAddress);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        // Save review
        Review savedReview = reviewRepository.save(review);

        // Update dish rating statistics
        updateDishRatingStats(reviewDTO.getDishId());

        log.info("Review created successfully with ID: {}", savedReview.getId());
        return reviewMapper.toDTO(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));
        return reviewMapper.toDTO(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        return reviewMapper.toDTOList(reviews);
    }

    @Override
    public ReviewDTO updateReview(Long id, ReviewDTO reviewDTO) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));

        // Update allowed fields
        if (reviewDTO.getRating() != null) {
            existingReview.setRating(reviewDTO.getRating());
        }
        if (reviewDTO.getComment() != null) {
            existingReview.setComment(reviewDTO.getComment());
        }
        if (reviewDTO.getIsActive() != null) {
            existingReview.setIsActive(reviewDTO.getIsActive());
        }
        if (reviewDTO.getIsVerified() != null) {
            existingReview.setIsVerified(reviewDTO.getIsVerified());
        }

        existingReview.setUpdatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(existingReview);

        // Update dish rating statistics if rating changed
        updateDishRatingStats(existingReview.getDishId());

        return reviewMapper.toDTO(savedReview);
    }

    @Override
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));

        Long dishId = review.getDishId();
        reviewRepository.delete(review);

        // Update dish rating statistics
        updateDishRatingStats(dishId);

        log.info("Review deleted with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByDishId(Long dishId) {
        List<Review> reviews = reviewRepository.findByDishIdAndIsActiveTrue(dishId);
        return reviewMapper.toDTOList(reviews);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByDishId(Long dishId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByDishIdAndIsActiveTrue(dishId, pageable);
        return reviews.map(reviewMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByDishIdAndRating(Long dishId, Integer rating, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByDishIdAndRatingAndIsActiveTrue(dishId, rating, pageable);
        return reviews.map(reviewMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingByDishId(Long dishId) {
        return reviewRepository.findAverageRatingByDishId(dishId).orElse(0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalReviewsByDishId(Long dishId) {
        return reviewRepository.countByDishIdAndIsActiveTrue(dishId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Long> getRatingDistributionByDishId(Long dishId) {
        List<Object[]> results = reviewRepository.findRatingDistributionByDishId(dishId);
        Map<Integer, Long> distribution = new HashMap<>();

        // Initialize all ratings with 0
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        // Fill with actual data
        for (Object[] result : results) {
            Integer rating = (Integer) result[0];
            Long count = (Long) result[1];
            distribution.put(rating, count);
        }

        return distribution;
    }

    @Override
    public ReviewDTO verifyReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));

        review.setIsVerified(true);
        review.setUpdatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);

        log.info("Review verified with ID: {}", id);
        return reviewMapper.toDTO(savedReview);
    }

    @Override
    public ReviewDTO unverifyReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));

        review.setIsVerified(false);
        review.setUpdatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);

        return reviewMapper.toDTO(savedReview);
    }

    @Override
    public ReviewDTO activateReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));

        review.setIsActive(true);
        review.setUpdatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);

        updateDishRatingStats(review.getDishId());
        return reviewMapper.toDTO(savedReview);
    }

    @Override
    public ReviewDTO deactivateReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));

        review.setIsActive(false);
        review.setUpdatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);

        updateDishRatingStats(review.getDishId());
        return reviewMapper.toDTO(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsWithFilters(Long dishId, Integer rating, Boolean isVerified, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findReviewsWithFilters(dishId, rating, isVerified, pageable);
        return reviews.map(reviewMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserReview(String customerEmail, Long dishId) {
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            return true; // Allow anonymous reviews
        }

        List<Review> existingReviews = reviewRepository.findByCustomerEmailAndDishIdAndIsActiveTrue(customerEmail, dishId);
        return existingReviews.size() < MAX_REVIEWS_PER_EMAIL_PER_DISH;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canIpReview(String ipAddress, Long dishId) {
        if (ipAddress == null) {
            return true;
        }

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(REVIEW_COOLDOWN_HOURS);
        List<Review> recentReviews = reviewRepository.findByIpAddressAndDishIdAndCreatedAtAfter(ipAddress, dishId, oneHourAgo);
        return recentReviews.size() < MAX_REVIEWS_PER_IP_PER_HOUR;
    }

    @Override
    public void updateDishRatingStats(Long dishId) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Dish not found with id: " + dishId));

        // Load reviews and update stats
        List<Review> activeReviews = reviewRepository.findByDishIdAndIsActiveTrue(dishId);
        dish.setReviews(activeReviews);
        dish.updateRatingStats();

        dishRepository.save(dish);
        log.debug("Updated rating stats for dish ID: {}", dishId);
    }

    @Override
    public void updateAllDishRatingStats() {
        log.info("Starting bulk update of all dish rating statistics");
        List<Dish> allDishes = dishRepository.findAll();

        for (Dish dish : allDishes) {
            try {
                updateDishRatingStats(dish.getId());
            } catch (Exception e) {
                log.error("Error updating rating stats for dish ID: {}", dish.getId(), e);
            }
        }

        log.info("Completed bulk update of dish rating statistics");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getUnverifiedReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByIsVerified(false, pageable);
        return reviews.map(reviewMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getInactiveReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByIsActive(false, pageable);
        return reviews.map(reviewMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getRecentReviews(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Review> reviews = reviewRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc();
        return reviewMapper.toDTOList(reviews.stream().limit(limit).collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> findReviewsWithKeyword(String keyword) {
        List<Review> reviews = reviewRepository.findByCommentContainingIgnoreCase(keyword);
        return reviewMapper.toDTOList(reviews);
    }

    @Override
    public void bulkVerifyReviews(List<Long> reviewIds) {
        log.info("Bulk verifying {} reviews", reviewIds.size());
        for (Long reviewId : reviewIds) {
            try {
                verifyReview(reviewId);
            } catch (Exception e) {
                log.error("Error verifying review ID: {}", reviewId, e);
            }
        }
    }

    @Override
    public void bulkDeleteReviews(List<Long> reviewIds) {
        log.info("Bulk deleting {} reviews", reviewIds.size());
        for (Long reviewId : reviewIds) {
            try {
                deleteReview(reviewId);
            } catch (Exception e) {
                log.error("Error deleting review ID: {}", reviewId, e);
            }
        }
    }

    // Private helper methods
    private void validateReviewData(ReviewDTO reviewDTO) {
        if (reviewDTO.getCustomerName() == null || reviewDTO.getCustomerName().trim().isEmpty()) {
            throw new ValidationException("Customer name is required");
        }

        if (reviewDTO.getCustomerName().length() > 100) {
            throw new ValidationException("Customer name must not exceed 100 characters");
        }

        if (reviewDTO.getRating() == null || reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }

        if (reviewDTO.getComment() == null || reviewDTO.getComment().trim().isEmpty()) {
            throw new ValidationException("Comment is required");
        }

        if (reviewDTO.getComment().length() > 1000) {
            throw new ValidationException("Comment must not exceed 1000 characters");
        }

        // Check for profanity or inappropriate content
        if (containsInappropriateContent(reviewDTO.getComment())) {
            throw new ValidationException("Comment contains inappropriate content");
        }
    }

    private boolean containsInappropriateContent(String content) {
        // Simple profanity filter - in production, use a more sophisticated solution
        String[] inappropriateWords = {"spam", "fake", "scam"}; // Add more as needed
        String lowerContent = content.toLowerCase();

        for (String word : inappropriateWords) {
            if (lowerContent.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
