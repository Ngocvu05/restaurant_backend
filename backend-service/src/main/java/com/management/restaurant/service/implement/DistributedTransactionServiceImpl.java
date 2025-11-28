package com.management.restaurant.service.implement;

import com.management.restaurant.analytics.model.SalesReport;
import com.management.restaurant.analytics.repository.SalesReportRepository;
import com.management.restaurant.contains.BookingStatus;
import com.management.restaurant.helper.SagaTransaction;
import com.management.restaurant.model.Booking;
import com.management.restaurant.repository.BookingRepository;
import com.management.restaurant.service.DistributedTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

/**
 * Service demonstrating Distributed Transactions across multiple databases
 * <p>
 * IMPORTANT: Spring @Transactional chỉ hoạt động với 1 database
 * Để handle multiple databases, có 3 approaches:
 * <p>
 * 1. ChainedTransactionManager (deprecated) - Execute transactions sequentially
 * 2. JTA/XA Transactions (Atomikos, Bitronix) - True 2-phase commit
 * 3. Saga Pattern - Compensating transactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedTransactionServiceImpl implements DistributedTransactionService {
    private final BookingRepository bookingRepository;
    private final SalesReportRepository salesReportRepository;

    @Qualifier("restaurantTransactionManager")
    private final PlatformTransactionManager restaurantTxManager;

    @Qualifier("analyticsTransactionManager")
    private final PlatformTransactionManager analyticsTxManager;

    /**
     * APPROACH 1: Sequential Transactions (Simple but not ACID)
     * Commit DB1 first, then DB2
     * Problem: Nếu DB2 fail, DB1 đã commit -> data inconsistency
     */
    @Override
    public void completeBookingSequential(Long bookingId) {
        log.info("Completing booking with sequential transactions");

        Booking booking = null;

        // Transaction 1: Update restaurant DB
        try {
            TransactionTemplate restaurantTx = new TransactionTemplate(restaurantTxManager);
            booking = restaurantTx.execute(status -> {
                Booking b = bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new RuntimeException("Booking not found"));

                b.setStatus(BookingStatus.COMPLETED);
                return bookingRepository.save(b);
            });

            log.info("Restaurant DB updated successfully");

        } catch (Exception e) {
            log.error("Failed to update restaurant DB", e);
            throw new RuntimeException("Booking update failed", e);
        }

        // Transaction 2: Update analytics DB
        try {
            TransactionTemplate analyticsTx = new TransactionTemplate(analyticsTxManager);
            Booking finalBooking = booking;

            analyticsTx.execute(status -> {
                assert finalBooking != null;
                SalesReport report = SalesReport.builder()
                        .bookingId(finalBooking.getId())
                        .amount(finalBooking.getTotalAmount())
                        .reportDate(LocalDate.now())
                        .status("COMPLETED")
                        .build();

                return salesReportRepository.save(report);
            });

            log.info("Analytics DB updated successfully");

        } catch (Exception e) {
            log.error("Failed to update analytics DB - Data inconsistency!", e);
            // Problem: Restaurant DB đã commit, không thể rollback
            // Need compensating transaction để rollback booking
            compensateBookingUpdate(bookingId);
            throw new RuntimeException("Analytics update failed", e);
        }
    }

    /**
     * APPROACH 2: Saga Pattern with Compensating Transactions
     * Best practice cho distributed transactions
     */
    @Override
    public void completeBookingWithSaga(Long bookingId) {
        log.info("Completing booking with Saga pattern");

        SagaTransaction saga = new SagaTransaction();

        try {
            // Step 1: Update booking
            Booking booking = updateBookingWithCompensation(bookingId, saga);

            // Step 2: Create sales report
            createSalesReportWithCompensation(booking, saga);

            // Step 3: Send notification (external service)
            sendNotificationWithCompensation(booking, saga);

            log.info("Saga completed successfully");
            saga.commit();

        } catch (Exception e) {
            log.error("Saga failed, executing compensations", e);
            saga.rollback();
            throw new RuntimeException("Transaction failed", e);
        }
    }

    private Booking updateBookingWithCompensation(Long bookingId, SagaTransaction saga) {
        TransactionTemplate tx = new TransactionTemplate(restaurantTxManager);

        Booking booking = tx.execute(status -> {
            Booking b = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            BookingStatus oldStatus = b.getStatus();
            b.setStatus(BookingStatus.COMPLETED);
            Booking saved = bookingRepository.save(b);

            // Register compensation
            saga.addCompensation(() -> {
                log.info("Compensating: Reverting booking status");
                TransactionTemplate compensateTx = new TransactionTemplate(restaurantTxManager);
                compensateTx.execute(s -> {
                    Booking toRevert = bookingRepository.findById(bookingId).orElse(null);
                    if (toRevert != null) {
                        toRevert.setStatus(oldStatus);
                        bookingRepository.save(toRevert);
                    }
                    return null;
                });
            });

            return saved;
        });

        log.info("Booking updated with compensation registered");
        return booking;
    }

    private void createSalesReportWithCompensation(Booking booking, SagaTransaction saga) {
        TransactionTemplate tx = new TransactionTemplate(analyticsTxManager);

        SalesReport report = tx.execute(status -> {
            SalesReport r = SalesReport.builder()
                    .bookingId(booking.getId())
                    .amount(booking.getTotalAmount())
                    .reportDate(LocalDate.now())
                    .status("COMPLETED")
                    .build();

            SalesReport saved = salesReportRepository.save(r);

            // Register compensation
            saga.addCompensation(() -> {
                log.info("Compensating: Deleting sales report");
                TransactionTemplate compensateTx = new TransactionTemplate(analyticsTxManager);
                compensateTx.execute(s -> {
                    salesReportRepository.deleteById(saved.getId());
                    return null;
                });
            });

            return saved;
        });

        log.info("Sales report created with compensation registered");
    }

    private void sendNotificationWithCompensation(Booking booking, SagaTransaction saga) {
        // Simulate external service call
        boolean success = sendExternalNotification(booking);

        if (success) {
            saga.addCompensation(() -> {
                log.info("Compensating: Sending cancellation notification");
                sendCancellationNotification(booking);
            });
        } else {
            throw new RuntimeException("Failed to send notification");
        }
    }

    /**
     * APPROACH 3: Two-Phase Commit (2PC) with Best Effort
     * Prepare both transactions, then commit
     */
    @Override
    public void completeBookingWith2PC(Long bookingId) {
        log.info("Completing booking with 2-Phase Commit pattern");

        TransactionTemplate restaurantTx = new TransactionTemplate(restaurantTxManager);
        TransactionTemplate analyticsTx = new TransactionTemplate(analyticsTxManager);

        Booking booking;
        SalesReport report;

        // PHASE 1: PREPARE
        try {
            // Prepare restaurant DB transaction
            booking = restaurantTx.execute(status -> {
                Booking b = bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new RuntimeException("Booking not found"));

                b.setStatus(BookingStatus.COMPLETED);
                // Don't flush yet - just prepare
                return bookingRepository.save(b);
            });

            log.info("Phase 1a: Restaurant DB prepared");

            // Prepare analytics DB transaction
            Booking finalBooking = booking;
            report = analyticsTx.execute(status -> {
                SalesReport r = SalesReport.builder()
                        .bookingId(finalBooking.getId())
                        .amount(finalBooking.getTotalAmount())
                        .reportDate(LocalDate.now())
                        .status("COMPLETED")
                        .build();

                return salesReportRepository.save(r);
            });
            log.info("Phase 1b: Analytics DB prepared");

        } catch (Exception e) {
            log.error("Phase 1 failed, aborting", e);
            throw new RuntimeException("Prepare phase failed", e);
        }
        // PHASE 2: COMMIT (already committed by Spring)
        log.info("Phase 2: Both transactions committed");
    }

    /**
     * APPROACH 4: Eventual Consistency with Event Sourcing
     * Update primary DB, publish event, async update secondary DB
     */
    @Transactional("restaurantTransactionManager")
    @Override
    public void completeBookingEventual(Long bookingId) {
        log.info("Completing booking with eventual consistency");

        // Update restaurant DB
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        // Publish event (async update analytics DB)
        publishBookingCompletedEvent(booking);

        log.info("Booking completed, event published for eventual consistency");
    }

    // Event handler runs in separate transaction
    @Transactional("analyticsTransactionManager")
    public void handleBookingCompletedEvent(Booking booking) {
        log.info("Handling booking completed event");

        SalesReport report = SalesReport.builder()
                .bookingId(booking.getId())
                .amount(booking.getTotalAmount())
                .reportDate(LocalDate.now())
                .status("COMPLETED")
                .build();

        salesReportRepository.save(report);
        log.info("Analytics updated via event");
    }

    /**
     * APPROACH 5: Manual Transaction Management
     * Full control over transaction boundaries
     */
    @Override
    public void completeBookingManual(Long bookingId) {
        log.info("Completing booking with manual transaction management");

        TransactionStatus restaurantTxStatus = null;
        TransactionStatus analyticsTxStatus = null;

        try {
            // Start restaurant transaction
            restaurantTxStatus = restaurantTxManager.getTransaction(
                    new org.springframework.transaction.support.DefaultTransactionDefinition()
            );

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);

            log.info("Restaurant DB updated, transaction not committed yet");

            // Start analytics transaction
            analyticsTxStatus = analyticsTxManager.getTransaction(
                    new org.springframework.transaction.support.DefaultTransactionDefinition()
            );

            SalesReport report = SalesReport.builder()
                    .bookingId(booking.getId())
                    .amount(booking.getTotalAmount())
                    .reportDate(LocalDate.now())
                    .status("COMPLETED")
                    .build();
            salesReportRepository.save(report);

            log.info("Analytics DB updated, transaction not committed yet");

            // Commit both
            restaurantTxManager.commit(restaurantTxStatus);
            log.info("Restaurant transaction committed");

            analyticsTxManager.commit(analyticsTxStatus);
            log.info("Analytics transaction committed");

        } catch (Exception e) {
            log.error("Error occurred, rolling back", e);

            if (analyticsTxStatus != null && !analyticsTxStatus.isCompleted()) {
                analyticsTxManager.rollback(analyticsTxStatus);
                log.info("Analytics transaction rolled back");
            }

            if (restaurantTxStatus != null && !restaurantTxStatus.isCompleted()) {
                restaurantTxManager.rollback(restaurantTxStatus);
                log.info("Restaurant transaction rolled back");
            }

            throw new RuntimeException("Transaction failed", e);
        }
    }

    /**
     * Comparison Demo
     */
    @Override
    public void demonstrateDistributedTransactions(Long bookingId) {
        log.info("=== Demonstrating Distributed Transaction Approaches ===");

        try {
            log.info("\n1. Sequential Transactions");
            completeBookingSequential(bookingId);
        } catch (Exception e) {
            log.error("Sequential approach failed", e);
        }

        try {
            log.info("\n2. Saga Pattern");
            completeBookingWithSaga(bookingId);
        } catch (Exception e) {
            log.error("Saga approach failed", e);
        }

        try {
            log.info("\n3. Two-Phase Commit");
            completeBookingWith2PC(bookingId);
        } catch (Exception e) {
            log.error("2PC approach failed", e);
        }

        try {
            log.info("\n4. Eventual Consistency");
            completeBookingEventual(bookingId);
        } catch (Exception e) {
            log.error("Eventual consistency approach failed", e);
        }

        try {
            log.info("\n5. Manual Transaction Management");
            completeBookingManual(bookingId);
        } catch (Exception e) {
            log.error("Manual approach failed", e);
        }
    }

    // Helper methods
    private void compensateBookingUpdate(Long bookingId) {
        log.info("Executing compensation for booking {}", bookingId);
        TransactionTemplate tx = new TransactionTemplate(restaurantTxManager);
        tx.execute(status -> {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking != null) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
            }
            return null;
        });
    }

    private boolean sendExternalNotification(Booking booking) {
        log.info("Sending notification for booking {}", booking.getId());
        return true; // Simulate success
    }

    private void sendCancellationNotification(Booking booking) {
        log.info("Sending cancellation notification for booking {}", booking.getId());
    }

    private void publishBookingCompletedEvent(Booking booking) {
        log.info("Publishing BookingCompletedEvent for booking {}", booking.getId());
        // In real app: use Spring Events, Kafka, RabbitMQ, etc.
    }
}