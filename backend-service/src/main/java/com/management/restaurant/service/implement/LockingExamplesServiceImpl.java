package com.management.restaurant.service.implement;

import com.management.restaurant.contains.TableStatus;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.TableEntity;
import com.management.restaurant.repository.DishRepository;
import com.management.restaurant.repository.TableRepository;
import com.management.restaurant.service.LockingExamplesService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class LockingExamplesServiceImpl implements LockingExamplesService {
    private final DishRepository dishRepository;
    private final TableRepository tableRepository;
    private final EntityManager entityManager;

    // ============= OPTIMISTIC LOCKING =============

    /**
     * Optimistic Locking với @Version
     * Use case: Cập nhật thông tin món ăn (ít conflict)
     */
    @Transactional
    @Retryable(
            value = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Override
    public Dish updateDishPriceOptimistic(Long dishId, BigDecimal newPrice) {
        // Thêm @Version vào Dish entity:
        // @Version
        // private Long version;

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Dish not found"));

        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        dish.setPrice(newPrice);

        // Nếu có thread khác đã update trước, sẽ throw OptimisticLockException
        return dishRepository.save(dish);
    }

    /**
     * Test Optimistic Locking Conflict
     */
    public void testOptimisticLockingConflict(Long dishId) {
        log.info("=== Testing Optimistic Locking Conflict ===");

        // Thread 1: Update price to 50000
        Thread thread1 = new Thread(() -> {
            try {
                updateDishPriceOptimistic(dishId, BigDecimal.valueOf(50000));
                log.info("Thread 1: Updated price to 50000");
            } catch (OptimisticLockingFailureException e) {
                log.warn("Thread 1: Optimistic lock failed - Retrying...");
            }
        });

        // Thread 2: Update price to 60000
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(50); // Start slightly after thread 1
                updateDishPriceOptimistic(dishId, BigDecimal.valueOf(60000));
                log.info("Thread 2: Updated price to 60000");
            } catch (Exception e) {
                log.warn("Thread 2: Optimistic lock failed - Retrying...");
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ============= PESSIMISTIC LOCKING =============

    /**
     * Pessimistic Write Lock (PESSIMISTIC_WRITE)
     * Use case: Đặt bàn - không cho thread khác đọc/ghi
     */
    @Transactional
    @Override
    public TableEntity bookTableWithPessimisticLock(Long tableId) {
        log.info("Booking table with PESSIMISTIC_WRITE lock");

        // Lock bàn ngay khi đọc - block các transaction khác
        TableEntity table = entityManager.find(
                TableEntity.class,
                tableId,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (table == null) {
            throw new RuntimeException("Table not found");
        }

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new RuntimeException("Table is not available");
        }

        // Simulate booking process
        try {
            Thread.sleep(2000); // Lock sẽ được giữ trong 2 giây
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        table.setStatus(TableStatus.BOOKED);
        return entityManager.merge(table);
    }

    /**
     * Pessimistic Read Lock (PESSIMISTIC_READ)
     * Use case: Đọc dữ liệu cần consistency cao, cho phép đọc concurrent
     */
    @Transactional
    @Override
    public TableEntity getTableWithPessimisticRead(Long tableId) {
        log.info("Reading table with PESSIMISTIC_READ lock");

        // Shared lock - cho phép các transaction khác đọc nhưng không ghi
        return entityManager.find(
                TableEntity.class,
                tableId,
                LockModeType.PESSIMISTIC_READ
        );
    }

    /**
     * Pessimistic Force Increment
     * Use case: Tăng version ngay cả khi không có thay đổi
     */
    @Transactional
    @Override
    public Dish incrementDishVersion(Long dishId) {
        log.info("Force incrementing dish version");

        Dish dish = entityManager.find(
                Dish.class,
                dishId,
                LockModeType.PESSIMISTIC_FORCE_INCREMENT
        );

        // Version sẽ tăng ngay cả khi không có thay đổi dữ liệu
        return dish;
    }

    /**
     * Test Pessimistic Locking - Deadlock scenario
     */
    public void testPessimisticLockingDeadlock() {
        log.info("=== Testing Pessimistic Locking Deadlock ===");

        Long table1Id = 1L;
        Long table2Id = 2L;

        // Thread 1: Lock table 1 -> table 2
        Thread thread1 = new Thread(() -> {
            try {
                lockTwoTables(table1Id, table2Id);
                log.info("Thread 1: Successfully locked both tables");
            } catch (Exception e) {
                log.error("Thread 1: Deadlock or timeout", e);
            }
        });

        // Thread 2: Lock table 2 -> table 1 (reverse order)
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(100);
                lockTwoTables(table2Id, table1Id);
                log.info("Thread 2: Successfully locked both tables");
            } catch (Exception e) {
                log.error("Thread 2: Deadlock or timeout", e);
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Transactional
    @Override
    public void lockTwoTables(Long firstTableId, Long secondTableId) {
        log.info("Locking tables {} and {}", firstTableId, secondTableId);

        // Lock first table
        TableEntity table1 = entityManager.find(
                TableEntity.class,
                firstTableId,
                LockModeType.PESSIMISTIC_WRITE
        );
        log.info("Locked table {}", firstTableId);

        try {
            Thread.sleep(500); // Increase chance of deadlock
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Lock second table - may cause deadlock
        TableEntity table2 = entityManager.find(
                TableEntity.class,
                secondTableId,
                LockModeType.PESSIMISTIC_WRITE
        );
        log.info("Locked table {}", secondTableId);
    }

    /**
     * Optimistic + Pessimistic Hybrid
     * Use case: Update order count - optimistic + fallback to pessimistic
     */
    @Transactional
    @Override
    public Dish incrementOrderCountHybrid(Long dishId) {
        log.info("Incrementing order count with hybrid locking");

        try {
            // Try optimistic first
            return incrementOrderCountOptimistic(dishId);
        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic lock failed, falling back to pessimistic");
            // Fallback to pessimistic
            return incrementOrderCountPessimistic(dishId);
        }
    }

    private Dish incrementOrderCountOptimistic(Long dishId) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Dish not found"));

        dish.setOrderCount(dish.getOrderCount() + 1);
        return dishRepository.save(dish);
    }

    private Dish incrementOrderCountPessimistic(Long dishId) {
        Dish dish = entityManager.find(
                Dish.class,
                dishId,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (dish == null) {
            throw new RuntimeException("Dish not found");
        }

        dish.setOrderCount(dish.getOrderCount() + 1);
        return entityManager.merge(dish);
    }

    /**
     * Lock Timeout Configuration
     */
    @Transactional
    @Override
    public TableEntity bookTableWithTimeout(Long tableId) {
        log.info("Booking table with timeout configuration");

        // Set lock timeout to 5 seconds
        entityManager.setProperty("javax.persistence.lock.timeout", 5000);

        try {
            TableEntity table = entityManager.find(
                    TableEntity.class,
                    tableId,
                    LockModeType.PESSIMISTIC_WRITE
            );

            if (table == null) {
                throw new RuntimeException("Table not found");
            }

            table.setStatus(TableStatus.BOOKED);
            return entityManager.merge(table);

        } catch (LockTimeoutException e) {
            log.error("Lock timeout after 5 seconds", e);
            throw new RuntimeException("Cannot acquire lock - table is busy");
        }
    }

    /**
     * Named Query với Locking
     */
    @Transactional
    @Override
    public Dish getDishWithLock(Long dishId) {
        TypedQuery<Dish> query = entityManager.createQuery(
                "SELECT d FROM Dish d WHERE d.id = :id",
                Dish.class
        );

        query.setParameter("id", dishId);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        return query.getSingleResult();
    }
}