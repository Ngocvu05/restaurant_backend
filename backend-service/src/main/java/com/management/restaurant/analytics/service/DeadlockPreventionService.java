package com.management.restaurant.analytics.service;

import com.management.restaurant.model.Booking;
import com.management.restaurant.model.TableEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface DeadlockPreventionService {
    void bookMultipleTablesWithOrdering(List<Long> tableIds);
    Booking createBookingWithRetry(Long tableId, Long userId,
                                   LocalDateTime bookingTime,
                                   int numberOfGuests);
    Booking createBookingShortTransaction(Long tableId);
    TableEntity lockAndReserveTable(Long tableId);
    Booking createBookingRecord(TableEntity table, LocalDateTime bookingTime, int guests);
    void processOrderWithDeadlockHandling(Long bookingId, List<Long> dishIds);
    void updateDishPriceEfficient(Long dishId, BigDecimal newPrice);
    void processPaymentWithRollback(Long bookingId, BigDecimal amount);
    void processOrderWithSavepoint(Long bookingId, List<Long> dishIds);
    Booking createBookingNoRollbackOnValidation(Long tableId, int numberOfGuests);
    void processOrderStrictRollback(Long bookingId);
    void updateTwoDishes(Long dishId1, Long dishId2, BigDecimal priceChange);
    void simulateDeadlock();
}