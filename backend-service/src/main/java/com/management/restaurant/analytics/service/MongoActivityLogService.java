package com.management.restaurant.analytics.service;

import com.management.restaurant.analytics.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MongoActivityLogService {
    /**
     * Log activity of user
     */
    void logActivity(String userId, String activityType,
                            String description, Map<String, Object> metadata);
    /**
     * Log booking activity
     */
    void logBookingActivity(Long bookingId, Long userId, String action,
                                   Map<String, Object> bookingDetails);
    /**
     * Log order activity cho Cart/PreOrder
     */
    void logOrderActivity(Long userId, String orderType,
                                 List<Map<String, Object>> items);
    /**
     * Fetch the user activity logs with a filter
     */
    Page<ActivityLog> getUserActivities(String userId,
                                               String activityType,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               Pageable pageable);
    /**
     * Daily activity statistics
     */
    Map<String, Long> getActivityStatsByDate(LocalDateTime startDate,
                                                    LocalDateTime endDate);
    /**
     * Delete old logs (cleanup task)
     */
    long cleanupOldLogs(int daysToKeep);

    List<ActivityLog> getUserActivityHistory(Long userId, int days);
}