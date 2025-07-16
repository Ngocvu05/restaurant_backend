package com.management.restaurant.service;

import com.management.restaurant.model.PreOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface EmailService {
    void sendBookingConfirmation(String to, String fullName, String tableName,
                                 LocalDateTime bookingTime, int guests, List<PreOrder> preOrders, BigDecimal totalAmount);

}
