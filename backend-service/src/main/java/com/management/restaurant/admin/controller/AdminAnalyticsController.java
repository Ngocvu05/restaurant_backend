package com.management.restaurant.admin.controller;

import com.management.restaurant.analytics.model.ActivityLog;
import com.management.restaurant.analytics.service.MongoActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
@Slf4j
public class AdminAnalyticsController {
    private final MongoActivityLogService activityLogService;
    private final MongoTemplate mongoTemplate;

    @GetMapping("/user/{userId}/activity")
    public ResponseEntity<List<ActivityLog>> getUserActivity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {

        List<ActivityLog> activities = activityLogService
                .getUserActivityHistory(userId, days);

        return ResponseEntity.ok(activities);
    }

    @GetMapping("/popular-activities")
    public ResponseEntity<Map<String, Long>> getPopularActivities(
            @RequestParam(defaultValue = "7") int days) {

        // Aggregate by activity type
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("timestamp")
                        .gte(LocalDateTime.now().minusDays(days))),
                Aggregation.group("activityType").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "count")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(
                aggregation, "activity_logs", Map.class
        );

        Map<String, Long> activityCounts = results.getMappedResults().stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("_id"),
                        m -> ((Number) m.get("count")).longValue()
                ));

        return ResponseEntity.ok(activityCounts);
    }

    @GetMapping("/booking-trends")
    public ResponseEntity<Map<String, Object>> getBookingTrends(
            @RequestParam(defaultValue = "30") int days) {

        Query query = new Query();
        query.addCriteria(Criteria.where("activityType").is("BOOKING_CREATED"));
        query.addCriteria(Criteria.where("timestamp")
                .gte(LocalDateTime.now().minusDays(days)));

        List<ActivityLog> bookingActivities = mongoTemplate.find(query, ActivityLog.class);

        // Calculate statistics
        long totalBookings = bookingActivities.size();
        double avgGuestsPerBooking = bookingActivities.stream()
                .mapToInt(log -> (int) log.getMetadata().getOrDefault("numberOfGuests", 0))
                .average()
                .orElse(0.0);

        BigDecimal totalRevenue = bookingActivities.stream()
                .map(log -> (BigDecimal) log.getMetadata().get("totalAmount"))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(Map.of(
                "totalBookings", totalBookings,
                "avgGuestsPerBooking", avgGuestsPerBooking,
                "totalRevenue", totalRevenue,
                "period", days + " days"
        ));
    }
}