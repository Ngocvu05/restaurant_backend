package com.management.restaurant.analytics.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    @Id
    private String id;

    @Indexed(unique = true)
    private String sessionId;

    @Indexed
    private Long userId;

    private String deviceInfo;

    private String ipAddress;

    private String userAgent;

    @Indexed
    private LocalDateTime loginTime;

    @Indexed
    private LocalDateTime lastActivity;

    private LocalDateTime logoutTime;

    @Indexed
    private boolean isActive;
}