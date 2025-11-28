package com.management.restaurant.analytics.repository;

import com.management.restaurant.analytics.dto.ActivityCountByDate;
import com.management.restaurant.analytics.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogMongoRepository extends MongoRepository<ActivityLog, String> {
    List<ActivityLog> findByUserId(String userId);

    List<ActivityLog> findByUserIdAndActivityType(String userId, String activityType);

    Page<ActivityLog> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    List<ActivityLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    long deleteByTimestampBefore(LocalDateTime cutoffDate);

    @Query("{ 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<ActivityLog> findActivitiesInRange(LocalDateTime start, LocalDateTime end);

    // MongoDB Aggregation để group by date
    @Aggregation(pipeline = {
            "{ $match: { timestamp: { $gte: ?0, $lte: ?1 } } }",
            "{ $project: { date: { $dateToString: { format: '%Y-%m-%d', date: '$timestamp' } } } }",
            "{ $group: { _id: '$date', count: { $sum: 1 } } }",
            "{ $project: { _id: 0, date: '$_id', count: '$count' } }",
            "{ $sort: { date: 1 } }"
    })
    List<ActivityCountByDate> countActivitiesByDateRangeGrouped(LocalDateTime start, LocalDateTime end);
}
