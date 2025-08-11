package com.management.restaurant.service.payments;

import com.management.restaurant.common.PaymentMethod;
import com.management.restaurant.dto.PaymentDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentMethodService {
    List<PaymentDTO> getPaymentsByBookingId(Long bookingId);
    BigDecimal getTotalPaidAmount(Long bookingId);
    boolean isBookingFullyPaid(Long bookingId, BigDecimal totalAmount);
    PaymentDTO createDirectPayment(Long bookingId, BigDecimal amount, PaymentMethod paymentMethod);
    boolean cancelPendingPayment(Long paymentId);
}
