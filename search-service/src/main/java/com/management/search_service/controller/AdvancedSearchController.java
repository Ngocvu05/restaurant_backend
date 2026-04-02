package com.management.search_service.controller;

import com.management.search_service.document.DishDocument;
import com.management.search_service.dto.DishSearchResultDto;
import com.management.search_service.dto.SearchAggregationsDto;
import com.management.search_service.dto.SearchAnalyticsDto;
import com.management.search_service.service.AdvancedDishSearchService;
import com.management.search_service.service.SearchAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search/advanced")
@RequiredArgsConstructor
@Slf4j
public class AdvancedSearchController {
    private final AdvancedDishSearchService advancedSearchService;
    private final SearchAnalyticsService analyticsService;

    /**
     * 1. Fuzzy Search - Tìm kiếm chịu lỗi chính tả
     * GET /api/v1/search/advanced/fuzzy?q=pho&maxEdits=2
     */
    @GetMapping("/fuzzy")
    public ResponseEntity<List<DishSearchResultDto>> fuzzySearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "AUTO") String maxEdits,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        log.info("Fuzzy search: query={}, maxEdits={}", q, maxEdits);

        int edits = maxEdits.equals("AUTO") ? 2 : Integer.parseInt(maxEdits);
        List<DishSearchResultDto> results = advancedSearchService.fuzzySearch(q, edits);

        // Track search
        analyticsService.trackSearch(q, (long) results.size(), userId, sessionId);

        return ResponseEntity.ok(results);
    }

    /**
     * 2. Autocomplete - Gợi ý tự động
     * GET /api/v1/search/advanced/autocomplete?prefix=ph
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(@RequestParam String prefix) {
        log.info("Autocomplete: prefix={}", prefix);
        List<String> suggestions = advancedSearchService.autocomplete(prefix);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * 3. Search with Aggregations - Tìm kiếm + thống kê
     * GET /api/v1/search/advanced/aggregations?q=chicken&category=Main Course
     */
    @GetMapping("/aggregations")
    public ResponseEntity<SearchAggregationsDto> searchWithAggregations(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        log.info("Aggregations search: q={}, category={}, price={}-{}",
                q, category, minPrice, maxPrice);

        SearchAggregationsDto results = advancedSearchService.searchWithAggregations(
                q, category, minPrice, maxPrice);

        if (q != null && !q.trim().isEmpty()) {
            analyticsService.trackSearch(q, results.getTotalHits(), userId, sessionId);
        }

        return ResponseEntity.ok(results);
    }

    /**
     * 4. Custom Scoring Search - Tìm kiếm với scoring tùy chỉnh
     * GET /api/v1/search/advanced/ranked?q=noodles
     */
    @GetMapping("/ranked")
    public ResponseEntity<List<DishSearchResultDto>> searchWithCustomScoring(
            @RequestParam String q,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        log.info("Custom scoring search: query={}", q);

        List<DishSearchResultDto> results = advancedSearchService.searchWithCustomScoring(q);

        analyticsService.trackSearch(q, (long) results.size(), userId, sessionId);

        return ResponseEntity.ok(results);
    }

    /**
     * 5. More Like This - Tìm món ăn tương tự
     * GET /api/v1/search/advanced/similar/123?size=5
     */
    @GetMapping("/similar/{dishId}")
    public ResponseEntity<List<DishDocument>> findSimilarDishes(
            @PathVariable Long dishId,
            @RequestParam(defaultValue = "5") int size) {

        log.info("Finding similar dishes for dishId={}, size={}", dishId, size);

        List<DishDocument> similar = advancedSearchService.findSimilarDishes(dishId, size);

        return ResponseEntity.ok(similar);
    }

    /**
     * 6. Track Click Event
     * POST /api/v1/search/advanced/track-click
     */
    @PostMapping("/track-click")
    public ResponseEntity<Void> trackClick(
            @RequestParam String keyword,
            @RequestParam Long dishId,
            @RequestParam int position,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        analyticsService.trackClick(keyword, dishId, position, sessionId);

        return ResponseEntity.ok().build();
    }

    /**
     * 7. Get Top Searches - Top từ khóa được tìm nhiều nhất
     * GET /api/v1/search/advanced/analytics/top?limit=10&days=7
     */
    @GetMapping("/analytics/top")
    public ResponseEntity<List<SearchAnalyticsDto>> getTopSearches(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "7") int days) {

        log.info("Getting top {} searches from last {} days", limit, days);

        List<SearchAnalyticsDto> topSearches = analyticsService.getTopSearches(limit, days);

        return ResponseEntity.ok(topSearches);
    }

    /**
     * 8. Get Zero Result Searches - Từ khóa không có kết quả
     * GET /api/v1/search/advanced/analytics/zero-results?limit=20&days=7
     */
    @GetMapping("/analytics/zero-results")
    public ResponseEntity<List<SearchAnalyticsDto>> getZeroResultSearches(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "7") int days) {

        log.info("Getting zero-result searches");

        List<SearchAnalyticsDto> zeroResults = analyticsService.getZeroResultSearches(limit, days);

        return ResponseEntity.ok(zeroResults);
    }

    /**
     * 9. Get Trending Searches - Từ khóa đang trending
     * GET /api/v1/search/advanced/analytics/trending?limit=10
     */
    @GetMapping("/analytics/trending")
    public ResponseEntity<List<SearchAnalyticsDto>> getTrendingSearches(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Getting trending searches");

        List<SearchAnalyticsDto> trending = analyticsService.getTrendingSearches(limit);

        return ResponseEntity.ok(trending);
    }

    /**
     * 10. Get Click-Through Rates
     * GET /api/v1/search/advanced/analytics/ctr?days=7
     */
    @GetMapping("/analytics/ctr")
    public ResponseEntity<Map<String, Double>> getClickThroughRates(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Getting CTR for last {} days", days);

        Map<String, Double> ctr = analyticsService.getClickThroughRates(days);

        return ResponseEntity.ok(ctr);
    }
}