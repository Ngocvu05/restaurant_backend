package com.management.restaurant.analytics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Analytics Entity - User Activity Tracking
 * Stored in analytics database
 */
@Entity
@Table(name = "user_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "action_timestamp", nullable = false)
    private LocalDateTime actionTimestamp;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @PrePersist
    protected void onCreate() {
        if (actionTimestamp == null) {
            actionTimestamp = LocalDateTime.now();
        }
    }
}
