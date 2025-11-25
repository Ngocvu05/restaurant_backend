package com.management.restaurant.service.advance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * ADVANCED TOPIC 1: DISTRIBUTED LOCKING WITH REDIS
 * <p>
 * Use Case: Multiple application instances cần lock shared resources
 * Example: Booking system với multiple servers
 * <p>
 * Benefits over Database Locking:
 * - Faster (in-memory)
 * - Distributed across multiple app instances
 * - Automatic expiration
 * - Less database load
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisDistributedLockService {
    private final RedisTemplate<String, String> redisTemplate;

    // Lua script for atomic lock release
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('del', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    /**
     * Try to acquire lock
     *
     * @param lockKey Unique key for the resource
     * @param timeout Lock expiration time
     * @return Lock token if acquired, null otherwise
     */
    public String tryLock(String lockKey, Duration timeout) {
        String lockToken = UUID.randomUUID().toString();

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockToken, timeout);

        if (Boolean.TRUE.equals(acquired)) {
            log.info("Lock acquired: {} with token: {}", lockKey, lockToken);
            return lockToken;
        }

        log.warn("Failed to acquire lock: {}", lockKey);
        return null;
    }

    /**
     * Release lock safely (only if token matches)
     *
     * @param lockKey The lock key
     * @param lockToken The token from tryLock
     * @return true if released, false otherwise
     */
    public boolean releaseLock(String lockKey, String lockToken) {
        if (lockToken == null) {
            return false;
        }

        RedisScript<Long> script = RedisScript.of(UNLOCK_SCRIPT, Long.class);
        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(lockKey),
                lockToken
        );

        boolean released = result != null && result == 1L;
        if (released) {
            log.info("Lock released: {} with token: {}", lockKey, lockToken);
        } else {
            log.warn("Failed to release lock: {} (token mismatch or expired)", lockKey);
        }

        return released;
    }

    /**
     * Execute action with distributed lock
     *
     * @param lockKey The resource to lock
     * @param timeout Lock timeout
     * @param action The action to execute
     * @param <T> Return type
     * @return Result of action, or null if lock not acquired
     */
    public <T> T executeWithLock(String lockKey, Duration timeout,
                                 java.util.function.Supplier<T> action) {
        String lockToken = tryLock(lockKey, timeout);

        if (lockToken == null) {
            throw new RuntimeException("Could not acquire lock: " + lockKey);
        }

        try {
            return action.get();
        } finally {
            releaseLock(lockKey, lockToken);
        }
    }

    /**
     * EXAMPLE 1: Book table with distributed lock
     */
    public void bookTableWithRedisLock(Long tableId, Long userId) {
        String lockKey = "table:booking:" + tableId;

        executeWithLock(lockKey, Duration.ofSeconds(10), () -> {
            log.info("Processing booking for table {} by user {}", tableId, userId);

            // Check if table is available
            // Create booking
            // Update table status

            try {
                Thread.sleep(2000); // Simulate processing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            log.info("Booking completed for table {}", tableId);
            return null;
        });
    }

    /**
     * EXAMPLE 2: Update inventory with lock
     */
    public void updateInventoryWithLock(Long dishId, int quantity) {
        String lockKey = "inventory:" + dishId;

        executeWithLock(lockKey, Duration.ofSeconds(5), () -> {
            log.info("Updating inventory for dish {}: quantity={}", (Object) dishId, (Object) quantity);

            // Read current inventory
            // Check if sufficient stock
            // Update inventory
            // Create inventory log

            log.info("Inventory updated for dish {}", dishId);
            return null;
        });
    }

    /**
     * EXAMPLE 3: Prevent duplicate payment processing
     */
    public boolean processPaymentWithLock(String orderId, java.math.BigDecimal amount) {
        String lockKey = "payment:processing:" + orderId;

        try {
            return executeWithLock(lockKey, Duration.ofMinutes(2), () -> {
                log.info("Processing payment for order {}: amount={}", orderId, amount);

                // Check if already processed
                // Call payment gateway
                // Update order status
                // Send confirmation

                try {
                    Thread.sleep(1000); // Simulate payment gateway call
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                log.info("Payment processed for order {}", orderId);
                return (Boolean) true;
            });
        } catch (Exception e) {
            log.error("Payment processing failed for order {}", orderId, e);
            return false;
        }
    }

    /**
     * EXAMPLE 4: Cron job synchronization across multiple instances
     */
    public void executeCronJobWithLock(String jobName, Runnable job) {
        String lockKey = "cronjob:" + jobName;
        String lockToken = tryLock(lockKey, Duration.ofMinutes(5));

        if (lockToken == null) {
            log.info("Cron job {} already running on another instance", jobName);
            return;
        }

        try {
            log.info("Starting cron job: {}", jobName);
            job.run();
            log.info("Cron job completed: {}", jobName);
        } finally {
            releaseLock(lockKey, lockToken);
        }
    }

    /**
     * EXAMPLE 5: Rate limiting with Redis
     */
    public boolean checkRateLimit(String userId, int maxRequests, Duration window) {
        String key = "ratelimit:" + userId;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, window);
        }

        if (count != null && count > maxRequests) {
            log.warn("Rate limit exceeded for user {}: {}/{}", (Object) userId, (Object) count, (Object) maxRequests);
            return false;
        }

        return true;
    }

    /**
     * ADVANCED: Redlock Algorithm (multiple Redis instances)
     * For production use, consider using Redisson library
     */
    public String tryRedlock(String resource, Duration timeout, int quorum) {
        // Implement Redlock algorithm
        // Try to acquire lock on majority of Redis instances
        // This is simplified version - use Redisson in production

        log.info("Attempting Redlock for resource: {}", resource);
        return tryLock(resource, timeout);
    }
}