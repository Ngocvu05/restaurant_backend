package com.management.restaurant.admin.service.implement;

import com.management.restaurant.admin.dto.AdminConfirmationRequest;
import com.management.restaurant.admin.dto.PaymentConfirmationDTO;
import com.management.restaurant.admin.mapper.AdminPaymentMapper;
import com.management.restaurant.common.BookingStatus;
import com.management.restaurant.repository.PaymentConfirmationRepository;
import com.management.restaurant.admin.service.AdminPaymentService;
import com.management.restaurant.common.PaymentStatus;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.model.Booking;
import com.management.restaurant.model.Payment;
import com.management.restaurant.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminPaymentServiceImpl implements AdminPaymentService {
    private final PaymentConfirmationRepository paymentConfirmationRepository;
    private final BookingRepository bookingRepository;
    private final AdminPaymentMapper paymentConfirmationMapper;

    @Override
    public List<PaymentConfirmationDTO> getAllPendingConfirmations() {
        return paymentConfirmationRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.PENDING)
                .stream()
                .map(paymentConfirmationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentConfirmationDTO getByBookingId(Long bookingId) {
        Payment confirmation = paymentConfirmationRepository
                .findByBooking_IdAndStatus(bookingId, PaymentStatus.PENDING)
                .orElseThrow(() -> new NotFoundException("Payment confirmation not found for booking: " + bookingId));

        return paymentConfirmationMapper.toDTO(confirmation);
    }

    /**
     * THÊM METHOD MỚI - Lấy danh sách payment confirmations theo booking ID
     */
    @Override
    public List<PaymentConfirmationDTO> getConfirmationsByBookingId(Long bookingId) {
        // Kiểm tra booking có tồn tại không
        if (!bookingRepository.existsById(bookingId)) {
            throw new NotFoundException("Booking not found: " + bookingId);
        }

        // Lấy tất cả payment confirmations cho booking này (không chỉ PENDING)
        List<Payment> confirmations = paymentConfirmationRepository.findByBooking_IdOrderByCreatedAtDesc(bookingId);

        return confirmations.stream()
                .map(paymentConfirmationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentConfirmationDTO createConfirmationRequest(PaymentConfirmationDTO dto) {
        // Kiểm tra booking tồn tại
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new NotFoundException("Booking not found: " + dto.getBookingId()));

        // Kiểm tra xem đã có yêu cầu pending nào chưa
        if (paymentConfirmationRepository.existsByBookingIdAndStatus(dto.getBookingId(), "PENDING")) {
            throw new IllegalStateException("Đã có yêu cầu xác nhận thanh toán đang chờ xử lý cho booking này");
        }

        Payment confirmation = paymentConfirmationMapper.toEntity(dto);
        confirmation.setId(null);
        confirmation.setBooking(booking);
        confirmation.setStatus(PaymentStatus.PENDING);
        confirmation.setCreatedAt(LocalDateTime.now());

        // Auto-generate transaction reference
        if (confirmation.getTransactionReference() == null || confirmation.getTransactionReference().isEmpty()) {
            confirmation.setTransactionReference("MANUAL_" + booking.getId() + "_" + System.currentTimeMillis());
        }

        Payment saved = paymentConfirmationRepository.save(confirmation);
        return paymentConfirmationMapper.toDTO(saved);
    }

    @Override
    public PaymentConfirmationDTO confirmPayment(Long id, AdminConfirmationRequest request) {
        Payment confirmation = paymentConfirmationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment confirmation not found: " + id));

        if (!PaymentStatus.PENDING.equals(confirmation.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xác nhận các thanh toán đang ở trạng thái PENDING");
        }

        confirmation.setStatus(PaymentStatus.SUCCESS);
        confirmation.setAdminNote(request.getAdminNote());
        confirmation.setCreatedAt(LocalDateTime.now());
        confirmation.setProcessedBy(request.getProcessedBy());
        confirmation.setProcessedAt(LocalDateTime.now());

        // update booking status
        Booking booking = confirmation.getBooking();
        if (booking != null && "PENDING".equals(booking.getStatus().toString())) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }

        Payment saved = paymentConfirmationRepository.save(confirmation);
        return paymentConfirmationMapper.toDTO(saved);
    }

    @Override
    public PaymentConfirmationDTO rejectPayment(Long id, String adminNote) {
        Payment confirmation = paymentConfirmationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment confirmation not found: " + id));

        if (!PaymentStatus.PENDING.equals(confirmation.getStatus())) {
            throw new IllegalStateException("Chỉ có thể từ chối các thanh toán đang ở trạng thái PENDING");
        }

        confirmation.setStatus(PaymentStatus.REJECTED);
        confirmation.setAdminNote(adminNote);
        confirmation.setProcessedAt(LocalDateTime.now());

        Payment saved = paymentConfirmationRepository.save(confirmation);
        return paymentConfirmationMapper.toDTO(saved);
    }

    @Override
    public List<PaymentConfirmationDTO> getConfirmationsByStatus(PaymentStatus status) {
        return paymentConfirmationRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.PENDING)
                .stream()
                .map(paymentConfirmationMapper::toDTO)
                .collect(Collectors.toList());
    }
}