package com.management.restaurant.mapper.implement;

import com.management.restaurant.dto.PaymentDTO;
import com.management.restaurant.mapper.PaymentMapper;
import com.management.restaurant.model.Booking;
import com.management.restaurant.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapperImpl implements PaymentMapper {
    @Override
    public PaymentDTO toDTO(Payment payment) {
        if (payment == null) return null;
        return PaymentDTO.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking() != null ? payment.getBooking().getId() : null)
                .amount(payment.getAmount())
                .status(payment.getStatus() != null ? payment.getStatus() : null)
                .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : null)
                .build();
    }

    @Override
    public Payment toEntity(PaymentDTO dto) {
        if (dto == null) return null;

        Payment payment = new Payment();
        payment.setId(dto.getId());
        payment.setAmount(dto.getAmount());
        payment.setStatus(dto.getStatus() != null ? dto.getStatus() : null);
        payment.setPaymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : null);
        if (dto.getBookingId() != null) {
            Booking booking = new Booking();
            booking.setId(dto.getBookingId());
            payment.setBooking(booking);
        }

        return payment;
    }
}