package com.management.restaurant.analytics.service;

public interface DistributedTransactionService {
    void completeBookingSequential(Long bookingId);
    void completeBookingWithSaga(Long bookingId);
    void completeBookingWith2PC(Long bookingId);
    void completeBookingEventual(Long bookingId);
    void completeBookingManual(Long bookingId);
    void demonstrateDistributedTransactions(Long bookingId);
}