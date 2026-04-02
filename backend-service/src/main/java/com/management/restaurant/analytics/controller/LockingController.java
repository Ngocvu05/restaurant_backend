package com.management.restaurant.analytics.controller;

import com.management.restaurant.analytics.service.LockingExamplesService;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.TableEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * CONTROLLER 3: Locking Mechanisms Demo
 */
@RestController
@RequestMapping("/api/transactions/locking")
@RequiredArgsConstructor
@Slf4j
public class LockingController {
    private final LockingExamplesService lockingService;

    /**
     * PUT /api/transactions/locking/optimistic/dish/{id}
     * Test Optimistic Locking - Update dish price
     */
    @PutMapping("/optimistic/dish/{id}")
    public ResponseEntity<Map<String, Object>> testOptimisticLocking(
            @PathVariable Long id,
            @RequestParam BigDecimal newPrice) {

        Map<String, Object> response = new HashMap<>();
        try {
            Dish dish = lockingService.updateDishPriceOptimistic(id, newPrice);
            response.put("success", true);
            response.put("dish", dish);
            response.put("lockingType", "OPTIMISTIC");
            response.put("description", "Uses @Version, retries on conflict");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("retryAdvice", "Automatic retry handled by @Retryable");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/locking/optimistic/conflict-test/{id}
     * Simulate optimistic locking conflict
     */
    @PostMapping("/optimistic/conflict-test/{id}")
    public ResponseEntity<Map<String, Object>> testOptimisticConflict(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            lockingService.testOptimisticLockingConflict(id);
            response.put("success", true);
            response.put("message", "Check server logs for conflict demonstration");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/locking/pessimistic/book-table/{id}
     * Test Pessimistic Locking - Book table
     */
    @PostMapping("/pessimistic/book-table/{id}")
    public ResponseEntity<Map<String, Object>> testPessimisticLocking(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            TableEntity table = lockingService.bookTableWithPessimisticLock(id);
            response.put("success", true);
            response.put("table", table);
            response.put("lockingType", "PESSIMISTIC_WRITE");
            response.put("description", "Exclusive lock, blocks other transactions");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/locking/pessimistic/deadlock-test
     * Simulate deadlock scenario
     */
    @PostMapping("/pessimistic/deadlock-test")
    public ResponseEntity<Map<String, Object>> testDeadlock() {
        Map<String, Object> response = new HashMap<>();
        try {
            lockingService.testPessimisticLockingDeadlock();
            response.put("success", true);
            response.put("message", "Check server logs for deadlock demonstration");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * PUT /api/transactions/locking/hybrid/dish/{id}
     * Test Hybrid approach (Optimistic + Pessimistic fallback)
     */
    @PutMapping("/hybrid/dish/{id}")
    public ResponseEntity<Map<String, Object>> testHybridLocking(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Dish dish = lockingService.incrementOrderCountHybrid(id);
            response.put("success", true);
            response.put("dish", dish);
            response.put("lockingType", "HYBRID");
            response.put("description", "Try optimistic first, fallback to pessimistic");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/locking/timeout/{id}
     * Test lock timeout
     */
    @PostMapping("/timeout/{id}")
    public ResponseEntity<Map<String, Object>> testLockTimeout(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            TableEntity table = lockingService.bookTableWithTimeout(id);
            response.put("success", true);
            response.put("table", table);
            response.put("timeout", "5 seconds");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timeoutOccurred", e.getMessage().contains("timeout"));
            return ResponseEntity.badRequest().body(response);
        }
    }
}