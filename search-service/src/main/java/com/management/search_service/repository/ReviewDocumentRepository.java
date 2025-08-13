package com.management.search_service.repository;

import com.management.search_service.document.ReviewDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewDocumentRepository extends ElasticsearchRepository<ReviewDocument,String> {
    Optional<ReviewDocument> findByReviewId(Long reviewId);

    Page<ReviewDocument> findByDishId(Long dishId, Pageable pageable);

    Page<ReviewDocument> findByRating(Integer rating, Pageable pageable);

    Page<ReviewDocument> findByIsActive(Boolean isActive, Pageable pageable);

    Page<ReviewDocument> findByIsVerified(Boolean isVerified, Pageable pageable);

    Page<ReviewDocument> findByCommentContainingIgnoreCase(String comment, Pageable pageable);

    List<ReviewDocument> findByDishIdAndIsActive(Long dishId, Boolean isActive);

    void deleteByReviewId(Long reviewId);
}