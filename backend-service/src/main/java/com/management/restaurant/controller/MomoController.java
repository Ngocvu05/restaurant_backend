package com.management.restaurant.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments/momo")
@RequiredArgsConstructor
@Slf4j
public class MomoController {
    /*private final MoMoPaymentService moMoPaymentService;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody CreatePaymentRequest request) {
        try {
            log.info("Creating MoMo payment for booking: {}, amount: {}",
                    request.getBookingId(), request.getAmount());

            String orderInfo = request.getOrderInfo() != null ?
                    request.getOrderInfo() :
                    "Thanh toán đặt bàn #" + request.getBookingId();

            Map<String, Object> paymentData = moMoPaymentService.createPaymentRequest(
                    request.getBookingId(),
                    request.getAmount(),
                    orderInfo
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment request created successfully",
                    "data", paymentData
            ));

        } catch (Exception e) {
            log.error("Error creating MoMo payment", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    *//**
     * Process callback from MoMo (IPN - Instant Payment Notification)
     *//*
    @PostMapping("/callback")
    public ResponseEntity<?> handleCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            log.info("Received MoMo callback: {}", callbackData);

            PaymentDTO paymentResult = moMoPaymentService.handlePaymentCallback(callbackData);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Callback processed successfully",
                    "data", paymentResult
            ));

        } catch (Exception e) {
            log.error("Error processing MoMo callback", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    *//**
     * Process return URL from MoMo
     *//*
    @GetMapping("/return")
    public ResponseEntity<?> handleReturn(
            @RequestParam Map<String, Object> params) {
        try {
            log.info("Received MoMo return: {}", params);

            PaymentDTO paymentResult = moMoPaymentService.handlePaymentCallback(params);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment processed successfully",
                    "data", paymentResult
            ));

        } catch (Exception e) {
            log.error("Error processing MoMo return", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    *//**
     * Checking Payment status
     *//*
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long paymentId) {
        try {
            PaymentDTO payment = moMoPaymentService.getPaymentStatus(paymentId);

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
    }*/
}