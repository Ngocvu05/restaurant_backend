package com.management.search_service.service;

import com.management.search_service.document.DishDocument;
import com.management.search_service.events.implement.DishEvent;
import com.management.search_service.model.Dish;
import com.management.search_service.repository.DishDocumentRepository;
import com.management.search_service.repository.DishSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DishSearchService {
    private final DishSearchRepository dishSearchRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final DishDocumentRepository dishDocumentRepository;

    public List<Dish> searchByName(String keyword) {
        log.info("Searching dishes by name: {}", keyword);
        return dishSearchRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<Dish> searchByCategory(String category) {
        log.info("Searching dishes by category: {}", category);
        return dishSearchRepository.findByCategory(category);
    }

    public List<Dish> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Searching dishes by price range: {} - {}", minPrice, maxPrice);
        return dishSearchRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Dish> searchByNameOrDescription(String keyword) {
        log.info("Searching dishes by name or description: {}", keyword);
        return dishSearchRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword);
    }

    public List<Dish> searchAdvanced(String keyword) {
        log.info("Advanced search for: {}", keyword);

        // Raw JSON query với multi-match và fuzziness
        String rawQuery = """
            {
              "multi_match": {
                "query": "%s",
                "fields": ["name^3", "description^2", "category"],
                "fuzziness": "AUTO",
                "type": "best_fields"
              }
            }
        """.formatted(keyword);

        Query query = new StringQuery(rawQuery);
        SearchHits<Dish> hits = elasticsearchTemplate.search(query, Dish.class);

        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

    /**
     * Search with filter by keyword and category
     */
    public List<Dish> searchWithFilters(String keyword, String category,
                                        BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Search with filters - keyword: {}, category: {}, price: {}-{}",
                keyword, category, minPrice, maxPrice);

        String rawQuery = """
            {
              "bool": {
                "must": [
                  {
                    "multi_match": {
                      "query": "%s",
                      "fields": ["name^3", "description^2"],
                      "fuzziness": "AUTO"
                    }
                  }
                ],
                "filter": [
                  %s
                  %s
                ]
              }
            }
        """.formatted(
                keyword,
                category != null ? String.format("{\"term\": {\"category\": \"%s\"}},", category) : "",
                (minPrice != null && maxPrice != null) ?
                        String.format("{\"range\": {\"price\": {\"gte\": %s, \"lte\": %s}}}", minPrice, maxPrice) : ""
        );

        // Clean up empty filters
        rawQuery = rawQuery.replaceAll(",\\s*]", "]").replaceAll("\\[\\s*,", "[");

        Query query = new StringQuery(rawQuery);
        SearchHits<Dish> hits = elasticsearchTemplate.search(query, Dish.class);

        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

    /**
     * Search with auto suggest
     */
    public List<Dish> searchSuggestions(String keyword) {
        log.info("Getting search suggestions for: {}", keyword);

        String rawQuery = """
            {
              "bool": {
                "should": [
                  {
                    "match": {
                      "name": {
                        "query": "%s",
                        "fuzziness": "AUTO"
                      }
                    }
                  },
                  {
                    "prefix": {
                      "name": "%s"
                    }
                  }
                ]
              }
            }
        """.formatted(keyword, keyword);

        Query query = new StringQuery(rawQuery);
        SearchHits<Dish> hits = elasticsearchTemplate.search(query, Dish.class);

        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .limit(10) // Giới hạn 10 gợi ý
                .collect(Collectors.toList());
    }

    public List<Dish> getAllDishes() {
        return (List<Dish>) dishSearchRepository.findAll();
    }

    public void indexDish(DishEvent event) {
        try {
            DishDocument document = DishDocument.builder()
                    .id(event.getDishId().toString())
                    .dishId(event.getDishId())
                    .name(event.getName())
                    .description(event.getDescription())
                    .price(event.getPrice())
                    .isAvailable(event.getIsAvailable())
                    .category(event.getCategory())
                    .imageUrls(event.getImageUrls())
                    .averageRating(event.getAverageRating())
                    .totalReviews(event.getTotalReviews())
                    .orderCount(event.getOrderCount())
                    .createdAt(event.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();

            dishDocumentRepository.save(document);
            log.info("Indexed dish document: {}", event.getDishId());
        } catch (Exception e) {
            log.error("Failed to index dish: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void deleteDish(Long dishId) {
        try {
            dishDocumentRepository.deleteByDishId(dishId);
            log.info("Deleted dish document: {}", dishId);
        } catch (Exception e) {
            log.error("Failed to delete dish: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Page<DishDocument> searchDishes(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return dishDocumentRepository.findAll(pageable);
        }
        return dishDocumentRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                query, query, pageable);
    }

    public Page<DishDocument> findByCategory(String category, Pageable pageable) {
        return dishDocumentRepository.findByCategory(category, pageable);
    }

    public Page<DishDocument> findAvailableDishes(Pageable pageable) {
        return dishDocumentRepository.findByIsAvailable(true, pageable);
    }

    public Page<DishDocument> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return dishDocumentRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    }

    public List<DishDocument> findHighRatedDishes(BigDecimal minRating) {
        return dishDocumentRepository.findByAverageRatingGreaterThanEqual(minRating);
    }

    public Optional<DishDocument> findById(Long dishId) {
        return dishDocumentRepository.findByDishId(dishId);
    }
}