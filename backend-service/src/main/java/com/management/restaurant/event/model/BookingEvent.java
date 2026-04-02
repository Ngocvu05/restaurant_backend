package com.management.restaurant.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {
    // Event metadata
    @Builder.Default
    private String eventId = "evt_" + UUID.randomUUID().toString();
    private String eventType; // booking.created, booking.status.changed, etc.
    @Builder.Default
    private String eventVersion = "v1";
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    @Builder.Default
    private String source = "user-service";

    // Booking data
    private Long bookingId;
    private Long userId;
    private String username;
    private Long tableId;
    private String tableName;
    private LocalDateTime bookingTime;
    private Integer numberOfGuests;
    private String status;
    private String note;
    private BigDecimal totalAmount;

    // For status change events
    private String oldStatus;
    private String newStatus;

    // Version for ordering
    private Long version;
    private String triggeredBy;
}
