package com.management.restaurant.analytics.service.implement;

import com.management.restaurant.contains.BookingStatus;
import com.management.restaurant.contains.TableStatus;
import com.management.restaurant.exception.DeadlockException;
import com.management.restaurant.model.Booking;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.TableEntity;
import com.management.restaurant.repository.BookingRepository;
import com.management.restaurant.repository.DishRepository;
import com.management.restaurant.repository.TableRepository;
import com.management.restaurant.analytics.service.DeadlockPreventionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeadlockPreventionServiceImpl implements DeadlockPreventionService {
    private final EntityManager entityManager;
    private final BookingRepository bookingRepository;
    private final TableRepository tableRepository;
    private final DishRepository dishRepository;

    /**
     * STRATEGY 1: Lock Ordering - Luôn lock theo thứ tự ID tăng dần
     * Đây là cách tốt nhất để tránh deadlock
     */
    @Transactional
    @Override
    public void bookMultipleTablesWithOrdering(List<Long> tableIds) {
        log.info("Booking multiple tables with lock ordering");

        // Sort IDs để đảm bảo lock theo thứ tự
        List<Long> sortedIds = tableIds.stream()
                .sorted()
                .toList();

        List<TableEntity> lockedTables = new ArrayList<>();

        for (Long tableId : sortedIds) {
            TableEntity table = entityManager.find(
                    TableEntity.class,
                    tableId,
                    LockModeType.PESSIMISTIC_WRITE
            );

            if (table == null) {
                throw new RuntimeException("Table " + tableId + " not found");
            }

            if (table.getStatus() != TableStatus.AVAILABLE) {
                // Rollback nếu có bàn không available
                throw new RuntimeException("Table " + tableId + " is not available");
            }

            lockedTables.add(table);
            log.info("Locked table {}", tableId);
        }

        // Update tất cả bàn
        for (TableEntity table : lockedTables) {
            table.setStatus(TableStatus.BOOKED);
            entityManager.merge(table);
        }
        log.info("Successfully booked {} tables", lockedTables.size());
    }

    /**
     * STRATEGY 2: Timeout với Retry
     * Set timeout và retry khi gặp deadlock
     */
    @Transactional
    @Retryable(
            value = {DeadlockLoserDataAccessException.class, CannotAcquireLockException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2, random = true)
    )
    @Override
    public Booking createBookingWithRetry(Long tableId, Long userId, LocalDateTime bookingTime, int numberOfGuests) {
        log.info("Creating booking with retry strategy (attempt)");

        // Set lock timeout
        entityManager.setProperty("javax.persistence.lock.timeout", 3000);

        try {
            TableEntity table = entityManager.find(
                    TableEntity.class,
                    tableId,
                    LockModeType.PESSIMISTIC_WRITE
            );

            if (table == null) {
                throw new RuntimeException("Table not found");
            }

            if (table.getStatus() != TableStatus.AVAILABLE) {
                throw new RuntimeException("Table not available");
            }

            // Create booking
            Booking booking = Booking.builder()
                    .table(table)
                    .bookingTime(bookingTime)
                    .numberOfGuests(numberOfGuests)
                    .status(BookingStatus.PENDING)
                    .build();

            table.setStatus(TableStatus.BOOKED);

            entityManager.persist(booking);
            entityManager.merge(table);

            return booking;

        } catch (PessimisticLockException e) {
            log.warn("Lock acquisition failed, will retry", e);
            throw new CannotAcquireLockException("Cannot acquire lock", e);
        }
    }

    /**
     * STRATEGY 3: Shorter Transactions
     * Chia transaction lớn thành các transaction nhỏ
     */
    @Transactional
    @Override
    public Booking createBookingShortTransaction(Long tableId) {
        log.info("Creating booking with short transaction");

        // Step 1: Quick lock and update table (short transaction)
        TableEntity table = lockAndReserveTable(tableId);

        // Step 2: Create booking in separate transaction
        return createBookingRecord(table, LocalDateTime.now(), 4);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public TableEntity lockAndReserveTable(Long tableId) {
        TableEntity table = entityManager.find(
                TableEntity.class,
                tableId,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (table != null && table.getStatus() == TableStatus.AVAILABLE) {
            table.setStatus(TableStatus.BOOKED);
            entityManager.merge(table);
        }

        return table;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Booking createBookingRecord(TableEntity table, LocalDateTime bookingTime, int guests) {
        Booking booking = Booking.builder()
                .table(table)
                .bookingTime(bookingTime)
                .numberOfGuests(guests)
                .status(BookingStatus.PENDING)
                .build();

        return bookingRepository.save(booking);
    }

    /**
     * STRATEGY 4: Deadlock Detection & Recovery
     * Catch deadlock và handle gracefully
     */
    @Transactional
    @Override
    public void processOrderWithDeadlockHandling(Long bookingId, List<Long> dishIds) {
        log.info("Processing order with deadlock handling");

        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                // Sort dish IDs to prevent deadlock
                List<Long> sortedDishIds = new ArrayList<>(dishIds);
                Collections.sort(sortedDishIds);

                Booking booking = entityManager.find(
                        Booking.class,
                        bookingId,
                        LockModeType.PESSIMISTIC_WRITE
                );

                BigDecimal totalAmount = BigDecimal.ZERO;

                for (Long dishId : sortedDishIds) {
                    Dish dish = entityManager.find(
                            Dish.class,
                            dishId,
                            LockModeType.PESSIMISTIC_WRITE
                    );

                    if (dish != null) {
                        dish.setOrderCount(dish.getOrderCount() + 1);
                        totalAmount = totalAmount.add(dish.getPrice());
                        entityManager.merge(dish);
                    }
                }

                booking.setTotalAmount(totalAmount);
                entityManager.merge(booking);

                log.info("Order processed successfully");
                return;

            } catch (DeadlockLoserDataAccessException | PessimisticLockException e) {
                attempt++;
                log.warn("Deadlock detected, attempt {}/{}", attempt, maxRetries);

                if (attempt >= maxRetries) {
                    log.error("Failed after {} attempts", maxRetries);
                    throw new DeadlockException("Cannot process order due to deadlock", e);
                }

                // Exponential backoff
                try {
                    Thread.sleep((long) (Math.pow(2, attempt) * 100));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }

    /**
     * STRATEGY 5: Read-Write Split
     * Đọc dữ liệu trước khi lock để giảm thời gian giữ lock
     */
    @Transactional
    @Override
    public void updateDishPriceEfficient(Long dishId, BigDecimal newPrice) {
        log.info("Updating dish price with read-write split");

        // Step 1: Read without lock (fast)
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Dish not found"));

        // Step 2: Business logic (outside lock)
        BigDecimal calculatedPrice = newPrice.multiply(BigDecimal.valueOf(1.1)); // Add 10% tax

        // Step 3: Quick lock and update (short lock time)
        Dish lockedDish = entityManager.find(
                Dish.class,
                dishId,
                LockModeType.PESSIMISTIC_WRITE
        );

        lockedDish.setPrice(calculatedPrice);
        entityManager.merge(lockedDish);

        log.info("Price updated efficiently");
    }

    /**
     * ROLLBACK STRATEGY 1: Programmatic Rollback
     */
    @Transactional
    @Override
    public void processPaymentWithRollback(Long bookingId, BigDecimal amount) {
        log.info("Processing payment with programmatic rollback");

        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Simulate payment processing
            boolean paymentSuccess = processExternalPayment(amount);

            if (!paymentSuccess) {
                log.warn("Payment failed, rolling back");
                // Spring sẽ tự động rollback khi throw RuntimeException
                throw new RuntimeException("Payment failed");
            }

            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setTotalAmount(amount);
            bookingRepository.save(booking);

            log.info("Payment processed successfully");

        } catch (Exception e) {
            log.error("Transaction will be rolled back", e);
            throw e; // Trigger rollback
        }
    }

    /**
     * ROLLBACK STRATEGY 2: Savepoint (Manual Rollback)
     */
    @Transactional
    @Override
    public void processOrderWithSavepoint(Long bookingId, List<Long> dishIds) {
        log.info("Processing order with savepoint");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Long dishId : dishIds) {
            try {
                // Tạo savepoint trước mỗi dish
                Dish dish = dishRepository.findById(dishId).orElse(null);

                if (dish == null || !dish.getIsAvailable()) {
                    log.warn("Dish {} not available, skipping", dishId);
                    continue; // Skip dish này, không rollback toàn bộ
                }

                dish.setOrderCount(dish.getOrderCount() + 1);
                totalAmount = totalAmount.add(dish.getPrice());
                dishRepository.save(dish);

            } catch (Exception e) {
                log.error("Error processing dish {}, continuing with others", dishId, e);
                // Continue với các dish khác
            }
        }

        booking.setTotalAmount(totalAmount);
        bookingRepository.save(booking);
    }

    /**
     * ROLLBACK STRATEGY 3: No Rollback on Specific Exception
     */
    @Transactional(noRollbackFor = {IllegalArgumentException.class})
    @Override
    public Booking createBookingNoRollbackOnValidation(Long tableId, int numberOfGuests) {
        log.info("Creating booking with conditional rollback");

        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        // Validation error - sẽ KHÔNG rollback
        if (numberOfGuests > table.getCapacity()) {
            log.warn("Validation failed but transaction will commit");
            throw new IllegalArgumentException("Too many guests for this table");
        }

        Booking booking = Booking.builder()
                .table(table)
                .numberOfGuests(numberOfGuests)
                .bookingTime(LocalDateTime.now())
                .status(BookingStatus.PENDING)
                .build();

        return bookingRepository.save(booking);
    }

    /**
     * ROLLBACK STRATEGY 4: Rollback on Any Exception
     */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void processOrderStrictRollback(Long bookingId) {
        log.info("Processing with strict rollback policy");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        try {
            // Any exception here will trigger rollback
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // Checked exception cũng sẽ trigger rollback
            validateBooking(booking);

        } catch (Exception e) {
            log.error("Any exception triggers rollback", e);
            throw new RuntimeException(e);
        }
    }

    private void validateBooking(Booking booking) throws Exception {
        if (booking.getNumberOfGuests() <= 0) {
            throw new Exception("Invalid booking");
        }
    }

    private boolean processExternalPayment(BigDecimal amount) {
        // Simulate external payment
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Test Deadlock Scenario
     */
    @Override
    public void simulateDeadlock() {
        log.info("=== Simulating Deadlock Scenario ===");

        Long dishId1 = 1L;
        Long dishId2 = 2L;

        // Thread 1: Update dish 1 -> dish 2
        Thread t1 = new Thread(() -> {
            try {
                updateTwoDishes(dishId1, dishId2, BigDecimal.valueOf(100));
                log.info("Thread 1 completed");
            } catch (Exception e) {
                log.error("Thread 1 failed", e);
            }
        });

        // Thread 2: Update dish 2 -> dish 1 (reverse order = deadlock)
        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(100);
                updateTwoDishes(dishId2, dishId1, BigDecimal.valueOf(200));
                log.info("Thread 2 completed");
            } catch (Exception e) {
                log.error("Thread 2 failed", e);
            }
        });

        t1.start();
        t2.start();
    }

    @Transactional
    @Override
    public void updateTwoDishes(Long dishId1, Long dishId2, BigDecimal priceChange) {
        Dish dish1 = entityManager.find(Dish.class, dishId1, LockModeType.PESSIMISTIC_WRITE);
        log.info("Locked dish {}", dishId1);

        try {
            Thread.sleep(500); // Increase deadlock chance
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Dish dish2 = entityManager.find(Dish.class, dishId2, LockModeType.PESSIMISTIC_WRITE);
        log.info("Locked dish {}", dishId2);

        dish1.setPrice(dish1.getPrice().add(priceChange));
        dish2.setPrice(dish2.getPrice().add(priceChange));

        entityManager.merge(dish1);
        entityManager.merge(dish2);
    }
}