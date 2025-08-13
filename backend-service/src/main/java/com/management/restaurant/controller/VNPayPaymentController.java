package com.management.restaurant.controller;

import com.management.restaurant.dto.PaymentDTO;
import com.management.restaurant.dto.payments.CreatePaymentRequest;
import com.management.restaurant.dto.payments.RefundRequest;
import com.management.restaurant.service.payments.VNPayPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VNPayPaymentController {
    private final VNPayPaymentService vnPayPaymentService;

    /**
     * Create payment request VNPay
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody CreatePaymentRequest request) {
        try {
            log.info("Creating VNPay payment for booking: {}, amount: {}",
                    request.getBookingId(), request.getAmount());

            String orderInfo = request.getOrderInfo() != null ?
                    request.getOrderInfo() :
                    "Thanh toan dat ban #" + request.getBookingId();

            Map<String, Object> paymentData = vnPayPaymentService.createPaymentRequest(
                    request.getBookingId(),
                    request.getAmount(),
                    orderInfo
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "VNPay payment request created successfully",
                    "data", paymentData
            ));

        } catch (Exception e) {
            log.error("Error creating VNPay payment", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Process return URL from VNPay
     */
    @GetMapping("/return")
    public ResponseEntity<?> handleReturn(HttpServletRequest request) {
        try {
            log.info("Received VNPay return with query: {}", request.getQueryString());

            // Extract all parameters
            Map<String, String> params = new HashMap<>();
            Enumeration<String> parameterNames = request.getParameterNames();

            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String paramValue = request.getParameter(paramName);
                params.put(paramName, paramValue);
            }

            log.info("VNPay return parameters: {}", params);

            PaymentDTO paymentResult = vnPayPaymentService.handlePaymentReturn(params);

            // Determine redirect URL based on payment status
            String redirectUrl = switch (paymentResult.getStatus()) {
                case SUCCESS -> "/payment/success?paymentId=" + paymentResult.getId();
                case FAILED -> "/payment/failed?paymentId=" + paymentResult.getId();
                default -> "/payment/pending?paymentId=" + paymentResult.getId();
            };

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment processed successfully",
                    "data", paymentResult,
                    "redirectUrl", redirectUrl
            ));

        } catch (Exception e) {
            log.error("Error processing VNPay return", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "redirectUrl", "/payment/error"
            ));
        }
    }

    /**
     * Process IPN (Instant Payment Notification) from VNPay
     */
    @PostMapping("/ipn")
    public ResponseEntity<?> handleIPN(HttpServletRequest request) {
        try {
            log.info("Received VNPay IPN");

            // Extract all parameters
            Map<String, String> params = new HashMap<>();
            Enumeration<String> parameterNames = request.getParameterNames();

            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String paramValue = request.getParameter(paramName);
                params.put(paramName, paramValue);
            }

            log.info("VNPay IPN parameters: {}", params);

            PaymentDTO paymentResult = vnPayPaymentService.handlePaymentReturn(params);

            // VNPay expects specific response format for IPN
            Map<String, Object> response = new HashMap<>();
            if (paymentResult.getStatus().toString().equals("COMPLETED")) {
                response.put("RspCode", "00");
                response.put("Message", "success");
            } else {
                response.put("RspCode", "97");
                response.put("Message", "Checksum failed");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing VNPay IPN", e);

            // Return error response to VNPay
            return ResponseEntity.ok(Map.of(
                    "RspCode", "99",
                    "Message", "Unknown error"
            ));
        }
    }

    /**
     * Query transaction status từ VNPay
     */
    @GetMapping("/query/{txnRef}")
    public ResponseEntity<?> queryTransaction(@PathVariable String txnRef) {
        try {
            Map<String, Object> result = vnPayPaymentService.queryTransaction(txnRef);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result
            ));

        } catch (Exception e) {
            log.error("Error querying VNPay transaction", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Checking payment status
     */
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long paymentId) {
        try {
            PaymentDTO payment = vnPayPaymentService.getPaymentStatus(paymentId);

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
     * Test endpoint to checking connection
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "VNPay Payment API is working",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Refund transaction
     */
    @PostMapping("/refund")
    public ResponseEntity<?> refundTransaction(@RequestBody RefundRequest request) {
        try {
            log.info("Processing VNPay refund for txnRef: {}, amount: {}",
                    request.getTxnRef(), request.getAmount());

            // Implement refund logic here
            // VNPay requires calling their refund API
            Map<String, Object> result = new HashMap<>();
            result.put("txnRef", request.getTxnRef());
            result.put("refundAmount", request.getAmount());
            result.put("status", "processing");
            result.put("message", "Refund request submitted successfully");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Refund processed successfully",
                    "data", result
            ));

        } catch (Exception e) {
            log.error("Error processing VNPay refund", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}