package com.management.restaurant.mapper;

import com.management.restaurant.dto.PaymentDTO;
import com.management.restaurant.model.Payment;

public interface PaymentMapper {
    PaymentDTO toDTO(Payment payment);

    Payment toEntity(PaymentDTO dto);
}