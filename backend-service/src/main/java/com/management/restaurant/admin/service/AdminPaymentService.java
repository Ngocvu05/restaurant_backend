package com.management.restaurant.admin.service;

import com.management.restaurant.admin.dto.AdminConfirmationRequest;
import com.management.restaurant.admin.dto.PaymentConfirmationDTO;
import com.management.restaurant.common.PaymentStatus;

import java.util.List;

public interface AdminPaymentService {
    /**
     * Lấy tất cả các yêu cầu xác nhận thanh toán
     */
    List<PaymentConfirmationDTO> getAllPendingConfirmations();

    /**
     * Lấy yêu cầu xác nhận theo booking ID
     */
    PaymentConfirmationDTO getByBookingId(Long bookingId);

    /**
     * Tạo yêu cầu xác nhận thanh toán mới
     */
    PaymentConfirmationDTO createConfirmationRequest(PaymentConfirmationDTO dto);

    /**
     * Admin xác nhận thanh toán thành công
     */
    PaymentConfirmationDTO confirmPayment(Long id, AdminConfirmationRequest request);

    /**
     * Admin từ chối thanh toán
     */
    PaymentConfirmationDTO rejectPayment(Long id, String adminNote);

    /**
     * Lấy lịch sử xác nhận theo trạng thái
     */
    List<PaymentConfirmationDTO> getConfirmationsByStatus(PaymentStatus status);

    List<PaymentConfirmationDTO> getConfirmationsByBookingId(Long bookingId);
}