package com.management.restaurant.analytics.service.implement;

import com.management.restaurant.admin.service.NotificationService;
import com.management.restaurant.analytics.service.BookingPropagationService;
import com.management.restaurant.contains.BookingStatus;
import com.management.restaurant.model.Booking;
import com.management.restaurant.model.Notification;
import com.management.restaurant.model.OrderHistory;
import com.management.restaurant.repository.BookingRepository;
import com.management.restaurant.repository.NotificationRepository;
import com.management.restaurant.repository.OrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingPropagationServiceImpl implements BookingPropagationService {
    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

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
                .isRead(Boolean.valueOf(false))
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
                    .served(Boolean.valueOf(false))
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
                addOrderToBooking(bookingId, Long.valueOf(1L), 2);
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

@Service
@RequiredArgsConstructor
@Slf4j
class AuditLogService {
    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void logBookingUpdate(Long bookingId, BookingStatus newStatus) {
        log.info("Logging booking update in same transaction");

        Notification audit = Notification.builder()
                .title("Audit Log")
                .content(String.format("Booking %d updated to %s", bookingId, newStatus))
                .createdAt(LocalDateTime.now())
                .isRead(Boolean.valueOf(false))
                .build();

        notificationRepository.save(audit);
    }
}