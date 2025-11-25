package com.management.restaurant.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "events")
public class DomainEvent {
    private String id;
    private String aggregateId;  // ID of the entity
    private String aggregateType; // Type: Booking, Order, etc.
    private String eventType;     // Created, Updated, Cancelled, etc.
    private String eventData;     // JSON payload
    private String userId;        // Who triggered the event
    private LocalDateTime timestamp;
    private Long version;         // Event version for ordering

    @Builder.Default
    private String metadata = "{}"; // Additional context
}
