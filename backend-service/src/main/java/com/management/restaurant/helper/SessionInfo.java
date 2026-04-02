package com.management.restaurant.helper;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class SessionInfo {
    private String deviceId;
    private String deviceName;
    private String ipAddress;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private int useCount;
}
