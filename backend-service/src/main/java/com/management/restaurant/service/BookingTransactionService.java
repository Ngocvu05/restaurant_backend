package com.management.restaurant.service;

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
}
