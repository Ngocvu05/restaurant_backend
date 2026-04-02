package com.management.restaurant.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private String bookingId;
    private String paymentId;
    private java.math.BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime processedAt;
}
