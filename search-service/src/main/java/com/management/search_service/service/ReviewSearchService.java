package com.management.search_service.service;

import com.management.search_service.document.ReviewDocument;
import com.management.search_service.events.implement.ReviewEvent;
import com.management.search_service.repository.ReviewDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewSearchService {
    private final ReviewDocumentRepository reviewDocumentRepository;

    /**
     * Index review - evict all review caches
     */
    @Caching(evict = {
            @CacheEvict(value = "reviews", allEntries = true, cacheManager = "redisCacheManager"),
            @CacheEvict(value = "reviews", allEntries = true, cacheManager = "caffeineCacheManager")
    })
    public void indexReview(ReviewEvent event) {
        try {
            ReviewDocument document = ReviewDocument.builder()
                    .id(event.getReviewId().toString())
                    .reviewId(event.getReviewId())
                    .dishId(event.getDishId())
                    .customerName(event.getCustomerName())
                    .customerEmail(event.getCustomerEmail())
                    .customerAvatar(event.getCustomerAvatar())
                    .rating(event.getRating())
                    .comment(event.getComment())
                    .isActive(event.getIsActive())
                    .isVerified(event.getIsVerified())
                    .createdAt(event.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();

            reviewDocumentRepository.save(document);
            log.info("Indexed review document: {}", event.getReviewId());
        } catch (Exception e) {
            log.error("Failed to index review: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete review - evict caches
     */
    @Caching(evict = {
            @CacheEvict(value = "reviews", allEntries = true, cacheManager = "redisCacheManager"),
            @CacheEvict(value = "reviews", allEntries = true, cacheManager = "caffeineCacheManager")
    })
    public void deleteReview(Long reviewId) {
        try {
            reviewDocumentRepository.deleteByReviewId(reviewId);
            log.info("Deleted review document: {}", reviewId);
        } catch (Exception e) {
            log.error("Failed to delete review: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Search reviews - cache by query + page
     */
    @Cacheable(value = "reviews",
            key = "'search:' + (#query ?: 'all') + ':' + #pageable.pageNumber + ':' + #pageable.pageSize",
            cacheManager = "redisCacheManager")
    public Page<ReviewDocument> searchReviews(String query, Pageable pageable) {
        log.info("Searching reviews: query={}, page={} [CACHE MISS]", query, pageable.getPageNumber());
        if (query == null || query.trim().isEmpty()) {
            return reviewDocumentRepository.findAll(pageable);
        }
        return reviewDocumentRepository.findByCommentContainingIgnoreCase(query, pageable);
    }

    /**
     * Find by dish - frequently accessed, cache enabled
     */
    @Cacheable(value = "reviews",
            key = "'dish:' + #dishId + ':' + #pageable.pageNumber",
            cacheManager = "redisCacheManager")
    public Page<ReviewDocument> findByDish(Long dishId, Pageable pageable) {
        log.info("Finding reviews for dish: {} [CACHE MISS]", dishId);
        return reviewDocumentRepository.findByDishId(dishId, pageable);
    }

    /**
     * Find by rating - cache enabled
     */
    @Cacheable(value = "reviews",
            key = "'rating:' + #rating + ':' + #pageable.pageNumber",
            cacheManager = "redisCacheManager")
    public Page<ReviewDocument> findByRating(Integer rating, Pageable pageable) {
        log.info("Finding reviews with rating: {} [CACHE MISS]", rating);
        return reviewDocumentRepository.findByRating(rating, pageable);
    }

    /**
     * Find active reviews - frequently accessed
     */
    @Cacheable(value = "reviews",
            key = "'active:' + #pageable.pageNumber",
            cacheManager = "redisCacheManager")
    public Page<ReviewDocument> findActiveReviews(Pageable pageable) {
        log.info("Finding active reviews [CACHE MISS]");
        return reviewDocumentRepository.findByIsActive(true, pageable);
    }

    /**
     * Find active reviews by dish - high traffic endpoint
     */
    @Cacheable(value = "reviews",
            key = "'dish-active:' + #dishId",
            cacheManager = "redisCacheManager")
    public List<ReviewDocument> findActiveReviewsByDish(Long dishId) {
        log.info("Finding active reviews for dish: {} [CACHE MISS]", dishId);
        return reviewDocumentRepository.findByDishIdAndIsActive(dishId, true);
    }

    /**
     * Find by ID - cache individual reviews
     */
    @Cacheable(value = "reviews",
            key = "'id:' + #reviewId",
            cacheManager = "redisCacheManager")
    public Optional<ReviewDocument> findById(Long reviewId) {
        log.info("Finding review by ID: {} [CACHE MISS]", reviewId);
        return reviewDocumentRepository.findByReviewId(reviewId);
    }
}