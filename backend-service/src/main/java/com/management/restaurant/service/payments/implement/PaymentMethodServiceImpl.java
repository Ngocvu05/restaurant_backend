package com.management.restaurant.service.payments.implement;

import com.management.restaurant.common.PaymentMethod;
import com.management.restaurant.common.PaymentStatus;
import com.management.restaurant.dto.PaymentDTO;
import com.management.restaurant.mapper.PaymentMapper;
import com.management.restaurant.model.Payment;
import com.management.restaurant.repository.PaymentRepository;
import com.management.restaurant.service.payments.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodServiceImpl implements PaymentMethodService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public List<PaymentDTO> getPaymentsByBookingId(Long bookingId) {
        List<Payment> payments = paymentRepository.findByBookingId(bookingId);

        return payments.stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalPaidAmount(Long bookingId) {
        List<Payment> completedPayments = paymentRepository.findByBookingId(bookingId)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .toList();

        return completedPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean isBookingFullyPaid(Long bookingId, BigDecimal totalAmount) {
        BigDecimal paidAmount = getTotalPaidAmount(bookingId);
        return paidAmount.compareTo(totalAmount) >= 0;
    }

    @Override
    public PaymentDTO createDirectPayment(Long bookingId, BigDecimal amount, PaymentMethod paymentMethod) {
        try {
            log.info("Creating direct payment for booking: {}, method: {}", bookingId, paymentMethod);

            // Create payment record và return DTO
            Map<String, Object> result = Map.of(
                    "message", "Direct payment created successfully",
                    "paymentMethod", paymentMethod.toString(),
                    "amount", amount,
                    "status", "COMPLETED"
            );

            return PaymentDTO.builder()
                    .bookingId(bookingId)
                    .amount(amount)
                    .paymentMethod(paymentMethod)
                    .status(PaymentStatus.SUCCESS)
                    .paymentTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error creating direct payment", e);
            throw new RuntimeException("Failed to create direct payment: " + e.getMessage());
        }
    }

    @Override
    public boolean cancelPendingPayment(Long paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.CANCELLED);
                paymentRepository.save(payment);
                log.info("Payment cancelled successfully: {}", paymentId);
                return true;
            } else {
                log.warn("Cannot cancel payment with status: {}", payment.getStatus());
                return false;
            }

        } catch (Exception e) {
            log.error("Error cancelling payment: {}", paymentId, e);
            return false;
        }
    }
}
