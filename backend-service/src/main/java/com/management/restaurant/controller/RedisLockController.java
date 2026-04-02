package com.management.restaurant.controller;

import com.management.restaurant.service.advance.RedisDistributedLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisLockController {
    private final RedisDistributedLockService lockService;
    /**
     * POST /api/redis/book-table
     * Test distributed locking for table booking
     */
    @PostMapping("/book-table")
    public ResponseEntity<?> bookTable(
            @RequestParam Long tableId,
            @RequestParam Long userId) {

        Map<String, Object> response = new HashMap<>();

        try {
            lockService.bookTableWithRedisLock(tableId, userId);
            response.put("success", Optional.of(true));
            response.put("message", "Table booked successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", Optional.of(false));
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/redis/update-inventory
     * Test distributed locking for inventory update
     */
    @PostMapping("/update-inventory")
    public ResponseEntity<?> updateInventory(
            @RequestParam Long dishId,
            @RequestParam int quantity) {

        Map<String, Object> response = new HashMap<>();

        try {
            lockService.updateInventoryWithLock(dishId, quantity);
            response.put("success", Optional.of(true));
            response.put("message", "Inventory updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", Optional.of(false));
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/redis/process-payment
     * Test payment processing with lock
     */
    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(
            @RequestParam String orderId,
            @RequestParam java.math.BigDecimal amount) {

        Map<String, Object> response = new HashMap<>();

        boolean success = lockService.processPaymentWithLock(orderId, amount);

        response.put("success", Optional.of(success));
        response.put("message", success ? "Payment processed" : "Payment failed");
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    /**
     * GET /api/redis/rate-limit-check
     * Test rate limiting
     */
    @GetMapping("/rate-limit-check")
    public ResponseEntity<?> checkRateLimit(@RequestParam String userId) {
        Map<String, Object> response = new HashMap<>();

        boolean allowed = lockService.checkRateLimit(
                userId,
                10, // max 10 requests
                java.time.Duration.ofMinutes(1) // per minute
        );

        response.put("allowed", Optional.of(allowed));
        response.put("message", allowed ? "Request allowed" : "Rate limit exceeded");

        return allowed ? ResponseEntity.ok(response) : ResponseEntity.status(429).body(response);
    }
}
