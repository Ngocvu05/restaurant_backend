package com.management.restaurant.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Transaction Helper Utility
 * <p>
 * Provides convenient methods for working with distributed transactions
 * across multiple databases.
 * <p>
 * Usage:
 * <pre>
 * {@code
 * // Execute in restaurant database transaction
 * transactionHelper.executeInRestaurantTransaction(() -> {
 *     // Your code here
 *     return result;
 * });
 *
 * // Execute in analytics database transaction
 * transactionHelper.executeInAnalyticsTransaction(() -> {
 *     // Your code here
 *     return result;
 * });
 * }
 * </pre>
 *
 * @author Restaurant Management System
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionHelper {
    @Qualifier("restaurantTransactionManager")
    private final PlatformTransactionManager restaurantTxManager;

    @Qualifier("analyticsTransactionManager")
    private final PlatformTransactionManager analyticsTxManager;

    // ========================================
    // RESTAURANT DATABASE TRANSACTIONS
    // ========================================

    /**
     * Execute code within a restaurant database transaction
     *
     * @param <T> Return type
     * @param action Supplier that contains the business logic
     * @return Result from the action
     */
    public <T> T executeInRestaurantTransaction(Supplier<T> action) {
        log.debug("Executing in restaurant database transaction");
        TransactionTemplate template = new TransactionTemplate(restaurantTxManager);
        return template.execute(status -> action.get());
    }

    /**
     * Execute code within a restaurant database transaction (void return)
     *
     * @param action Runnable that contains the business logic
     */
    public void executeInRestaurantTransaction(Runnable action) {
        log.debug("Executing in restaurant database transaction (void)");
        TransactionTemplate template = new TransactionTemplate(restaurantTxManager);
        template.execute(status -> {
            action.run();
            return null;
        });
    }

    /**
     * Execute code within a read-only restaurant database transaction
     *
     * @param <T> Return type
     * @param action Supplier that contains the business logic
     * @return Result from the action
     */
    public <T> T executeInRestaurantTransactionReadOnly(Supplier<T> action) {
        log.debug("Executing in restaurant database transaction (read-only)");
        TransactionTemplate template = new TransactionTemplate(restaurantTxManager);
        template.setReadOnly(true);
        return template.execute(status -> action.get());
    }

    // ========================================
    // ANALYTICS DATABASE TRANSACTIONS
    // ========================================

    /**
     * Execute code within an analytics database transaction
     *
     * @param <T> Return type
     * @param action Supplier that contains the business logic
     * @return Result from the action
     */
    public <T> T executeInAnalyticsTransaction(Supplier<T> action) {
        log.debug("Executing in analytics database transaction");
        TransactionTemplate template = new TransactionTemplate(analyticsTxManager);
        return template.execute(status -> action.get());
    }

    /**
     * Execute code within an analytics database transaction (void return)
     *
     * @param action Runnable that contains the business logic
     */
    public void executeInAnalyticsTransaction(Runnable action) {
        log.debug("Executing in analytics database transaction (void)");
        TransactionTemplate template = new TransactionTemplate(analyticsTxManager);
        template.execute(status -> {
            action.run();
            return null;
        });
    }

    /**
     * Execute code within a read-only analytics database transaction
     *
     * @param <T> Return type
     * @param action Supplier that contains the business logic
     * @return Result from the action
     */
    public <T> T executeInAnalyticsTransactionReadOnly(Supplier<T> action) {
        log.debug("Executing in analytics database transaction (read-only)");
        TransactionTemplate template = new TransactionTemplate(analyticsTxManager);
        template.setReadOnly(true);
        return template.execute(status -> action.get());
    }

    // ========================================
    // MANUAL TRANSACTION MANAGEMENT
    // ========================================

    /**
     * Begin a new restaurant database transaction manually
     *
     * @return TransactionStatus for managing the transaction
     */
    public TransactionStatus beginRestaurantTransaction() {
        log.debug("Beginning restaurant database transaction manually");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return restaurantTxManager.getTransaction(def);
    }

    /**
     * Begin a new analytics database transaction manually
     *
     * @return TransactionStatus for managing the transaction
     */
    public TransactionStatus beginAnalyticsTransaction() {
        log.debug("Beginning analytics database transaction manually");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return analyticsTxManager.getTransaction(def);
    }

    /**
     * Commit restaurant database transaction
     *
     * @param status TransactionStatus from beginRestaurantTransaction()
     */
    public void commitRestaurantTransaction(TransactionStatus status) {
        log.debug("Committing restaurant database transaction");
        restaurantTxManager.commit(status);
    }

    /**
     * Commit analytics database transaction
     *
     * @param status TransactionStatus from beginAnalyticsTransaction()
     */
    public void commitAnalyticsTransaction(TransactionStatus status) {
        log.debug("Committing analytics database transaction");
        analyticsTxManager.commit(status);
    }

    /**
     * Rollback restaurant database transaction
     *
     * @param status TransactionStatus from beginRestaurantTransaction()
     */
    public void rollbackRestaurantTransaction(TransactionStatus status) {
        log.warn("Rolling back restaurant database transaction");
        if (status != null && !status.isCompleted()) {
            restaurantTxManager.rollback(status);
        }
    }

    /**
     * Rollback analytics database transaction
     *
     * @param status TransactionStatus from beginAnalyticsTransaction()
     */
    public void rollbackAnalyticsTransaction(TransactionStatus status) {
        log.warn("Rolling back analytics database transaction");
        if (status != null && !status.isCompleted()) {
            analyticsTxManager.rollback(status);
        }
    }

    // ========================================
    // DISTRIBUTED TRANSACTION HELPERS
    // ========================================

    /**
     * Execute operations in both databases sequentially
     * Warning: This is NOT atomic across databases!
     *
     * @param <R> Restaurant operation return type
     * @param <A> Analytics operation return type
     * @param restaurantOperation Operation to execute in restaurant DB
     * @param analyticsOperation Operation to execute in analytics DB
     * @return DistributedResult containing results from both operations
     */
    public <R, A> DistributedResult<R, A> executeInBothDatabases(
            Supplier<R> restaurantOperation,
            Supplier<A> analyticsOperation) {

        log.info("Executing distributed operation across both databases");

        R restaurantResult = null;
        A analyticsResult = null;

        try {
            // Execute restaurant operation
            restaurantResult = executeInRestaurantTransaction(restaurantOperation);
            log.debug("Restaurant operation completed successfully");

            // Execute analytics operation
            analyticsResult = executeInAnalyticsTransaction(analyticsOperation);
            log.debug("Analytics operation completed successfully");

            return new DistributedResult<>(restaurantResult, analyticsResult, true);

        } catch (Exception e) {
            log.error("Distributed operation failed", e);
            return new DistributedResult<>(restaurantResult, analyticsResult, false);
        }
    }

    /**
     * Result holder for distributed operations
     */
    public static class DistributedResult<R, A> {
        private final R restaurantResult;
        private final A analyticsResult;
        private final boolean success;

        public DistributedResult(R restaurantResult, A analyticsResult, boolean success) {
            this.restaurantResult = restaurantResult;
            this.analyticsResult = analyticsResult;
            this.success = success;
        }

        public R getRestaurantResult() {
            return restaurantResult;
        }

        public A getAnalyticsResult() {
            return analyticsResult;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
