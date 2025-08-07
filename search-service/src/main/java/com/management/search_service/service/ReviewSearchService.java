package com.management.search_service.service;

import com.management.search_service.document.ReviewDocument;
import com.management.search_service.events.implement.ReviewEvent;
import com.management.search_service.repository.ReviewDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void deleteReview(Long reviewId) {
        try {
            reviewDocumentRepository.deleteByReviewId(reviewId);
            log.info("Deleted review document: {}", reviewId);
        } catch (Exception e) {
            log.error("Failed to delete review: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Page<ReviewDocument> searchReviews(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return reviewDocumentRepository.findAll(pageable);
        }
        return reviewDocumentRepository.findByCommentContainingIgnoreCase(query, pageable);
    }

    public Page<ReviewDocument> findByDish(Long dishId, Pageable pageable) {
        return reviewDocumentRepository.findByDishId(dishId, pageable);
    }

    public Page<ReviewDocument> findByRating(Integer rating, Pageable pageable) {
        return reviewDocumentRepository.findByRating(rating, pageable);
    }

    public Page<ReviewDocument> findActiveReviews(Pageable pageable) {
        return reviewDocumentRepository.findByIsActive(true, pageable);
    }

    public List<ReviewDocument> findActiveReviewsByDish(Long dishId) {
        return reviewDocumentRepository.findByDishIdAndIsActive(dishId, true);
    }

    public Optional<ReviewDocument> findById(Long reviewId) {
        return reviewDocumentRepository.findByReviewId(reviewId);
    }
}
