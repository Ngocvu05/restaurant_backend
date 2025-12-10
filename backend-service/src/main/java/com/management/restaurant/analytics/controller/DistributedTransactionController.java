package com.management.restaurant.analytics.controller;

import com.management.restaurant.analytics.service.DistributedTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * CONTROLLER 5: Distributed Transactions Demo
 */
@RestController
@RequestMapping("/api/transactions/distributed")
@RequiredArgsConstructor
@Slf4j
public class DistributedTransactionController {
    private final DistributedTransactionService distributedService;

    /**
     * POST /api/transactions/distributed/sequential/{id}
     * Test sequential transactions
     */
    @PostMapping("/sequential/{id}")
    public ResponseEntity<Map<String, Object>> testSequential(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            distributedService.completeBookingSequential(id);
            response.put("success", true);
            response.put("approach", "Sequential Transactions");
            response.put("description", "Commit DB1 first, then DB2");
            response.put("warning", "Not ACID - if DB2 fails, DB1 already committed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/distributed/saga/{id}
     * Test Saga pattern
     */
    @PostMapping("/saga/{id}")
    public ResponseEntity<Map<String, Object>> testSaga(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            distributedService.completeBookingWithSaga(id);
            response.put("success", true);
            response.put("approach", "Saga Pattern");
            response.put("description", "Compensating transactions for rollback");
            response.put("recommended", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Compensations executed");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/distributed/2pc/{id}
     * Test Two-Phase Commit
     */
    @PostMapping("/2pc/{id}")
    public ResponseEntity<Map<String, Object>> test2PC(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            distributedService.completeBookingWith2PC(id);
            response.put("success", true);
            response.put("approach", "Two-Phase Commit (2PC)");
            response.put("description", "Prepare both, then commit");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/distributed/eventual/{id}
     * Test eventual consistency
     */
    @PostMapping("/eventual/{id}")
    public ResponseEntity<Map<String, Object>> testEventual(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            distributedService.completeBookingEventual(id);
            response.put("success", true);
            response.put("approach", "Eventual Consistency");
            response.put("description", "Update primary DB, publish event for async update");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/distributed/manual/{id}
     * Test manual transaction management
     */
    @PostMapping("/manual/{id}")
    public ResponseEntity<Map<String, Object>> testManual(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            distributedService.completeBookingManual(id);
            response.put("success", true);
            response.put("approach", "Manual Transaction Management");
            response.put("description", "Full control over transaction boundaries");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/transactions/distributed/demo/{id}
     * Demonstrate all approaches
     */
    @PostMapping("/demo/{id}")
    public ResponseEntity<Map<String, Object>> demonstrateAll(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            distributedService.demonstrateDistributedTransactions(id);
            response.put("success", true);
            response.put("message", "Check server logs for all approaches demonstration");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}