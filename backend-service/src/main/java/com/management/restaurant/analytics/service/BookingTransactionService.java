package com.management.restaurant.analytics.service;

import com.management.restaurant.contains.BookingStatus;
import com.management.restaurant.model.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BookingTransactionService {
    Booking getConfirmedBooking(Long bookingId);
    BigDecimal calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);
    Booking createBookingWithSerializable(Long tableId, Long userId,
                                          LocalDateTime bookingTime,
                                          int numberOfGuests);
    Booking updateBookingStatus(Long bookingId, BookingStatus newStatus);
    void demonstrateIsolationLevels(Long bookingId);

    void addOrderToBooking(Long bookingId, Long dishId, int quantity);
    void performNonTransactionalTask(String taskName);
    void sendEmailNotification(Long bookingId, String emailContent);
    Booking getBooking(Long bookingId);
    void validateBookingInTransaction(Booking booking);
    void logBookingAction(Long bookingId, String action);
    Booking updateBooking(Long bookingId, BookingStatus newStatus);
    void complexBookingOperation(Long bookingId);

    void testRollbackScenario(Long bookingId);
}