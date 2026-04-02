package com.management.restaurant.service.advance;

import com.management.restaurant.helper.BookingAggregate;
import com.management.restaurant.model.event.BookingCancelledEvent;
import com.management.restaurant.model.event.BookingConfirmedEvent;
import com.management.restaurant.model.event.BookingCreatedEvent;
import com.management.restaurant.model.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventSourcingService {
    private final EventStoreService eventStore;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    /**
     * Create booking with event sourcing
     */
    public String createBooking(Long tableId, Long userId,
                                LocalDateTime bookingTime, int numberOfGuests) {

        BookingAggregate aggregate = BookingAggregate.create(
                tableId, userId, bookingTime, numberOfGuests
        );

        // Save event
        BookingCreatedEvent event = new BookingCreatedEvent(
                aggregate.getId(), tableId, userId, bookingTime, numberOfGuests, LocalDateTime.now()
        );

        eventStore.saveEvent(
                aggregate.getId(),
                "Booking",
                "BookingCreated",
                event,
                userId.toString()
        );

        log.info("Booking created with ID: {}", aggregate.getId());
        return aggregate.getId();
    }

    /**
     * Confirm booking
     */
    public void confirmBooking(String bookingId, String confirmedBy) {
        BookingConfirmedEvent event = new BookingConfirmedEvent(
                bookingId, confirmedBy, LocalDateTime.now()
        );

        eventStore.saveEvent(
                bookingId,
                "Booking",
                "BookingConfirmed",
                event,
                confirmedBy
        );

        log.info("Booking confirmed: {}", bookingId);
    }

    /**
     * Cancel booking
     */
    public void cancelBooking(String bookingId, String reason, String cancelledBy) {
        BookingCancelledEvent event = new BookingCancelledEvent(
                bookingId, reason, cancelledBy, LocalDateTime.now()
        );

        eventStore.saveEvent(
                bookingId,
                "Booking",
                "BookingCancelled",
                event,
                cancelledBy
        );

        log.info("Booking cancelled: {}", bookingId);
    }

    /**
     * Get booking history (audit trail)
     */
    public List<DomainEvent> getBookingHistory(String bookingId) {
        return eventStore.getAuditTrail(bookingId);
    }

    /**
     * Rebuild booking state from events
     */
    public BookingAggregate rebuildBookingState(String bookingId) {
        List<DomainEvent> events = eventStore.getEventsForAggregate(bookingId);

        if (events.isEmpty()) {
            return null;
        }

        BookingAggregate.BookingAggregateBuilder builder = BookingAggregate.builder()
                .id(bookingId);

        // Apply events sequentially
        for (DomainEvent event : events) {
            try {
                switch (event.getEventType()) {
                    case "BookingCreated":
                        BookingCreatedEvent created = objectMapper.readValue(
                                event.getEventData(), BookingCreatedEvent.class
                        );
                        builder.tableId(created.getTableId())
                                .userId(created.getUserId())
                                .bookingTime(created.getBookingTime())
                                .numberOfGuests(created.getNumberOfGuests())
                                .status("PENDING")
                                .createdAt(created.getCreatedAt());
                        break;
                    case "BookingConfirmed":
                        builder.status("CONFIRMED");
                        break;
                    case "BookingCancelled":
                        builder.status("CANCELLED");
                        break;
                }
            } catch (Exception e) {
                log.error("Failed to apply event", e);
            }
        }

        return builder.build();
    }
}