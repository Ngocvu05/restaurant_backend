package com.management.restaurant.event.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OutboxMetrics {
    private long pendingCount;
    private long publishedCount;
    private long failedCount;
    private int recentFailures;
}
