package com.management.restaurant.analytics.service.implement;

import com.management.restaurant.analytics.dto.ActivityCountByDate;
import com.management.restaurant.analytics.help.RequestHelper;
import com.management.restaurant.analytics.model.ActivityLog;
import com.management.restaurant.analytics.repository.ActivityLogMongoRepository;
import com.management.restaurant.analytics.service.MongoActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for handling Activity Logs in MongoDB
 * Storage: User activities, booking history, order tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MongoActivityLogServiceImpl implements MongoActivityLogService {
    private final ActivityLogMongoRepository activityLogRepository;
    private final MongoTemplate mongoTemplate;
    private final RequestHelper requestHelper;

    /**
     * Log activity of user
     */

    @Async
    @Override
    public void logActivity(String userId, String activityType, String description, Map<String, Object> metadata) {
        try{
            ActivityLog activityLog = ActivityLog.builder()
                    .userId(userId)
                    .activityType(activityType)
                    .description(description)
                    .metadata(metadata)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(requestHelper.getCurrentIpAddress())
                    .userAgent(requestHelper.getCurrentUserAgent())
                    .build();

            activityLogRepository.save(activityLog);
            log.info("Logged activity: {} for user: {}", activityType, userId);
        }catch(Exception e){
            log.error("Failed to log activity: {}", e.getMessage());
        }
    }

    /**
     * Log booking activity
     */
    @Override
    public void logBookingActivity(Long bookingId, Long userId, String action, Map<String, Object> bookingDetails) {
        Map<String, Object> metadata = Map.of(
                "bookingId", bookingId,
                "action", action,
                "details", bookingDetails
        );

        logActivity(userId.toString(), "BOOKING",
                "User " + userId + " " + action + " booking " + bookingId,
                metadata);
    }

    /**
     * Log order activity cho Cart/PreOrder
     */
    @Override
    public void logOrderActivity(Long userId, String orderType, List<Map<String, Object>> items) {
        Map<String, Object> metadata = Map.of(
                "orderType", orderType,
                "items", items,
                "itemCount", items.size()
        );

        logActivity(userId.toString(), "ORDER",
                "User placed " + orderType + " order", metadata);
    }

    /**
     * Fetch the user activity logs with a filter
     */
    @Override
    public Page<ActivityLog> getUserActivities(String userId, String activityType, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));

        if (activityType != null) {
            query.addCriteria(Criteria.where("activityType").is(activityType));
        }

        if (startDate != null && endDate != null) {
            query.addCriteria(Criteria.where("timestamp")
                    .gte(startDate).lte(endDate));
        }

        // Add pagination
        query.with(pageable);

        // Execute query
        List<ActivityLog> activities = mongoTemplate.find(query, ActivityLog.class);

        // Count total records
        long total = mongoTemplate.count(
                query.skip(0).limit(0), // Reset skip/limit for count
                ActivityLog.class
        );

        return new PageImpl<>(activities, pageable, total);
    }

    /**
     * Daily activity statistics
     */
    @Override
    public Map<String, Long> getActivityStatsByDate(LocalDateTime startDate, LocalDateTime endDate) {
        //Aggregate query to count activities by day
        return activityLogRepository.countActivitiesByDateRangeGrouped(startDate, endDate)
                .stream()
                .collect(Collectors.toMap(
                        ActivityCountByDate::getDate,
                        ActivityCountByDate::getCount,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Delete old logs (cleanup task)
     */
    @Override
    public long cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return activityLogRepository.deleteByTimestampBefore(cutoffDate);
    }

    /**
     * Get user activity history
     */
    @Override
    public List<ActivityLog> getUserActivityHistory(Long userId, int days) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("timestamp")
                .gte(LocalDateTime.now().minusDays(days)));
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));

        return mongoTemplate.find(query, ActivityLog.class);
    }
}