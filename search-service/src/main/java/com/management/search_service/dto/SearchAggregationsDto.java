package com.management.search_service.dto;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.management.search_service.document.DishDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchAggregationsDto {
    private List<DishDocument> dishes;
    private Long totalHits;
    private Map<String, Long> categoryCounts;
    private Map<String, Long> priceRanges;
    private Double averageRating;
    private Map<String, Long> ratingDistribution;

    public static SearchAggregationsDto fromResponse(SearchResponse<DishDocument> response) {
        SearchAggregationsDtoBuilder builder = SearchAggregationsDto.builder();

        // Dishes
        builder.dishes(response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList()));

        builder.totalHits(response.hits().total().value());

        // Category aggregation
        if (response.aggregations().containsKey("categories")) {
            Map<String, Long> categories = response.aggregations()
                    .get("categories")
                    .sterms()
                    .buckets()
                    .array()
                    .stream()
                    .collect(Collectors.toMap(
                            bucket -> bucket.key().stringValue(),
                            bucket -> bucket.docCount()
                    ));
            builder.categoryCounts(categories);
        }

        // Price ranges aggregation
        if (response.aggregations().containsKey("price_ranges")) {
            Map<String, Long> priceRanges = response.aggregations()
                    .get("price_ranges")
                    .range()
                    .buckets()
                    .array()
                    .stream()
                    .collect(Collectors.toMap(
                            bucket -> bucket.key(),
                            bucket -> bucket.docCount()
                    ));
            builder.priceRanges(priceRanges);
        }

        // Average rating
        if (response.aggregations().containsKey("avg_rating")) {
            Double avgRating = response.aggregations()
                    .get("avg_rating")
                    .avg()
                    .value();
            builder.averageRating(avgRating);
        }

        // Rating distribution
        if (response.aggregations().containsKey("rating_distribution")) {
            Map<String, Long> ratingDist = response.aggregations()
                    .get("rating_distribution")
                    .histogram()
                    .buckets()
                    .array()
                    .stream()
                    .collect(Collectors.toMap(
                            bucket -> String.valueOf(bucket.key()),
                            bucket -> bucket.docCount()
                    ));
            builder.ratingDistribution(ratingDist);
        }

        return builder.build();
    }
}
