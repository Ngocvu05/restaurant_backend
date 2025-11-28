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

@Document(collection = "booking_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistory {
    @Id
    private String id;

    @Indexed
    private Long bookingId;

    @Indexed
    private Long userId;

    private String statusChange; // PENDING->CONFIRMED, CONFIRMED->COMPLETED

    private Map<String, Object> bookingDetails;

    @Indexed
    private LocalDateTime timestamp;

    private String changedBy; // USER, ADMIN, SYSTEM

    private String note;
}