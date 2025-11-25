package com.management.restaurant.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelledEvent {
    private String bookingId;
    private String reason;
    private String cancelledBy;
    private LocalDateTime cancelledAt;
}
