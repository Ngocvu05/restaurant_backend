package com.management.restaurant.service.payments.implement;

import com.management.restaurant.common.PaymentMethod;
import com.management.restaurant.dto.PaymentDTO;
import com.management.restaurant.service.payments.MoMoPaymentService;
import com.management.restaurant.service.payments.UnifiedPaymentService;
import com.management.restaurant.service.payments.VNPayPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedPaymentServiceImpl implements UnifiedPaymentService {
    private final MoMoPaymentService moMoPaymentService;
    private final VNPayPaymentService vnPayPaymentService;

    @Override
    public Map<String, Object> createPayment(String paymentMethod, Long bookingId, BigDecimal amount, String orderInfo) {
        try {
            log.info("Creating payment with method: {} for booking: {}", paymentMethod, bookingId);

            return switch (paymentMethod.toUpperCase()) {
                case "MOMO" -> moMoPaymentService.createPaymentRequest(bookingId, amount, orderInfo);
                case "VNPAY" -> vnPayPaymentService.createPaymentRequest(bookingId, amount, orderInfo);
                default -> throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
            };

        } catch (Exception e) {
            log.error("Error creating payment with method: {}", paymentMethod, e);
            throw new RuntimeException("Failed to create payment: " + e.getMessage());
        }
    }

    @Override
    public PaymentDTO handleCallback(String paymentMethod, Map<String, Object> callbackData) {
        try {
            log.info("Handling callback for payment method: {}", paymentMethod);

            return switch (paymentMethod.toUpperCase()) {
                case "MOMO" -> moMoPaymentService.handlePaymentCallback(callbackData);
                case "VNPAY" -> {
                    // Convert Map<String, Object> to Map<String, String> for VNPay
                    Map<String, String> vnpayParams = callbackData.entrySet().stream()
                            .collect(HashMap::new,
                                    (map, entry) -> map.put(entry.getKey(), String.valueOf(entry.getValue())),
                                    HashMap::putAll);
                    yield vnPayPaymentService.handlePaymentReturn(vnpayParams);
                }
                default -> throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
            };

        } catch (Exception e) {
            log.error("Error handling callback for payment method: {}", paymentMethod, e);
            throw new RuntimeException("Failed to handle callback: " + e.getMessage());
        }
    }

    @Override
    public PaymentDTO getPaymentStatus(Long paymentId) {
        try {
            // Có thể dùng chung cho cả MoMo và VNPay vì đều query từ database
            return moMoPaymentService.getPaymentStatus(paymentId);
        } catch (Exception e) {
            log.error("Error getting payment status for id: {}", paymentId, e);
            throw new RuntimeException("Failed to get payment status: " + e.getMessage());
        }
    }

    @Override
    public PaymentMethod getPaymentMethodEnum(String paymentMethod) {
        return switch (paymentMethod.toUpperCase()) {
            case "MOMO" -> PaymentMethod.MOMO;
            case "VNPAY" -> PaymentMethod.VNPAY;
            case "CASH" -> PaymentMethod.CASH;
            case "CARD" -> PaymentMethod.CARD;
            default -> throw new IllegalArgumentException("Unknown payment method: " + paymentMethod);
        };
    }

    @Override
    public boolean isPaymentMethodSupported(String paymentMethod) {
        return switch (paymentMethod.toUpperCase()) {
            case "MOMO", "VNPAY", "CASH", "CARD" -> true;
            default -> false;
        };
    }
}
