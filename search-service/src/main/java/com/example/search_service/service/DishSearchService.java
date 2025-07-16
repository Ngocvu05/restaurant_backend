package com.example.search_service.service;

import com.example.search_service.model.Dish;
import com.example.search_service.repository.DishSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishSearchService {

    private final DishSearchRepository dishSearchRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    public DishSearchService(DishSearchRepository dishSearchRepository,
                             ElasticsearchTemplate elasticsearchTemplate) {
        this.dishSearchRepository = dishSearchRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    /**
     * Tìm kiếm đơn giản theo tên
     */
    public List<Dish> searchByName(String keyword) {
        log.info("Searching dishes by name: {}", keyword);
        return dishSearchRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * Tìm kiếm theo category
     */
    public List<Dish> searchByCategory(String category) {
        log.info("Searching dishes by category: {}", category);
        return dishSearchRepository.findByCategory(category);
    }

    /**
     * Tìm kiếm theo khoảng giá
     */
    public List<Dish> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Searching dishes by price range: {} - {}", minPrice, maxPrice);
        return dishSearchRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * Tìm kiếm trong tên hoặc description
     */
    public List<Dish> searchByNameOrDescription(String keyword) {
        log.info("Searching dishes by name or description: {}", keyword);
        return dishSearchRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword);
    }

    /**
     * Tìm kiếm nâng cao với multi-match và fuzziness
     */
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
     * Tìm kiếm phức tạp với filter theo category và price range
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
     * Tìm kiếm với auto-suggest (gợi ý)
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

    /**
     * Lấy tất cả dishes
     */
    public List<Dish> getAllDishes() {
        return (List<Dish>) dishSearchRepository.findAll();
    }
}