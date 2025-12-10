package com.management.restaurant.analytics.service.implement;

import com.management.restaurant.admin.service.NotificationService;
import com.management.restaurant.analytics.service.BookingTransactionService;
import com.management.restaurant.analytics.service.MongoUserSessionService;
import com.management.restaurant.contains.BookingStatus;
import com.management.restaurant.contains.TableStatus;
import com.management.restaurant.exception.ResourceNotFoundException;
import com.management.restaurant.model.Booking;
import com.management.restaurant.model.Notification;
import com.management.restaurant.model.OrderHistory;
import com.management.restaurant.model.TableEntity;
import com.management.restaurant.repository.BookingRepository;
import com.management.restaurant.repository.NotificationRepository;
import com.management.restaurant.repository.OrderHistoryRepository;
import com.management.restaurant.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingTransactionServiceImpl implements BookingTransactionService {
    private final BookingRepository bookingRepository;
    private final TableRepository tableRepository;
    private final NotificationRepository notificationRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    /**
     * READ_COMMITTED: Tránh dirty read
     * Use case: Đọc thông tin booking đã được confirm
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    @Override
    public Booking getConfirmedBooking(Long bookingId) {
        log.info("Getting confirmed booking with READ_COMMITTED isolation");
        return bookingRepository.findById(bookingId)
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    /**
     * REPEATABLE_READ: Đảm bảo đọc consistent data trong suốt transaction
     * Use case: Tính toán tổng doanh thu từ các bookings
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    @Override
    public BigDecimal calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating revenue with REPEATABLE_READ isolation");

        // Đọc lần 1
        BigDecimal total1 = bookingRepository
                .findByBookingTimeBetweenAndStatus(startDate, endDate, BookingStatus.COMPLETED)
                .stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Simulate some processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Đọc lần 2 - Sẽ trả về kết quả giống lần 1 trong cùng transaction
        BigDecimal total2 = bookingRepository
                .findByBookingTimeBetweenAndStatus(startDate, endDate, BookingStatus.COMPLETED)
                .stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Total1: {}, Total2: {} - Should be equal", total1, total2);
        return total2;
    }

    /**
     * SERIALIZABLE: Mức cao nhất, tránh phantom read
     * Use case: Đặt bàn - không cho phép 2 người đặt cùng 1 bàn
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public Booking createBookingWithSerializable(Long tableId, Long userId, LocalDateTime bookingTime, int numberOfGuests) {
        log.info("Creating booking with SERIALIZABLE isolation");

        // Kiểm tra bàn available
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new IllegalStateException("Table is not available");
        }

        // Kiểm tra không có booking trùng thời gian
        boolean hasConflict = bookingRepository
                .existsByTable_IdAndBookingTimeAndStatusNot(
                        tableId, bookingTime, BookingStatus.CANCELLED);

        if (hasConflict) {
            throw new IllegalStateException("Time slot already booked");
        }

        // Tạo booking
        Booking booking = Booking.builder()
                .table(table)
                .bookingTime(bookingTime)
                .numberOfGuests(numberOfGuests)
                .status(BookingStatus.PENDING)
                .build();

        // Update table status
        table.setStatus(TableStatus.BOOKED);
        tableRepository.save(table);

        return bookingRepository.save(booking);
    }

    /**
     * DEFAULT (database default): Thường là READ_COMMITTED
     * Use case: CRUD operations thông thường
     */
    @Transactional
    @Override
    public Booking updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        log.info("Updating booking status with DEFAULT isolation");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(newStatus);

        // Update table status based on booking status
        if (newStatus == BookingStatus.CANCELLED || newStatus == BookingStatus.COMPLETED) {
            TableEntity table = booking.getTable();
            table.setStatus(TableStatus.AVAILABLE);
            tableRepository.save(table);
        }

        return bookingRepository.save(booking);
    }

    /**
     * So sánh isolation levels
     */
    @Override
    public void demonstrateIsolationLevels(Long bookingId) {
        log.info("=== Demonstrating Isolation Levels ===");

        // Thread 1: Read with READ_COMMITTED
        new Thread(() -> {
            try {
                Booking booking =   getConfirmedBooking(bookingId);
                log.info("Thread 1 (READ_COMMITTED): {}", booking.getStatus());
            } catch (Exception e) {
                log.error("Thread 1 error", e);
            }
        }).start();

        // Thread 2: Update booking
        new Thread(() -> {
            try {
                Thread.sleep(500);
                updateBookingStatus(bookingId, BookingStatus.COMPLETED);
                log.info("Thread 2: Updated booking status");
            } catch (Exception e) {
                log.error("Thread 2 error", e);
            }
        }).start();
    }

    /**
     * REQUIRED (default): Join existing transaction hoặc tạo mới
     * Use case: Standard CRUD operations
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Booking updateBooking(Long bookingId, BookingStatus newStatus) {
        log.info("Updating booking with REQUIRED propagation");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(newStatus);
        Booking savedBooking = bookingRepository.save(booking);

        // Các method này sẽ join vào transaction hiện tại
        auditLogService.logBookingUpdate(bookingId, newStatus);

        return savedBooking;
    }

    /**
     * REQUIRES_NEW: Luôn tạo transaction mới, suspend transaction hiện tại
     * Use case: Logging, audit trail (không muốn bị rollback khi business logic fail)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void logBookingAction(Long bookingId, String action) {
        log.info("Logging with REQUIRES_NEW - Independent transaction");

        // Log này sẽ được commit ngay cả khi outer transaction rollback
        Notification notification = Notification.builder()
                .title("Booking Action")
                .content(String.format("Booking %d: %s at %s",
                        bookingId, action, LocalDateTime.now()))
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Log saved independently");
    }

    /**
     * MANDATORY: Bắt buộc phải có transaction
     * Use case: Methods không nên gọi trực tiếp, chỉ gọi trong transaction
     */
    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void validateBookingInTransaction(Booking booking) {
        log.info("Validating booking with MANDATORY propagation");

        if (booking.getNumberOfGuests() <= 0) {
            throw new IllegalArgumentException("Invalid number of guests");
        }

        if (booking.getBookingTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Booking time must be in future");
        }
    }

    /**
     * SUPPORTS: Chạy trong transaction nếu có, không có thì chạy non-transactional
     * Use case: Read operations linh hoạt
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Override
    public Booking getBooking(Long bookingId) {
        log.info("Getting booking with SUPPORTS propagation");
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    /**
     * NOT_SUPPORTED: Suspend transaction hiện tại, chạy non-transactional
     * Use case: Operations không cần transaction, tối ưu performance
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void sendEmailNotification(Long bookingId, String emailContent) {
        log.info("Sending email with NOT_SUPPORTED - No transaction");

        // Email sending không cần transaction
        // Simulation
        log.info("Email sent for booking {}: {}", bookingId, emailContent);
    }

    /**
     * NEVER: Throw exception nếu có transaction
     * Use case: Đảm bảo method chạy outside transaction
     */
    @Transactional(propagation = Propagation.NEVER)
    @Override
    public void performNonTransactionalTask(String taskName) {
        log.info("Performing non-transactional task: {}", taskName);

        // Tasks như calling external APIs, file operations
        // Không được chạy trong transaction
    }

    /**
     * NESTED: Tạo nested transaction (savepoint)
     * Use case: Partial rollback - rollback một phần nhỏ mà không ảnh hưởng toàn bộ
     * Note: Chỉ hoạt động với JDBC, không hoạt động với JPA
     */
    @Transactional(propagation = Propagation.NESTED)
    @Override
    public void addOrderToBooking(Long bookingId, Long dishId, int quantity) {
        log.info("Adding order with NESTED propagation");

        try {
            OrderHistory order = OrderHistory.builder()
                    .booking(bookingRepository.findById(bookingId).orElseThrow())
                    .quantity(quantity)
                    .served(false)
                    .build();
            orderHistoryRepository.save(order);

            // Nếu operation này fail, chỉ rollback nested transaction
            // Outer transaction vẫn có thể commit
        } catch (Exception e) {
            log.error("Failed to add order, but outer transaction continues", e);
            throw e; // Re-throw để rollback nested transaction
        }
    }

    /**
     * Demonstration: Complex scenario với nhiều propagation levels
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void complexBookingOperation(Long bookingId) {
        log.info("=== Starting complex booking operation ===");

        try {
            // 1. Update booking (REQUIRED - join transaction)
            Booking booking = updateBooking(bookingId, BookingStatus.CONFIRMED);

            // 2. Log action (REQUIRES_NEW - independent transaction)
            logBookingAction(bookingId, "CONFIRMED");

            // 3. Validate trong transaction (MANDATORY)
            validateBookingInTransaction(booking);

            // 4. Add order (NESTED - partial rollback nếu fail)
            try {
                addOrderToBooking(bookingId, 1L, 2);
            } catch (Exception e) {
                log.warn("Failed to add order, continuing...");
            }

            // 5. Send email (NOT_SUPPORTED - no transaction)
            sendEmailNotification(bookingId, "Booking confirmed");

            log.info("=== Complex operation completed ===");

        } catch (Exception e) {
            log.error("Complex operation failed", e);
            throw e; // Rollback main transaction
        }
    }

    /**
     * Test rollback behavior
     */
    @Transactional
    @Override
    public void testRollbackScenario(Long bookingId) {
        log.info("Testing rollback scenario");

        // Update booking
        updateBooking(bookingId, BookingStatus.CONFIRMED);

        // Log action - sẽ được commit dù outer transaction rollback
        logBookingAction(bookingId, "TESTING");

        // Force rollback
        throw new RuntimeException("Simulated error - trigger rollback");
    }
}