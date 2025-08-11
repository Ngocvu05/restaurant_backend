package com.management.restaurant.service.payments;

import com.management.restaurant.common.PaymentMethod;
import com.management.restaurant.dto.PaymentDTO;

import java.math.BigDecimal;
import java.util.Map;

public interface UnifiedPaymentService {
    Map<String, Object> createPayment(String paymentMethod, Long bookingId, BigDecimal amount, String orderInfo);
    PaymentDTO handleCallback(String paymentMethod, Map<String, Object> callbackData);
    PaymentDTO getPaymentStatus(Long paymentId);
    PaymentMethod getPaymentMethodEnum(String paymentMethod);
    boolean isPaymentMethodSupported(String paymentMethod);
}
