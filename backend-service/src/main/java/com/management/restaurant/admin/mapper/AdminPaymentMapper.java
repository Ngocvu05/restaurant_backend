package com.management.restaurant.admin.mapper;

import com.management.restaurant.admin.dto.PaymentConfirmationDTO;
import com.management.restaurant.model.Payment;

public interface AdminPaymentMapper {
    PaymentConfirmationDTO toDTO(Payment paymentConfirmation);

    Payment toEntity(PaymentConfirmationDTO dto);
}
