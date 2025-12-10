package com.management.restaurant.analytics.service;

import com.management.restaurant.contains.BookingStatus;
import com.management.restaurant.model.Booking;

public interface BookingPropagationService {
    Booking updateBooking(Long bookingId, BookingStatus newStatus);
    void logBookingAction(Long bookingId, String action);
    void validateBookingInTransaction(Booking booking);
    Booking getBooking(Long bookingId);
    void sendEmailNotification(Long bookingId, String emailContent);
    void performNonTransactionalTask(String taskName);
    void addOrderToBooking(Long bookingId, Long dishId, int quantity);
    void complexBookingOperation(Long bookingId);
    void testRollbackScenario(Long bookingId);
}
