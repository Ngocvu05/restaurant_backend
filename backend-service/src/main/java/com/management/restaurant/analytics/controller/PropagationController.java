package com.management.restaurant.analytics.controller;

import com.management.restaurant.analytics.service.BookingPropagationService;
import com.management.restaurant.contains.BookingStatus;
import com.management.restaurant.model.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * CONTROLLER 2: Transaction Propagation Demo
 */
@RestController
@RequestMapping("/api/transactions/propagation")
@RequiredArgsConstructor
@Slf4j
public class PropagationController {
    private final BookingPropagationService propagationService;

    /**
     * PUT /api/transactions/propagation/required/{id}
     * Test REQUIRED propagation
     */
    @PutMapping("/required/{id}")
    public ResponseEntity<Map<String, Object>> testRequired(
            @PathVariable Long id,
            @RequestParam BookingStatus status) {

        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = propagationService.updateBooking(id, status);
            response.put("success", true);
            response.put("booking", booking);
            response.put("propagation", "REQUIRED");
            response.put("description", "Joins existing or creates new transaction");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/propagation/requires-new/{id}
     * Test REQUIRES_NEW propagation - Independent logging
     */
    @PostMapping("/requires-new/{id}")
    public ResponseEntity<Map<String, Object>> testRequiresNew(
            @PathVariable Long id,
            @RequestParam String action) {

        Map<String, Object> response = new HashMap<>();
        try {
            propagationService.logBookingAction(id, action);
            response.put("success", true);
            response.put("message", "Log saved in independent transaction");
            response.put("propagation", "REQUIRES_NEW");
            response.put("description", "Always creates new transaction, commits independently");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/propagation/complex/{id}
     * Test complex scenario with multiple propagation types
     */
    @PostMapping("/complex/{id}")
    public ResponseEntity<Map<String, Object>> testComplexOperation(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            propagationService.complexBookingOperation(id);
            response.put("success", true);
            response.put("message", "Complex operation completed successfully");
            response.put("description", "Demonstrates REQUIRED, REQUIRES_NEW, MANDATORY, NESTED, NOT_SUPPORTED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/propagation/rollback-test/{id}
     * Test rollback behavior with REQUIRES_NEW
     */
    @PostMapping("/rollback-test/{id}")
    public ResponseEntity<Map<String, Object>> testRollback(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            propagationService.testRollbackScenario(id);
            response.put("success", false);
            response.put("message", "This should never return");
        } catch (Exception e) {
            response.put("success", true);
            response.put("message", "Main transaction rolled back, but log was saved (REQUIRES_NEW)");
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}