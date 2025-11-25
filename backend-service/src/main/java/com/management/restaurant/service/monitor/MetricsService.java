package com.management.restaurant.service.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MeterRegistry meterRegistry;

    /**
     * Record booking creation time
     */
    public void recordBookingCreation(long durationMs) {
        Timer.builder("booking.creation")
                .description("Time to create a booking")
                .tag("operation", "create")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Count optimistic lock failures
     */
    public void recordOptimisticLockFailure(String entityType) {
        Counter.builder("lock.optimistic.failure")
                .description("Optimistic lock failures")
                .tag("entity", entityType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Count deadlocks
     */
    public void recordDeadlock(String operation) {
        Counter.builder("transaction.deadlock")
                .description("Deadlock occurrences")
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record transaction duration
     */
    public void recordTransactionDuration(String type, long durationMs) {
        Timer.builder("transaction.duration")
                .description("Transaction execution time")
                .tag("type", type)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Record lock acquisition time
     */
    public void recordLockAcquisition(String lockType, long waitTimeMs) {
        Timer.builder("lock.acquisition")
                .description("Time to acquire lock")
                .tag("type", lockType)
                .register(meterRegistry)
                .record(waitTimeMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Count successful/failed payments
     */
    public void recordPaymentResult(boolean success) {
        Counter.builder("payment.result")
                .description("Payment processing results")
                .tag("status", success ? "success" : "failed")
                .register(meterRegistry)
                .increment();
    }
}
