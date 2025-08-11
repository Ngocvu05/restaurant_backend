package com.management.restaurant.admin.controller;

import com.management.restaurant.admin.dto.AdminConfirmationRequest;
import com.management.restaurant.admin.dto.PaymentConfirmationDTO;
import com.management.restaurant.admin.dto.PaymentConfirmationRequest;
import com.management.restaurant.admin.service.AdminPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@Slf4j
public class AdminPaymentController {
    private final AdminPaymentService paymentConfirmationService;

    /**
     * Lấy tất cả yêu cầu xác nhận đang chờ
     */
    @GetMapping("/pending")
    public ResponseEntity<List<PaymentConfirmationDTO>> getPendingConfirmations() {
        log.info("Getting all pending payment confirmations");
        List<PaymentConfirmationDTO> confirmations = paymentConfirmationService.getAllPendingConfirmations();
        return ResponseEntity.ok(confirmations);
    }

    /**
     * Lấy yêu cầu xác nhận theo trạng thái
     */
    @GetMapping
    public ResponseEntity<List<PaymentConfirmationDTO>> getConfirmationsByStatus(
            @RequestParam(defaultValue = "PENDING") String status) {
        log.info("Getting payment confirmations by status: {}", status);
        List<PaymentConfirmationDTO> confirmations = paymentConfirmationService.getConfirmationsByStatus(status);
        return ResponseEntity.ok(confirmations);
    }

    /**
     * Lấy yêu cầu xác nhận theo booking ID
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentConfirmationDTO> getByBookingId(@PathVariable Long bookingId) {
        log.info("Getting payment confirmation for booking: {}", bookingId);
        PaymentConfirmationDTO confirmation = paymentConfirmationService.getByBookingId(bookingId);
        return ResponseEntity.ok(confirmation);
    }

    /**
     * Tạo yêu cầu xác nhận thanh toán (từ phía user)
     */
    @PostMapping
    public ResponseEntity<PaymentConfirmationDTO> createConfirmationRequest(
            @Valid @RequestBody PaymentConfirmationRequest request) {
        log.info("Creating payment confirmation request for booking: {}", request.getBookingId());

        PaymentConfirmationDTO dto = PaymentConfirmationDTO.builder()
                .bookingId(request.getBookingId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(request.getTransactionReference())
                .customerNote(request.getCustomerNote())
                .status("PENDING")
                .build();

        PaymentConfirmationDTO created = paymentConfirmationService.createConfirmationRequest(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * Admin xác nhận thanh toán
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<PaymentConfirmationDTO> confirmPayment(
            @PathVariable Long id,
            @Valid @RequestBody AdminConfirmationRequest request) {
        log.info("Admin confirming payment: {} with note: {}", id, request.getAdminNote());

        PaymentConfirmationDTO confirmed = paymentConfirmationService.confirmPayment(id, request.getAdminNote());
        return ResponseEntity.ok(confirmed);
    }

    /**
     * Admin từ chối thanh toán
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<PaymentConfirmationDTO> rejectPayment(
            @PathVariable Long id,
            @Valid @RequestBody AdminConfirmationRequest request) {
        log.info("Admin rejecting payment: {} with note: {}", id, request.getAdminNote());

        PaymentConfirmationDTO rejected = paymentConfirmationService.rejectPayment(id, request.getAdminNote());
        return ResponseEntity.ok(rejected);
    }

    /**
     * Thống kê trạng thái xác nhận
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getConfirmationStats() {
        log.info("Getting payment confirmation statistics");

        List<PaymentConfirmationDTO> pending = paymentConfirmationService.getConfirmationsByStatus("PENDING");
        List<PaymentConfirmationDTO> confirmed = paymentConfirmationService.getConfirmationsByStatus("CONFIRMED");
        List<PaymentConfirmationDTO> rejected = paymentConfirmationService.getConfirmationsByStatus("REJECTED");

        Map<String, Object> stats = Map.of(
                "pending", pending.size(),
                "confirmed", confirmed.size(),
                "rejected", rejected.size(),
                "total", pending.size() + confirmed.size() + rejected.size()
        );

        return ResponseEntity.ok(stats);
    }
}