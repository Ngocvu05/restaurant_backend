package com.management.restaurant.service.implement;

import com.management.restaurant.contains.BookingStatus;
import com.management.restaurant.contains.TableStatus;
import com.management.restaurant.exception.ResourceNotFoundException;
import com.management.restaurant.model.Booking;
import com.management.restaurant.model.TableEntity;
import com.management.restaurant.repository.BookingRepository;
import com.management.restaurant.repository.TableRepository;
import com.management.restaurant.service.BookingTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingTransactionServiceImpl implements BookingTransactionService {
    private final BookingRepository bookingRepository;
    private final TableRepository tableRepository;

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
                Booking booking = getConfirmedBooking(bookingId);
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
}