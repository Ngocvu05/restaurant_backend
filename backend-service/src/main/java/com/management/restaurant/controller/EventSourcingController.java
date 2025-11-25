package com.management.restaurant.controller;

import com.management.restaurant.dto.CreateBookingRequest;
import com.management.restaurant.helper.BookingAggregate;
import com.management.restaurant.model.event.DomainEvent;
import com.management.restaurant.service.advance.BookingEventSourcingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/event-sourcing")
@RequiredArgsConstructor
public class EventSourcingController {
    private final BookingEventSourcingService eventSourcingService;

    @PostMapping("/v1/bookings")
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingRequest request) {
        String bookingId = eventSourcingService.createBooking(
                request.getTableId(),
                request.getUserId(),
                request.getBookingTime(),
                request.getNumberOfGuests()
        );

        return ResponseEntity.ok(Map.of("bookingId", bookingId));
    }

    @PutMapping("/v1/bookings/{id}/confirm")
    public ResponseEntity<?> confirmBooking(
            @PathVariable String id,
            @RequestParam String confirmedBy) {

        eventSourcingService.confirmBooking(id, confirmedBy);
        return ResponseEntity.ok(Map.of("message", "Booking confirmed"));
    }

    @PutMapping("/v1/bookings/{id}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable String id,
            @RequestParam String reason,
            @RequestParam String cancelledBy) {

        eventSourcingService.cancelBooking(id, reason, cancelledBy);
        return ResponseEntity.ok(Map.of("message", "Booking cancelled"));
    }

    @GetMapping("/v1/bookings/{id}/history")
    public ResponseEntity<?> getHistory(@PathVariable String id) {
        List<DomainEvent> history = eventSourcingService.getBookingHistory(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/v1/bookings/{id}/rebuild")
    public ResponseEntity<?> rebuildState(@PathVariable String id) {
        BookingAggregate aggregate = eventSourcingService.rebuildBookingState(id);
        return ResponseEntity.ok(aggregate);
    }
}