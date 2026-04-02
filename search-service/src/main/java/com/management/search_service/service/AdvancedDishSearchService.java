package com.management.search_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.management.search_service.document.DishDocument;
import com.management.search_service.dto.DishSearchResultDto;
import com.management.search_service.dto.SearchAggregationsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedDishSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final CustomCacheService customCacheService;

    /**
     * 1. FUZZY SEARCH - Cache với maxEdits parameter
     * High traffic endpoint - use L1 + L2 cache
     */
    @Cacheable(value = "search",
            key = "'fuzzy:' + #keyword + ':' + #maxEdits",
            cacheManager = "redisCacheManager")
    public List<DishSearchResultDto> fuzzySearch(String keyword, int maxEdits) {
        log.info("Fuzzy search: keyword={}, maxEdits={} [CACHE MISS]", keyword, maxEdits);
        try {
            SearchResponse<DishDocument> response = elasticsearchClient.search(s -> s
                            .index("dishes")
                            .query(q -> q
                                    .multiMatch(m -> m
                                            .query(keyword)
                                            .fields("name^3", "description^2", "category")
                                            .fuzziness(String.valueOf(maxEdits)) // 0, 1, 2 hoặc "AUTO"
                                            .prefixLength(2) // Số ký tự đầu phải khớp chính xác
                                            .maxExpansions(50) // Giới hạn số biến thể
                                    )
                            )
                            .highlight(h -> h
                                    .fields("name", f -> f.preTags("<em>").postTags("</em>"))
                                    .fields("description", f -> f.preTags("<em>").postTags("</em>"))
                            )
                            .size(20),
                    DishDocument.class
            );

            return convertToSearchResults(response);
        } catch (Exception e) {
            log.error("Fuzzy search failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 2. AUTOCOMPLETE - L1 cache preferred (fast, frequent access)
     * Short TTL: 5 minutes
     */
    @Cacheable(value = "suggestions",
            key = "'autocomplete:' + #prefix",
            cacheManager = "caffeineCacheManager")
    public List<String> autocomplete(String prefix) {
        log.info("Autocomplete for prefix: {} [CACHE MISS]", prefix);
        try {
            SearchResponse<DishDocument> response = elasticsearchClient.search(s -> s
                            .index("dishes")
                            .query(q -> q
                                    .bool(b -> b
                                            .should(sh -> sh
                                                    .prefix(p -> p
                                                            .field("name")
                                                            .value(prefix)
                                                            .boost(2.0f)
                                                    )
                                            )
                                            .should(sh -> sh
                                                    .matchPhrasePrefix(mp -> mp
                                                            .field("name")
                                                            .query(prefix)
                                                    )
                                            )
                                    )
                            )
                            .size(10)
                            .source(src -> src.filter(f -> f.includes("name"))),
                    DishDocument.class
            );

            return response.hits().hits().stream()
                    .map(hit -> {
                        assert hit.source() != null;
                        return hit.source().getName();
                    })
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Autocomplete failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 3. AGGREGATIONS - Cache results (complex query, expensive)
     * Use L2 cache with longer TTL
     */
    @Cacheable(value = "search",
            key = "'agg:' + #keyword + ':' + (#category ?: 'null') + ':' + (#minPrice ?: 'null') + ':' + (#maxPrice ?: 'null')",
            cacheManager = "redisCacheManager")
    public SearchAggregationsDto searchWithAggregations(String keyword,
                                                        String category,
                                                        BigDecimal minPrice,
                                                        BigDecimal maxPrice) {
        log.info("Aggregations search: keyword={}, category={}, price={}-{} [CACHE MISS]",
                keyword, category, minPrice, maxPrice);
        try {
            SearchResponse<DishDocument> response = elasticsearchClient.search(s -> {
                // Build query
                Query mainQuery = buildMainQuery(keyword, category, minPrice, maxPrice);

                return s.index("dishes")
                        .query(mainQuery)
                        .aggregations("categories", a -> a
                                .terms(t -> t
                                        .field("category.keyword")
                                        .size(20)
                                )
                        )
                        .aggregations("price_ranges", a -> a
                                .range(r -> r
                                        .field("price")
                                        .ranges(range -> range.to("50000"))
                                        .ranges(range -> range.from("50000").to("100000"))
                                        .ranges(range -> range.from("100000").to("200000"))
                                        .ranges(range -> range.from("200000"))
                                )
                        )
                        .aggregations("avg_rating", a -> a
                                .avg(avg -> avg.field("averageRating"))
                        )
                        .aggregations("rating_distribution", a -> a
                                .histogram(h -> h
                                        .field("averageRating")
                                        .interval(1.0)
                                        .minDocCount(0)
                                )
                        )
                        .size(50);
            }, DishDocument.class);

            return SearchAggregationsDto.fromResponse(response);
        } catch (Exception e) {
            log.error("Aggregations search failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 4. CUSTOM SCORING - Cache with keyword
     * Results may change over time (ratings, orders), so use shorter TTL
     */
    @Cacheable(value = "search",
            key = "'scoring:' + #keyword",
            cacheManager = "redisCacheManager")
    public List<DishSearchResultDto> searchWithCustomScoring(String keyword) {
        log.info("Custom scoring search for: {} [CACHE MISS]", keyword);
        try {
            SearchResponse<DishDocument> response = elasticsearchClient.search(s -> s
                            .index("dishes")
                            .query(q -> q
                                    .functionScore(fs -> fs
                                            .query(Query.of(mq -> mq
                                                    .multiMatch(m -> m
                                                            .query(keyword)
                                                            .fields("name^3", "description^2")
                                                            .fuzziness("AUTO")
                                                    )
                                            ))
                                            // Boost theo rating (món 5* boost x2)
                                            .functions(fn -> fn
                                                    .fieldValueFactor(fvf -> fvf
                                                            .field("averageRating")
                                                            .factor(0.5)
                                                            .modifier(FieldValueFactorModifier.valueOf("log1p"))
                                                            .missing(3.0) // Giá trị mặc định nếu null
                                                    )
                                            )
                                            // Boost theo số đơn hàng
                                            .functions(fn -> fn
                                                    .fieldValueFactor(fvf -> fvf
                                                            .field("orderCount")
                                                            .factor(0.1)
                                                            .modifier(FieldValueFactorModifier.valueOf("log1p"))
                                                    )
                                            )
                                            // Boost món mới (updatedAt gần đây)
                                            .functions(fn -> fn
                                                    .fieldValueFactor(fvf -> fvf
                                                            .field("orderCount")
                                                            .factor(0.1)
                                                            .modifier(FieldValueFactorModifier.Log1p)
                                                    )
                                            )
                                            .scoreMode(FunctionScoreMode.Sum)
                                            .boostMode(FunctionBoostMode.Multiply)
                                    )
                            )
                            .highlight(h -> h
                                    .fields("name", f -> f)
                                    .fields("description", f -> f)
                            )
                            .size(20),
                    DishDocument.class
            );

            return convertToSearchResults(response);
        } catch (Exception e) {
            log.error("Custom scoring search failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 5. MORE LIKE THIS - Cache recommendations
     * Good candidate for caching as results are stable
     */
    @Cacheable(value = "search",
            key = "'similar:' + #dishId + ':' + #size",
            cacheManager = "redisCacheManager")
    public List<DishDocument> findSimilarDishes(Long dishId, int size) {
        try {
            SearchResponse<DishDocument> response = elasticsearchClient.search(s -> s
                            .index("dishes")
                            .query(q -> q
                                    .moreLikeThis(mlt -> mlt
                                            .fields("name", "description", "category")
                                            .like(l -> l
                                                    .document(d -> d
                                                            .index("dishes")
                                                            .id(dishId.toString())
                                                    )
                                            )
                                            .minTermFreq(1)
                                            .minDocFreq(1)
                                            .maxQueryTerms(25)
                                    )
                            )
                            .size(size),
                    DishDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("More like this search failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 6. PERCOLATE - Reverse search (lưu query, match với document mới)
     * Useful cho alerting: "Thông báo khi có món phở mới giá < 50k"
     * Search alerts - Not cached (write operation)
     */
    public void registerSearchAlert(String alertId, String keyword, BigDecimal maxPrice) {
        // Implementation depends on your alerting system
        // This is a placeholder to show the concept
        log.info("Registered search alert: {} for keyword: {}, maxPrice: {}",
                alertId, keyword, maxPrice);
    }

    // Helper methods
    private Query buildMainQuery(String keyword, String category,
                                 BigDecimal minPrice, BigDecimal maxPrice) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        if (keyword != null && !keyword.trim().isEmpty()) {
            boolQuery.must(m -> m
                    .multiMatch(mm -> mm
                            .query(keyword)
                            .fields("name^3", "description^2", "category")
                            .fuzziness("AUTO")
                    )
            );
        }

        if (category != null && !category.trim().isEmpty()) {
            boolQuery.filter(f -> f
                    .term(t -> t
                            .field("category.keyword")
                            .value(FieldValue.of(category))
                    )
            );
        }

        if (minPrice != null || maxPrice != null) {
            boolQuery.filter(f -> f
                    .range(r -> {
                        RangeQuery.Builder rangeBuilder = r.field("price");
                        if (minPrice != null) rangeBuilder.gte(JsonData.fromJson(String.valueOf(minPrice)));
                        if (maxPrice != null) rangeBuilder.lte(JsonData.fromJson(String.valueOf(maxPrice)));
                        return rangeBuilder;
                    })
            );
        }

        // Only show available dishes
        boolQuery.filter(f -> f
                .term(t -> t
                        .field("isAvailable")
                        .value(FieldValue.of(true))
                )
        );

        return Query.of(q -> q.bool(boolQuery.build()));
    }

    private List<DishSearchResultDto> convertToSearchResults(SearchResponse<DishDocument> response) {
        return response.hits().hits().stream()
                .map(hit -> {
                    DishDocument dish = hit.source();
                    Map<String, List<String>> highlights = new HashMap<>();

                    if (hit.highlight() != null) {
                        highlights.putAll(hit.highlight());
                    }

                    return DishSearchResultDto.builder()
                            .dish(dish)
                            .score(hit.score())
                            .highlights(highlights)
                            .build();
                })
                .collect(Collectors.toList());
    }
}