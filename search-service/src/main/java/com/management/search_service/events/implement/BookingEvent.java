package com.management.search_service.events.implement;

import com.management.search_service.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BookingEvent extends BaseEvent {
    private String eventVersion;

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

    private String oldStatus;
    private String newStatus;

    private Long version;
    private String triggeredBy;
}