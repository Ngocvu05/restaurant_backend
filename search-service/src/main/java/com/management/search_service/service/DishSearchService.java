package com.management.search_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.management.search_service.document.DishDocument;
import com.management.search_service.events.implement.DishEvent;
import com.management.search_service.model.Dish;
import com.management.search_service.repository.DishDocumentRepository;
import com.management.search_service.repository.DishSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
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
    private final ElasticsearchClient elasticsearchClient;
    private final CustomCacheService customCacheService;

    /**
     * Cache simple name searches - popular queries
     * TTL: 10 minutes (Caffeine) / 1 hour (Redis)
     */
    @Cacheable(value = "dishes", key = "'search:name:' + #keyword", cacheManager = "redisCacheManager")
    public List<Dish> searchByName(String keyword) {
        log.info("Searching dishes by name: {}", keyword);
        return dishSearchRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * Advanced search - cache with custom key
     */
    @Cacheable(value = "search", key = "'advanced:' + #keyword", cacheManager = "redisCacheManager")
    public List<DishDocument> searchByNameAdvanced(String keyword) {
        log.info("Advanced search for: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        try {
            SearchResponse<DishDocument> response = elasticsearchClient.search(s -> s
                            .index("dishes")
                            .query(q -> q
                                    .multiMatch(m -> m
                                            .query(keyword)
                                            .fields("name^3", "description^2", "category")
                                            .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                                            .fuzziness("AUTO") // Handles typos
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
                                    )
                            )
                            .size(50),
                    DishDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Advanced search failed", e);
            return List.of();
        }
    }

    /**
     * Cache by category - frequently accessed
     */
    @Cacheable(value = "dishes", key = "'category:' + #category", cacheManager = "redisCacheManager")
    public List<Dish> searchByCategory(String category) {
        log.info("Searching dishes by category: {}", category);
        return dishSearchRepository.findByCategory(category);
    }

    /**
     * Price range search - cache with composite key
     */
    @Cacheable(value = "dishes",
            key = "'price:' + #minPrice + ':' + #maxPrice",
            cacheManager = "redisCacheManager")
    public List<Dish> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Searching dishes by price range: {} - {}", minPrice, maxPrice);
        return dishSearchRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * Search by name or description - cache enabled
     */
    @Cacheable(value = "search", key = "'nameOrDesc:' + #keyword", cacheManager = "redisCacheManager")
    public List<Dish> searchByNameOrDescription(String keyword) {
        log.info("Searching dishes by name or description: {}", keyword);
        return dishSearchRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword);
    }

    /**
     * Advanced search with custom cache
     */
    public List<Dish> searchAdvanced(String keyword) {
        // Try custom cache first
        String cacheKey = "advanced:" + keyword;
        List<Dish> cached = customCacheService.get("search", cacheKey, List.class);

        if (cached != null) {
            log.info("Advanced search for: {} [CUSTOM CACHE HIT]", keyword);
            return cached;
        }

        log.info("Advanced search for: {} [CACHE MISS]", keyword);

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

        List<Dish> results = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // Cache results
        customCacheService.put("search", cacheKey, results);

        return results;
    }

    /**
     * Search with filter by keyword and category
     * Search with filters - complex cache key
     */
    @Cacheable(value = "search",
            key = "'filter:' + #keyword + ':' + (#category ?: 'null') + ':' + (#minPrice ?: 'null') + ':' + (#maxPrice ?: 'null')",
            cacheManager = "redisCacheManager")
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
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    /**
     * Search with auto suggest
     * Search suggestions - high-frequency cache (L1 preferred)
     */
    @Cacheable(value = "suggestions", key = "'suggest:' + #keyword", cacheManager = "caffeineCacheManager")
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
                .map(SearchHit::getContent)
                .limit(10) // Giới hạn 10 gợi ý
                .collect(Collectors.toList());
    }

    /**
     * Get all dishes - cache entire list
     */
    @Cacheable(value = "dishes", key = "'all'", cacheManager = "redisCacheManager")
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

    /**
     * Delete dish - evict all caches
     */
    @Caching(evict = {
            @CacheEvict(value = "dishes", allEntries = true, cacheManager = "redisCacheManager"),
            @CacheEvict(value = "dishes", allEntries = true, cacheManager = "caffeineCacheManager"),
            @CacheEvict(value = "search", allEntries = true, cacheManager = "redisCacheManager"),
            @CacheEvict(value = "suggestions", allEntries = true, cacheManager = "caffeineCacheManager")
    })
    public void deleteDish(Long dishId) {
        try {
            dishDocumentRepository.deleteByDishId(dishId);
            log.info("Deleted dish document: {}", dishId);
        } catch (Exception e) {
            log.error("Failed to delete dish: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Pageable queries - cache with page info
    @Cacheable(value = "search",
            key = "'page:' + #query + ':' + #pageable.pageNumber + ':' + #pageable.pageSize",
            cacheManager = "redisCacheManager")
    public Page<DishDocument> searchDishes(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return dishDocumentRepository.findAll(pageable);
        }
        return dishDocumentRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                query, query, pageable);
    }

    @Cacheable(value = "dishes",
            key = "'category:page:' + #category + ':' + #pageable.pageNumber",
            cacheManager = "redisCacheManager")
    public Page<DishDocument> findByCategory(String category, Pageable pageable) {
        return dishDocumentRepository.findByCategory(category, pageable);
    }

    @Cacheable(value = "dishes",
            key = "'available:page:' + #pageable.pageNumber",
            cacheManager = "redisCacheManager")
    public Page<DishDocument> findAvailableDishes(Pageable pageable) {
        return dishDocumentRepository.findByIsAvailable(true, pageable);
    }

    @Cacheable(value = "dishes",
            key = "'price:' + #minPrice + ':' + #maxPrice + ':' + #pageable.pageNumber",
            cacheManager = "redisCacheManager")
    public Page<DishDocument> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return dishDocumentRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    }

    @Cacheable(value = "dishes",
            key = "'rating:' + #minRating",
            cacheManager = "redisCacheManager")
    public List<DishDocument> findHighRatedDishes(BigDecimal minRating) {
        return dishDocumentRepository.findByAverageRatingGreaterThanEqual(minRating);
    }

    @Cacheable(value = "dishes",
            key = "'id:' + #dishId",
            cacheManager = "redisCacheManager")
    public Optional<DishDocument> findById(Long dishId) {
        return dishDocumentRepository.findByDishId(dishId);
    }
}