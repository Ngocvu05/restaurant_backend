package com.management.restaurant.admin.service;

import com.management.restaurant.admin.dto.AdminConfirmationRequest;
import com.management.restaurant.admin.dto.PaymentConfirmationDTO;
import com.management.restaurant.common.PaymentStatus;

import java.util.List;

public interface AdminPaymentService {

    List<PaymentConfirmationDTO> getAllPendingConfirmations();

    PaymentConfirmationDTO getByBookingId(Long bookingId);

    PaymentConfirmationDTO createConfirmationRequest(PaymentConfirmationDTO dto);

    PaymentConfirmationDTO confirmPayment(Long id, AdminConfirmationRequest request);

    PaymentConfirmationDTO rejectPayment(Long id, String adminNote);

    List<PaymentConfirmationDTO> getConfirmationsByStatus(PaymentStatus status);

    List<PaymentConfirmationDTO> getConfirmationsByBookingId(Long bookingId);
}