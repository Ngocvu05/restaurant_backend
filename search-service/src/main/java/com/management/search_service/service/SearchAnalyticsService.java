package com.management.search_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import com.management.search_service.dto.SearchAnalyticsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchAnalyticsService {
    private final ElasticsearchClient elasticsearchClient;
    private static final String ANALYTICS_INDEX = "search_analytics";

    /**
     * Track search event - Evict analytics caches
     * Async to not impact search performance
     */
    @Async
    @Caching(evict = {
            @CacheEvict(value = "analytics", key = "'top:*'", allEntries = true, cacheManager = "redisCacheManager"),
            @CacheEvict(value = "analytics", key = "'zero:*'", allEntries = true, cacheManager = "redisCacheManager"),
            @CacheEvict(value = "analytics", key = "'trending'", cacheManager = "redisCacheManager"),
            @CacheEvict(value = "analytics", key = "'ctr:*'", allEntries = true, cacheManager = "redisCacheManager")
    })
    public void trackSearch(String keyword, Long resultCount, String userId, String sessionId) {
        try {
            Map<String, Object> document = new HashMap<>();
            document.put("keyword", keyword.toLowerCase().trim());
            document.put("searchCount", 1);
            document.put("resultCount", resultCount);
            document.put("timestamp", LocalDateTime.now().toString());
            document.put("userId", userId);
            document.put("sessionId", sessionId);

            elasticsearchClient.index(IndexRequest.of(i -> i
                    .index(ANALYTICS_INDEX)
                    .document(document)
            ));

            log.info("Tracked search: keyword={}, results={}", keyword, resultCount);
        } catch (Exception e) {
            log.error("Failed to track search: {}", e.getMessage());
        }
    }

    /**
     * Track click event - Evict CTR cache
     */
    @Async
    @CacheEvict(value = "analytics", key = "'ctr:*'", allEntries = true, cacheManager = "redisCacheManager")
    public void trackClick(String keyword, Long dishId, int position, String sessionId) {
        try {
            Map<String, Object> document = new HashMap<>();
            document.put("keyword", keyword.toLowerCase().trim());
            document.put("clickedDishId", dishId);
            document.put("clickPosition", position);
            document.put("timestamp", LocalDateTime.now().toString());
            document.put("sessionId", sessionId);

            elasticsearchClient.index(IndexRequest.of(i -> i
                    .index(ANALYTICS_INDEX)
                    .document(document)
            ));

            log.info("Tracked click: keyword={}, dishId={}, position={}",
                    keyword, dishId, position);
        } catch (Exception e) {
            log.error("Failed to track click: {}", e.getMessage());
        }
    }

    /**
     * Get top searched keywords - Cache for 1 hour
     * High-value query, expensive aggregation
     */
    @Cacheable(value = "analytics",
            key = "'top:' + #limit + ':' + #daysBack",
            cacheManager = "redisCacheManager")
    public List<SearchAnalyticsDto> getTopSearches(int limit, int daysBack) {
        try {
            String timeFilter = "now-" + daysBack + "d/d";

            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                            .index(ANALYTICS_INDEX)
                            .size(0)
                            .query(q -> q
                                    .bool(b -> b
                                            .must(m -> m
                                                    .range(r -> r
                                                            .field("timestamp")
                                                            .gte(JsonData.of(timeFilter))
                                                    )
                                            )
                                            .must(m -> m
                                                    .exists(e -> e.field("resultCount"))
                                            )
                                    )
                            )
                            .aggregations("top_keywords", a -> a
                                    .terms(t -> t
                                            .field("keyword")
                                            .size(limit)
                                    )
                                    .aggregations("total_results", sub -> sub
                                            .sum(sum -> sum.field("resultCount"))
                                    )
                                    .aggregations("latest_search", sub -> sub
                                            .max(max -> max.field("timestamp"))
                                    )
                            ),
                    Map.class
            );

            return response.aggregations()
                    .get("top_keywords")
                    .sterms()
                    .buckets()
                    .array()
                    .stream()
                    .map(bucket -> {
                        Long totalResults = (long) bucket.aggregations()
                                .get("total_results")
                                .sum()
                                .value();

                        String latestSearchStr = bucket.aggregations()
                                .get("latest_search")
                                .max()
                                .valueAsString();

                        return SearchAnalyticsDto.builder()
                                .keyword(bucket.key().stringValue())
                                .searchCount(bucket.docCount())
                                .resultCount(totalResults)
                                .lastSearched(latestSearchStr != null && !latestSearchStr.isEmpty() ?
                                        LocalDateTime.parse(latestSearchStr) : null)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get top searches: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get zero-result searches - Cache for 1 hour
     * Important for SEO and content strategy
     */
    @Cacheable(value = "analytics",
            key = "'zero:' + #limit + ':' + #daysBack",
            cacheManager = "redisCacheManager")
    public List<SearchAnalyticsDto> getZeroResultSearches(int limit, int daysBack) {
        try {
            String timeFilter = "now-" + daysBack + "d/d";

            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                            .index(ANALYTICS_INDEX)
                            .size(0)
                            .query(q -> q
                                    .bool(b -> b
                                            .must(m -> m
                                                    .range(r -> r
                                                            .field("timestamp")
                                                            .gte(JsonData.of(timeFilter))
                                                    )
                                            )
                                            .must(m -> m
                                                    .term(t -> t
                                                            .field("resultCount")
                                                            .value(0)
                                                    )
                                            )
                                    )
                            )
                            .aggregations("zero_result_keywords", a -> a
                                    .terms(t -> t
                                            .field("keyword")
                                            .size(limit)
                                            .order(List.of(NamedValue.of("_count", SortOrder.Desc)))
                                    )
                                    .aggregations("latest_search", sub -> sub
                                            .max(max -> max.field("timestamp"))
                                    )
                            ),
                    Map.class
            );

            return response.aggregations()
                    .get("zero_result_keywords")
                    .sterms()
                    .buckets()
                    .array()
                    .stream()
                    .map(bucket -> {
                        String latestSearchStr = bucket.aggregations()
                                .get("latest_search")
                                .max()
                                .valueAsString();

                        return SearchAnalyticsDto.builder()
                                .keyword(bucket.key().stringValue())
                                .searchCount(bucket.docCount())
                                .resultCount(0L)
                                .lastSearched(latestSearchStr != null && !latestSearchStr.isEmpty() ?
                                        LocalDateTime.parse(latestSearchStr) : null)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get zero-result searches: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get trending searches - Cache for 30 minutes
     * Complex calculation, good cache candidate
     */
    @Cacheable(value = "analytics",
            key = "'trending:' + #limit",
            cacheManager = "redisCacheManager")
    public List<SearchAnalyticsDto> getTrendingSearches(int limit) {
        try {
            // Compare last 7 days vs previous 7 days
            SearchResponse<Map> recentResponse = elasticsearchClient.search(s -> s
                            .index(ANALYTICS_INDEX)
                            .size(0)
                            .query(q -> q
                                    .range(r -> r
                                            .field("timestamp")
                                            .gte(JsonData.of("now-7d/d"))
                                    )
                            )
                            .aggregations("keywords", a -> a
                                    .terms(t -> t
                                            .field("keyword")
                                            .size(100)
                                    )
                            ),
                    Map.class
            );

            SearchResponse<Map> previousResponse = elasticsearchClient.search(s -> s
                            .index(ANALYTICS_INDEX)
                            .size(0)
                            .query(q -> q
                                    .range(r -> r
                                            .field("timestamp")
                                            .gte(JsonData.of("now-14d/d"))
                                            .lt(JsonData.of("now-7d/d"))
                                    )
                            )
                            .aggregations("keywords", a -> a
                                    .terms(t -> t
                                            .field("keyword")
                                            .size(100)
                                    )
                            ),
                    Map.class
            );

            // Calculate growth rate
            Map<String, Long> recentCounts = extractKeywordCounts(recentResponse);
            Map<String, Long> previousCounts = extractKeywordCounts(previousResponse);

            return recentCounts.entrySet().stream()
                    .map(entry -> {
                        String keyword = entry.getKey();
                        Long recentCount = entry.getValue();
                        Long previousCount = previousCounts.getOrDefault(keyword, 0L);

                        double growthRate = previousCount > 0
                                ? ((double) (recentCount - previousCount) / previousCount) * 100
                                : 100.0; // New keyword = 100% growth

                        return SearchAnalyticsDto.builder()
                                .keyword(keyword)
                                .searchCount(recentCount)
                                .averageClickPosition(growthRate) // Reuse field for growth rate
                                .build();
                    })
                    .filter(dto -> dto.getAverageClickPosition() > 0) // Only growing trends
                    .sorted(Comparator.comparing(SearchAnalyticsDto::getAverageClickPosition).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get trending searches: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get click-through rates - Cache for 30 minutes
     * Expensive dual query
     */
    @Cacheable(value = "analytics",
            key = "'ctr:' + #daysBack",
            cacheManager = "redisCacheManager")
    public Map<String, Double> getClickThroughRates(int daysBack) {
        try {
            String timeFilter = "now-" + daysBack + "d/d";

            // Get total searches per keyword
            SearchResponse<Map> searchResponse = elasticsearchClient.search(s -> s
                            .index(ANALYTICS_INDEX)
                            .size(0)
                            .query(q -> q
                                    .bool(b -> b
                                            .must(m -> m.range(r -> r
                                                    .field("timestamp")
                                                    .gte(JsonData.of(timeFilter))
                                            ))
                                            .must(m -> m.exists(e -> e.field("resultCount")))
                                    )
                            )
                            .aggregations("keywords", a -> a
                                    .terms(t -> t.field("keyword").size(100))
                            ),
                    Map.class
            );

            // Get clicks per keyword
            SearchResponse<Map> clickResponse = elasticsearchClient.search(s -> s
                            .index(ANALYTICS_INDEX)
                            .size(0)
                            .query(q -> q
                                    .bool(b -> b
                                            .must(m -> m.range(r -> r
                                                    .field("timestamp")
                                                    .gte(JsonData.of(timeFilter))
                                            ))
                                            .must(m -> m.exists(e -> e.field("clickedDishId")))
                                    )
                            )
                            .aggregations("keywords", a -> a
                                    .terms(t -> t.field("keyword").size(100))
                            ),
                    Map.class
            );

            Map<String, Long> searchCounts = extractKeywordCounts(searchResponse);
            Map<String, Long> clickCounts = extractKeywordCounts(clickResponse);

            Map<String, Double> ctrMap = new HashMap<>();
            searchCounts.forEach((keyword, searches) -> {
                Long clicks = clickCounts.getOrDefault(keyword, 0L);
                double ctr = searches > 0 ? (double) clicks / searches * 100 : 0.0;
                ctrMap.put(keyword, ctr);
            });

            return ctrMap;
        } catch (Exception e) {
            log.error("Failed to calculate CTR: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    // Helper method
    private Map<String, Long> extractKeywordCounts(SearchResponse<Map> response) {
        return response.aggregations()
                .get("keywords")
                .sterms()
                .buckets()
                .array()
                .stream()
                .collect(Collectors.toMap(
                        bucket -> bucket.key().stringValue(),
                        StringTermsBucket::docCount
                ));
    }
}