package com.management.restaurant.helper;

import com.management.restaurant.model.event.BookingCancelledEvent;
import com.management.restaurant.model.event.BookingConfirmedEvent;
import com.management.restaurant.model.event.BookingCreatedEvent;
import com.management.restaurant.model.event.DomainEvent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookingAggregate {
    private String id;
    private Long tableId;
    private Long userId;
    private LocalDateTime bookingTime;
    private int numberOfGuests;
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED
    private java.math.BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<DomainEvent> uncommittedEvents = new ArrayList<>();

    /**
     * Create booking (generates event)
     */
    public static BookingAggregate create(Long tableId, Long userId,
                                          LocalDateTime bookingTime, int numberOfGuests) {
        String bookingId = UUID.randomUUID().toString();

        BookingCreatedEvent event = new BookingCreatedEvent(
                bookingId, tableId, userId, bookingTime, numberOfGuests, LocalDateTime.now()
        );

        BookingAggregate aggregate = BookingAggregate.builder()
                .id(bookingId)
                .tableId(tableId)
                .userId(userId)
                .bookingTime(bookingTime)
                .numberOfGuests(numberOfGuests)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        // Record event
        DomainEvent domainEvent = DomainEvent.builder()
                .aggregateId(bookingId)
                .aggregateType("Booking")
                .eventType("BookingCreated")
                .timestamp(LocalDateTime.now())
                .build();

        aggregate.getUncommittedEvents().add(domainEvent);

        return aggregate;
    }

    /**
     * Confirm booking
     */
    public void confirm(String confirmedBy) {
        if (!"PENDING".equals(this.status)) {
            throw new IllegalStateException("Can only confirm pending bookings");
        }

        this.status = "CONFIRMED";
        this.updatedAt = LocalDateTime.now();

        BookingConfirmedEvent event = new BookingConfirmedEvent(
                this.id, confirmedBy, LocalDateTime.now()
        );

        // Record event
        DomainEvent domainEvent = DomainEvent.builder()
                .aggregateId(this.id)
                .aggregateType("Booking")
                .eventType("BookingConfirmed")
                .timestamp(LocalDateTime.now())
                .build();

        this.uncommittedEvents.add(domainEvent);
    }

    /**
     * Cancel booking
     */
    public void cancel(String reason, String cancelledBy) {
        if ("CANCELLED".equals(this.status) || "COMPLETED".equals(this.status)) {
            throw new IllegalStateException("Cannot cancel " + this.status + " booking");
        }

        this.status = "CANCELLED";
        this.updatedAt = LocalDateTime.now();

        BookingCancelledEvent event = new BookingCancelledEvent(
                this.id, reason, cancelledBy, LocalDateTime.now()
        );

        DomainEvent domainEvent = DomainEvent.builder()
                .aggregateId(this.id)
                .aggregateType("Booking")
                .eventType("BookingCancelled")
                .timestamp(LocalDateTime.now())
                .build();

        this.uncommittedEvents.add(domainEvent);
    }

    /**
     * Get uncommitted events and clear
     */
    public List<DomainEvent> getAndClearUncommittedEvents() {
        List<DomainEvent> events = new ArrayList<>(this.uncommittedEvents);
        this.uncommittedEvents.clear();
        return events;
    }
}