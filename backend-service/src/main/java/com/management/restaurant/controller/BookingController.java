package com.management.restaurant.controller;

import com.management.restaurant.dto.BookingDTO;
import com.management.restaurant.dto.BookingDetailResponseDTO;
import com.management.restaurant.security.UserPrincipal;
import com.management.restaurant.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDTO> create(@RequestBody BookingDTO dto) {
        return ResponseEntity.ok(bookingService.createBooking(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailResponseDTO> getBookingDetails(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingDetail(id));
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAll() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PutMapping("/update{id}")
    public ResponseEntity<BookingDTO> update(@PathVariable Long id, @RequestBody BookingDTO dto) {
        return ResponseEntity.ok(bookingService.updateBooking(id, dto));
    }

    @DeleteMapping("/delete{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<BookingDTO>> getBookingHistory(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookingService.getBookingHistory(principal.getId()));
    }
}