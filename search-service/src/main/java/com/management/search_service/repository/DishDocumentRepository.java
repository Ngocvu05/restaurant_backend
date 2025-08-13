package com.management.search_service.repository;

import com.management.search_service.document.DishDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DishDocumentRepository extends ElasticsearchRepository<DishDocument,Long> {
    Optional<DishDocument> findByDishId(Long dishId);

    Page<DishDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable);

    Page<DishDocument> findByCategory(String category, Pageable pageable);

    Page<DishDocument> findByIsAvailable(Boolean isAvailable, Pageable pageable);

    Page<DishDocument> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    List<DishDocument> findByAverageRatingGreaterThanEqual(BigDecimal rating);

    void deleteByDishId(Long dishId);
}