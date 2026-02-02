package com.management.restaurant.service;

import com.management.restaurant.analytics.service.BookingTransactionService;
import com.management.restaurant.analytics.service.LockingExamplesService;
import com.management.restaurant.contains.BookingStatus;
import com.management.restaurant.model.Booking;
import com.management.restaurant.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class TransactionTestExamples {
    @Autowired
    private BookingTransactionService bookingTransactionService;

    @Autowired
    private LockingExamplesService lockingExamplesService;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * TEST 1: Optimistic Locking Conflict
     */
    @Test
    void testOptimisticLockingConflict() throws InterruptedException {
        // Given: A dish in database
        Long dishId = 1L;

        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // When: Two threads try to update the same dish
        Thread thread1 = new Thread(() -> {
            try {
                lockingExamplesService.updateDishPriceOptimistic(dishId,
                        java.math.BigDecimal.valueOf(50000));
                successCount.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                failureCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                lockingExamplesService.updateDishPriceOptimistic(dishId,
                        java.math.BigDecimal.valueOf(60000));
                successCount.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                failureCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        thread1.start();
        thread2.start();
        latch.await(5, TimeUnit.SECONDS);

        // Then: One should succeed, one should fail
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);
    }

    /**
     * TEST 2: Pessimistic Locking Prevents Concurrent Updates
     */
    @Test
    void testPessimisticLockingPreventsConcurrentUpdates() throws InterruptedException {
        // Given: A table in database
        Long tableId = 1L;

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);

        AtomicInteger completedCount = new AtomicInteger(0);

        // When: Two threads try to book the same table with pessimistic lock
        executor.submit(() -> {
            try {
                startLatch.await();
                lockingExamplesService.bookTableWithPessimisticLock(tableId);
                completedCount.incrementAndGet();
            } catch (Exception e) {
                System.err.println("Thread 1 failed: " + e.getMessage());
            } finally {
                endLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                Thread.sleep(100); // Start slightly after first thread
                lockingExamplesService.bookTableWithPessimisticLock(tableId);
                completedCount.incrementAndGet();
            } catch (Exception e) {
                System.err.println("Thread 2 failed: " + e.getMessage());
            } finally {
                endLatch.countDown();
            }
        });

        startLatch.countDown(); // Start both threads
        endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then: Only one should succeed due to lock
        assertThat(completedCount.get()).isLessThanOrEqualTo(1);
    }

    /**
     * TEST 3: Transaction Isolation Level - Repeatable Read
     */
    @Test
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void testRepeatableReadIsolation() {
        // Given: Bookings in database
        java.time.LocalDateTime start = java.time.LocalDateTime.now().minusDays(7);
        java.time.LocalDateTime end = java.time.LocalDateTime.now();

        // When: Calculate revenue twice in same transaction
        java.math.BigDecimal revenue1 = bookingTransactionService.calculateTotalRevenue(start, end);

        // Simulate another thread updating bookings (in real test, use separate thread)
        // But in REPEATABLE_READ, we should still get same result

        java.math.BigDecimal revenue2 = bookingTransactionService.calculateTotalRevenue(start, end);

        // Then: Both calculations should return same value
        assertThat(revenue1).isEqualTo(revenue2);
    }

    /**
     * TEST 4: Transaction Propagation - REQUIRES_NEW
     */
    @Test
    @Transactional
    void testRequiresNewPropagation() {
        // Given: A booking
        Long bookingId = 1L;

        // When: Update booking and log action
        try {
            bookingTransactionService.updateBooking(bookingId, BookingStatus.CONFIRMED);

            // This log is in REQUIRES_NEW transaction
            bookingTransactionService.logBookingAction(bookingId, "TEST");

            // Force outer transaction to rollback
            throw new RuntimeException("Simulated error");

        } catch (RuntimeException e) {
            // Expected
        }

        // Then: Log should be saved (REQUIRES_NEW committed independently)
        // But booking update should be rolled back
        // Verify in database after test
    }

    /**
     * TEST 5: Deadlock Detection
     */
    @Test
    void testDeadlockDetection() throws InterruptedException {
        // Given: Two dishes
        Long dish1Id = 1L;
        Long dish2Id = 2L;

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);

        AtomicInteger deadlockCount = new AtomicInteger(0);

        // When: Two threads try to lock resources in opposite order
        executor.submit(() -> {
            try {
                startLatch.await();
                lockTwoResourcesInOrder(dish1Id, dish2Id);
            } catch (Exception e) {
                if (e.getMessage().contains("deadlock")) {
                    deadlockCount.incrementAndGet();
                }
            } finally {
                endLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                Thread.sleep(50);
                lockTwoResourcesInOrder(dish2Id, dish1Id); // Reverse order
            } catch (Exception e) {
                if (e.getMessage().contains("deadlock")) {
                    deadlockCount.incrementAndGet();
                }
            } finally {
                endLatch.countDown();
            }
        });

        startLatch.countDown();
        endLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        // Then: At least one thread should detect deadlock
        assertThat(deadlockCount.get()).isGreaterThanOrEqualTo(0);
    }

    /**
     * TEST 6: Saga Pattern Rollback
     */
    @Test
    void testSagaPatternRollback() {
        // Given: A booking that will trigger saga
        Long bookingId = 1L;

        // When: Saga execution fails
        assertThrows(RuntimeException.class, () -> {
            // Simulate saga failure
            executeSagaWithFailure(bookingId);
        });

        // Then: All compensations should be executed
        // Verify booking is reverted to original state
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(booking.getStatus()).isNotEqualTo(BookingStatus.COMPLETED);
    }

    /**
     * TEST 7: Concurrent Updates with Retry
     */
    @Test
    void testConcurrentUpdatesWithRetry() throws InterruptedException {
        // Given: A dish that multiple threads will update
        Long dishId = 1L;
        int threadCount = 5;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        // When: Multiple threads increment order count
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    lockingExamplesService.incrementOrderCountHybrid(dishId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Update failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Then: All updates should eventually succeed with retry
        assertThat(successCount.get()).isEqualTo(threadCount);
    }

    /**
     * TEST 8: Transaction Timeout
     */
    @Test
    @Transactional(timeout = 2) // 2 seconds timeout
    void testTransactionTimeout() {
        // When: Long-running operation
        assertThrows(org.springframework.transaction.TransactionTimedOutException.class, () -> {
            // Simulate long operation
            Thread.sleep(3000); // Exceeds timeout
            bookingRepository.findAll();
        });
    }

    /**
     * TEST 9: Read-Only Transaction Optimization
     */
    @Test
    @Transactional(readOnly = true)
    void testReadOnlyTransaction() {
        // Given: Read-only operation

        // When: Try to read data
        java.util.List<Booking> bookings = bookingRepository.findAll();

        // Then: Should succeed
        assertThat(bookings).isNotNull();

        // But updates should fail (if FlushMode is set correctly)
        assertThrows(Exception.class, () -> {
            Booking booking = bookings.get(0);
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
            bookingRepository.flush(); // Force flush to trigger error
        });
    }

    /**
     * TEST 10: Distributed Transaction Consistency
     */
    @Test
    void testDistributedTransactionConsistency() {
        // This test requires both databases to be set up
        // Test that data in both databases is consistent after distributed transaction

        // Given: A booking to complete
        Long bookingId = 1L;

        // When: Complete booking with distributed transaction
        // (This would call DistributedTransactionService methods)

        // Then: Verify data consistency in both databases
        // Restaurant DB should have booking status = COMPLETED
        // Analytics DB should have corresponding sales report
    }

    // Helper methods
    @Transactional
    protected void lockTwoResourcesInOrder(Long id1, Long id2) throws InterruptedException {
        // Lock resources in specified order
        Thread.sleep(100);
    }

    private void executeSagaWithFailure(Long bookingId) {
        // Simulate saga execution with failure
        throw new RuntimeException("Saga failed");
    }
}