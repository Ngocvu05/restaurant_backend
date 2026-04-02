package com.management.restaurant.analytics.controller;

import com.management.restaurant.analytics.service.BookingTransactionService;
import com.management.restaurant.contains.BookingStatus;
import com.management.restaurant.dto.CreateBookingRequest;
import com.management.restaurant.model.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * CONTROLLER 1: Transaction Isolation Levels Demo
 */
@RestController
@RequestMapping("/api/transactions/isolation")
@RequiredArgsConstructor
@Slf4j
public class IsolationLevelController {

    private final BookingTransactionService bookingTransactionService;

    /**
     * GET /api/transactions/isolation/read-committed/{id}
     * Test READ_COMMITTED isolation level
     */
    @GetMapping("/read-committed/{id}")
    public ResponseEntity<Map<String, Object>> testReadCommitted(@PathVariable Long id) {
        log.info("Testing READ_COMMITTED isolation for booking {}", id);

        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = bookingTransactionService.getConfirmedBooking(id);
            response.put("success", true);
            response.put("booking", booking);
            response.put("isolationLevel", "READ_COMMITTED");
            response.put("description", "Only reads committed data, prevents dirty reads");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * GET /api/transactions/isolation/repeatable-read
     * Test REPEATABLE_READ isolation level - Calculate revenue
     */
    @GetMapping("/repeatable-read")
    public ResponseEntity<Map<String, Object>> testRepeatableRead(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Testing REPEATABLE_READ isolation for revenue calculation");

        Map<String, Object> response = new HashMap<>();
        try {
            BigDecimal revenue = bookingTransactionService.calculateTotalRevenue(startDate, endDate);
            response.put("success", true);
            response.put("totalRevenue", revenue);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("isolationLevel", "REPEATABLE_READ");
            response.put("description", "Ensures consistent reads within transaction");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/isolation/serializable
     * Test SERIALIZABLE isolation level - Create booking
     */
    @PostMapping("/serializable")
    public ResponseEntity<Map<String, Object>> testSerializable(
            @RequestBody CreateBookingRequest request) {

        log.info("Testing SERIALIZABLE isolation for booking creation");

        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = bookingTransactionService.createBookingWithSerializable(
                    request.getTableId(),
                    request.getUserId(),
                    request.getBookingTime(),
                    request.getNumberOfGuests()
            );
            response.put("success", true);
            response.put("booking", booking);
            response.put("isolationLevel", "SERIALIZABLE");
            response.put("description", "Highest isolation, prevents phantom reads");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * PUT /api/transactions/isolation/default/{id}
     * Test DEFAULT isolation level - Update booking status
     */
    @PutMapping("/default/{id}")
    public ResponseEntity<Map<String, Object>> testDefault(
            @PathVariable Long id,
            @RequestParam BookingStatus status) {

        log.info("Testing DEFAULT isolation for booking update");

        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = bookingTransactionService.updateBookingStatus(id, status);
            response.put("success", true);
            response.put("booking", booking);
            response.put("isolationLevel", "DEFAULT (Database default)");
            response.put("description", "Uses database default isolation level");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/isolation/demo/{id}
     * Demonstrate all isolation levels with concurrent threads
     */
    @PostMapping("/demo/{id}")
    public ResponseEntity<Map<String, Object>> demonstrateIsolationLevels(@PathVariable Long id) {
        log.info("Demonstrating isolation levels with concurrent threads");

        Map<String, Object> response = new HashMap<>();
        try {
            bookingTransactionService.demonstrateIsolationLevels(id);
            response.put("success", true);
            response.put("message", "Check server logs to see isolation level demonstration");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}