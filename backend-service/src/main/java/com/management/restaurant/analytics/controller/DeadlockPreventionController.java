package com.management.restaurant.analytics.controller;

import com.management.restaurant.analytics.service.DeadlockPreventionService;
import com.management.restaurant.dto.CreateBookingRequest;
import com.management.restaurant.model.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CONTROLLER 4: Deadlock Prevention Demo
 */
@RestController
@RequestMapping("/api/transactions/deadlock")
@RequiredArgsConstructor
@Slf4j
public class DeadlockPreventionController {
    private final DeadlockPreventionService deadlockService;

    /**
     * POST /api/transactions/deadlock/lock-ordering
     * Test lock ordering strategy
     */
    @PostMapping("/lock-ordering")
    public ResponseEntity<Map<String, Object>> testLockOrdering(
            @RequestBody List<Long> tableIds) {

        Map<String, Object> response = new HashMap<>();
        try {
            deadlockService.bookMultipleTablesWithOrdering(tableIds);
            response.put("success", true);
            response.put("message", "Tables booked successfully with lock ordering");
            response.put("strategy", "Lock Ordering");
            response.put("tableIds", tableIds);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/deadlock/retry
     * Test retry strategy
     */
    @PostMapping("/retry")
    public ResponseEntity<Map<String, Object>> testRetryStrategy(
            @RequestBody CreateBookingRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = deadlockService.createBookingWithRetry(
                    request.getTableId(),
                    request.getUserId(),
                    request.getBookingTime(),
                    request.getNumberOfGuests()
            );
            response.put("success", true);
            response.put("booking", booking);
            response.put("strategy", "Retry with Exponential Backoff");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/deadlock/short-transaction/{id}
     * Test short transaction strategy
     */
    @PostMapping("/short-transaction/{id}")
    public ResponseEntity<Map<String, Object>> testShortTransaction(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = deadlockService.createBookingShortTransaction(id);
            response.put("success", true);
            response.put("booking", booking);
            response.put("strategy", "Short Transactions");
            response.put("description", "Split into multiple small transactions");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/deadlock/process-order
     * Test deadlock detection & recovery
     */
    @PostMapping("/process-order")
    public ResponseEntity<Map<String, Object>> testDeadlockHandling(
            @RequestParam Long bookingId,
            @RequestBody List<Long> dishIds) {

        Map<String, Object> response = new HashMap<>();
        try {
            deadlockService.processOrderWithDeadlockHandling(bookingId, dishIds);
            response.put("success", true);
            response.put("message", "Order processed with deadlock handling");
            response.put("strategy", "Deadlock Detection & Recovery");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/deadlock/simulate
     * Simulate deadlock scenario
     */
    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateDeadlock() {
        Map<String, Object> response = new HashMap<>();
        try {
            deadlockService.simulateDeadlock();
            response.put("success", true);
            response.put("message", "Check server logs for deadlock simulation");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}