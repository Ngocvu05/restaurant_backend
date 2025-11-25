package com.management.restaurant.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {
    private String bookingId;
    private Long tableId;
    private Long userId;
    private LocalDateTime bookingTime;
    private int numberOfGuests;
    private LocalDateTime createdAt;
}
