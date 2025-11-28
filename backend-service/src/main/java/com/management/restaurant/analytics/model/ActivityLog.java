package com.management.restaurant.analytics.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

// ========== ActivityLog Document ==========
@Document(collection = "activity_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String activityType; // BOOKING, ORDER, LOGIN, REVIEW, etc.

    private String description;

    private Map<String, Object> metadata;

    @Indexed
    private LocalDateTime timestamp;

    private String ipAddress;
    private String userAgent;
    private String sessionId;
}
