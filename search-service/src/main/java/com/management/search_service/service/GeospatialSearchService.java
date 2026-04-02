package com.management.search_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.aggregations.MultiBucketBase;
import co.elastic.clients.elasticsearch._types.aggregations.RangeBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.management.search_service.document.RestaurantDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Geospatial Search with Caching
 * Location-based queries cached by coordinates + distance
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class GeospatialSearchService {
    private final ElasticsearchClient elasticsearchClient;

    /**
     * 1. Find nearby restaurants - Cache by location + distance
     * Popular queries cached in L2 (Redis)
     */
    @Cacheable(value = "search",
            key = "'geo:nearby:' + #lat + ':' + #lon + ':' + #distance",
            cacheManager = "redisCacheManager")
    public List<RestaurantDocument> findNearbyRestaurants(double lat, double lon, String distance) {
        log.info("Finding nearby restaurants: lat={}, lon={}, distance={} [CACHE MISS]",
                lat, lon, distance);
        try {
            SearchResponse<RestaurantDocument> response = elasticsearchClient.search(s -> s
                            .index("restaurants")
                            .query(q -> q
                                    .geoDistance(g -> g
                                            .field("location")
                                            .distance(distance) // "5km", "1000m", "2mi"
                                            .location(l -> l
                                                    .latlon(ll -> ll
                                                            .lat(lat)
                                                            .lon(lon)
                                                    )
                                            )
                                    )
                            )
                            .sort(sort -> sort
                                    .geoDistance(gd -> gd
                                            .field("location")
                                            .location(l -> l.latlon(ll -> ll.lat(lat).lon(lon)))
                                            .order(co.elastic.clients.elasticsearch._types.SortOrder.Asc)
                                    )
                            )
                            .size(20),
                    RestaurantDocument.class
            );

            return response.hits().hits().stream()
                    .map(hit -> {
                        RestaurantDocument doc = hit.source();
                        // Add distance to result
                        if (hit.sort() != null && !hit.sort().isEmpty()) {
                            double distanceInMeters = hit.sort().getFirst().doubleValue();
                            assert doc != null;
                            doc.setDistanceFromUser(distanceInMeters);
                        }
                        return doc;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to search nearby restaurants: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 2. Find in bounding box - Cache by box coordinates
     * Good for map view caching
     */
    @Cacheable(value = "search",
            key = "'geo:box:' + #topLeftLat + ':' + #topLeftLon + ':' + #bottomRightLat + ':' + #bottomRightLon",
            cacheManager = "redisCacheManager")
    public List<RestaurantDocument> findInBoundingBox(double topLeftLat, double topLeftLon,
                                                        double bottomRightLat, double bottomRightLon) {
        log.info("Finding in bounding box: topLeft=[{},{}], bottomRight=[{},{}] [CACHE MISS]",
                topLeftLat, topLeftLon, bottomRightLat, bottomRightLon);
        try {
            SearchResponse<RestaurantDocument> response = elasticsearchClient.search(s -> s
                            .index("restaurants")
                            .query(q -> q
                                    .geoBoundingBox(gb -> gb
                                            .field("location")
                                            .boundingBox(bb -> bb
                                                    .tlbr(tlbr -> tlbr
                                                            .topLeft(tl -> tl.latlon(ll -> ll.lat(topLeftLat).lon(topLeftLon)))
                                                            .bottomRight(br -> br.latlon(ll -> ll.lat(bottomRightLat).lon(bottomRightLon)))
                                                    )
                                            )
                                    )
                            )
                            .size(100),
                    RestaurantDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to search in bounding box: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 3. Find in polygon - Cache by polygon hash
     * Note: Create a hash of the polygon points for cache key
     */
    @Cacheable(value = "search",
            key = "'geo:polygon:' + T(java.util.Objects).hash(#polygonPoints)",
            cacheManager = "redisCacheManager")
    public List<RestaurantDocument> findInPolygon(List<double[]> polygonPoints) {
        try {
            SearchResponse<RestaurantDocument> response = elasticsearchClient.search(s -> s
                            .index("restaurants")
                            .query(q -> q
                                    .geoPolygon(gp -> gp
                                            .field("location")
                                            .polygon(p -> {
                                                for (double[] point : polygonPoints) {
                                                    p.points(GeoLocation.of(gl -> gl
                                                            .latlon(ll -> ll.lat(point[0]).lon(point[1]))
                                                    ));
                                                }
                                                return p;
                                            })
                                    )
                            )
                            .size(100),
                    RestaurantDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to search in polygon: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 4. Combined text + geo search - Cache by keyword + location
     * High-value query combining multiple factors
     */
    @Cacheable(value = "search",
            key = "'geo:combined:' + #keyword + ':' + #lat + ':' + #lon + ':' + #distance",
            cacheManager = "redisCacheManager")
    public List<RestaurantDocument> searchDishesNearby(String keyword, double lat, double lon, String distance) {
        log.info("Searching '{}' nearby: lat={}, lon={}, distance={} [CACHE MISS]",
                keyword, lat, lon, distance);
        try {
            SearchResponse<RestaurantDocument> response = elasticsearchClient.search(s -> s
                            .index("restaurants")
                            .query(q -> q
                                    .bool(b -> b
                                            // Text search
                                            .must(m -> m
                                                    .multiMatch(mm -> mm
                                                            .query(keyword)
                                                            .fields("name", "dishes.name^2", "dishes.description")
                                                            .fuzziness("AUTO")
                                                    )
                                            )
                                            // Geo filter
                                            .filter(f -> f
                                                    .geoDistance(gd -> gd
                                                            .field("location")
                                                            .distance(distance)
                                                            .location(l -> l.latlon(ll -> ll.lat(lat).lon(lon)))
                                                    )
                                            )
                                    )
                            )
                            // Score: relevance + distance boost
                            .sort(sort -> sort
                                    .score(sc -> sc.order(co.elastic.clients.elasticsearch._types.SortOrder.Desc))
                            )
                            .sort(sort -> sort
                                    .geoDistance(gd -> gd
                                            .field("location")
                                            .location(l -> l.latlon(ll -> ll.lat(lat).lon(lon)))
                                            .order(co.elastic.clients.elasticsearch._types.SortOrder.Asc)
                                    )
                            )
                            .size(20),
                    RestaurantDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to search dishes nearby: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 5. Aggregation by distance - Cache for analytics
     * Expensive aggregation, good cache candidate
     */
    @Cacheable(value = "analytics",
            key = "'geo:distance-agg:' + #lat + ':' + #lon",
            cacheManager = "redisCacheManager")
    public Map<String, Long> countByDistance(double lat, double lon) {
        log.info("Counting by distance from: lat={}, lon={} [CACHE MISS]", lat, lon);
        try {
            SearchResponse<RestaurantDocument> response = elasticsearchClient.search(s -> s
                            .index("restaurants")
                            .size(0)
                            .aggregations("distance_ranges", a -> a
                                    .geoDistance(gd -> gd
                                            .field("location")
                                            .origin(o -> o.latlon(ll -> ll.lat(lat).lon(lon)))
                                            .ranges(r -> r.to("1km"))
                                            .ranges(r -> r.from("1km").to("3km"))
                                            .ranges(r -> r.from("3km").to("5km"))
                                            .ranges(r -> r.from("5km").to("10km"))
                                            .ranges(r -> r.from("10km"))
                                    )
                            ),
                    RestaurantDocument.class
            );

            return response.aggregations()
                    .get("distance_ranges")
                    .geoDistance()
                    .buckets()
                    .array()
                    .stream()
                    .collect(Collectors.toMap(
                            RangeBucket::key,
                            MultiBucketBase::docCount
                    ));
        } catch (Exception e) {
            log.error("Failed to aggregate by distance: {}", e.getMessage(), e);
            return Map.of();
        }
    }
}