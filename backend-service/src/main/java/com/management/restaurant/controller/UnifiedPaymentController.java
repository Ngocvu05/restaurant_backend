package com.management.restaurant.controller;

import com.management.restaurant.dto.PaymentDTO;
import com.management.restaurant.dto.payments.CreatePaymentRequest;
import com.management.restaurant.service.payments.UnifiedPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class UnifiedPaymentController {
    private final UnifiedPaymentService unifiedPaymentService;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody CreatePaymentRequest request) {
        try {
            log.info("Creating payment: method={}, bookingId={}, amount={}",
                    request.getPaymentMethod(), request.getBookingId(), request.getAmount());

            // Validate payment method
            if (!unifiedPaymentService.isPaymentMethodSupported(request.getPaymentMethod())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Unsupported payment method: " + request.getPaymentMethod()
                ));
            }

            String orderInfo = request.getOrderInfo() != null ?
                    request.getOrderInfo() :
                    "Thanh toan dat ban #" + request.getBookingId();

            Map<String, Object> paymentData = unifiedPaymentService.createPayment(
                    request.getPaymentMethod(),
                    request.getBookingId(),
                    request.getAmount(),
                    orderInfo
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment request created successfully",
                    "paymentMethod", request.getPaymentMethod(),
                    "data", paymentData
            ));

        } catch (Exception e) {
            log.error("Error creating payment", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Xử lý callback/return chung cho tất cả payment methods
     */
    @PostMapping("/{paymentMethod}/callback")
    public ResponseEntity<?> handleCallback(
            @PathVariable String paymentMethod,
            @RequestBody Map<String, Object> callbackData) {
        try {
            log.info("Handling callback for payment method: {}", paymentMethod);

            PaymentDTO paymentResult = unifiedPaymentService.handleCallback(paymentMethod, callbackData);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Callback processed successfully",
                    "paymentMethod", paymentMethod,
                    "data", paymentResult
            ));

        } catch (Exception e) {
            log.error("Error handling callback for payment method: {}", paymentMethod, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Xử lý return URL chung (đặc biệt cho VNPay)
     */
    @GetMapping("/{paymentMethod}/return")
    public ResponseEntity<?> handleReturn(
            @PathVariable String paymentMethod,
            HttpServletRequest request) {
        try {
            log.info("Handling return for payment method: {}", paymentMethod);

            // Extract all parameters
            Map<String, Object> params = new HashMap<>();
            Enumeration<String> parameterNames = request.getParameterNames();

            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String paramValue = request.getParameter(paramName);
                params.put(paramName, paramValue);
            }

            PaymentDTO paymentResult = unifiedPaymentService.handleCallback(paymentMethod, params);

            // Determine frontend redirect URL based on status
            String frontendRedirectUrl = switch (paymentResult.getStatus()) {
                case SUCCESS -> "/payment/success?paymentId=" + paymentResult.getId();
                case FAILED -> "/payment/failed?paymentId=" + paymentResult.getId();
                default -> "/payment/pending?paymentId=" + paymentResult.getId();
            };

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment processed successfully",
                    "paymentMethod", paymentMethod,
                    "data", paymentResult,
                    "redirectUrl", frontendRedirectUrl
            ));

        } catch (Exception e) {
            log.error("Error handling return for payment method: {}", paymentMethod, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "redirectUrl", "/payment/error"
            ));
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long paymentId) {
        try {
            PaymentDTO payment = unifiedPaymentService.getPaymentStatus(paymentId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", payment
            ));

        } catch (Exception e) {
            log.error("Error getting payment status", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Lấy danh sách payment methods được hỗ trợ
     */
    @GetMapping("/methods")
    public ResponseEntity<?> getSupportedPaymentMethods() {
        Map<String, Object> methods = Map.of(
                "MOMO", Map.of(
                        "name", "Ví MoMo",
                        "description", "Thanh toán qua ví điện tử MoMo",
                        "icon", "momo-icon.png",
                        "enabled", true
                ),
                "VNPAY", Map.of(
                        "name", "VNPay",
                        "description", "Thanh toán qua cổng VNPay (ATM, Visa, Master)",
                        "icon", "vnpay-icon.png",
                        "enabled", true
                ),
                "CASH", Map.of(
                        "name", "Tiền mặt",
                        "description", "Thanh toán bằng tiền mặt tại quầy",
                        "icon", "cash-icon.png",
                        "enabled", true
                ),
                "CARD", Map.of(
                        "name", "Thẻ tín dụng",
                        "description", "Thanh toán bằng thẻ tín dụng/ghi nợ",
                        "icon", "card-icon.png",
                        "enabled", false // Có thể enable sau
                )
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", methods
        ));
    }

    /**
     * Test tất cả payment methods
     */
    @GetMapping("/test/all")
    public ResponseEntity<?> testAllPaymentMethods() {
        Map<String, Object> results = new HashMap<>();

        try {
            // Test MoMo
            results.put("momo", Map.of(
                    "status", "available",
                    "testUrl", "/api/payments/momo/test"
            ));
        } catch (Exception e) {
            results.put("momo", Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }

        try {
            // Test VNPay
            results.put("vnpay", Map.of(
                    "status", "available",
                    "testUrl", "/api/payments/vnpay/test"
            ));
        } catch (Exception e) {
            results.put("vnpay", Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment methods test completed",
                "data", results
        ));
    }
}
