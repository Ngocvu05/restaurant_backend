package com.management.restaurant.service.payments;

import com.management.restaurant.dto.PaymentDTO;

import java.math.BigDecimal;
import java.util.Map;

public interface VNPayPaymentService {
    Map<String, Object> createPaymentRequest(Long bookingId, BigDecimal amount, String orderInfo);
    PaymentDTO handlePaymentReturn(Map<String, String> params);
    Map<String, Object> queryTransaction(String txnRef);
    PaymentDTO getPaymentStatus(Long paymentId);
}
