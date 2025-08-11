package com.management.restaurant.service.payments;

import com.management.restaurant.dto.PaymentDTO;

import java.math.BigDecimal;
import java.util.Map;

public interface MoMoPaymentService {
    Map<String, Object> createPaymentRequest(Long bookingId, BigDecimal amount, String orderInfo);
    PaymentDTO handlePaymentCallback(Map<String, Object> callbackData);
    PaymentDTO getPaymentStatus(Long paymentId);
}
