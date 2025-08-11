package com.management.restaurant.admin.mapper.implement;

import com.management.restaurant.admin.dto.PaymentConfirmationDTO;
import com.management.restaurant.admin.mapper.AdminPaymentMapper;
import com.management.restaurant.common.PaymentMethod;
import com.management.restaurant.common.PaymentStatus;
import com.management.restaurant.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class AdminPaymentMapperImpl implements AdminPaymentMapper {
    @Override
    public PaymentConfirmationDTO toDTO(Payment paymentConfirmation) {
        if (paymentConfirmation == null) {
            return null;
        }
        return PaymentConfirmationDTO.builder()
                .id(paymentConfirmation.getId())
                .amount(paymentConfirmation.getAmount())
                .adminNote(paymentConfirmation.getAdminNote())
                .paymentMethod(String.valueOf(paymentConfirmation.getPaymentMethod()))
                .status(String.valueOf(paymentConfirmation.getStatus()))
                .processedAt(paymentConfirmation.getProcessedAt())
                .customerNote(paymentConfirmation.getCustomerNote())
                .processedBy(paymentConfirmation.getProcessedBy())
                .customerName(paymentConfirmation.getCustomerNote())
                .transactionReference(String.valueOf(paymentConfirmation.getTransactionReference()))
                .build();
    }

    @Override
    public Payment toEntity(PaymentConfirmationDTO dto) {
        if (dto == null) {
            return null;
        }
        return Payment.builder()
                .id(dto.getId())
                .amount(dto.getAmount())
                .adminNote(dto.getAdminNote())
                .paymentMethod(PaymentMethod.valueOf(dto.getPaymentMethod()))
                .processedAt(dto.getProcessedAt())
                .customerNote(dto.getCustomerNote())
                .processedBy(dto.getProcessedBy())
                .customerNote(dto.getCustomerNote())
                .transactionReference(dto.getTransactionReference())
                .status(PaymentStatus.valueOf(dto.getStatus()))
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
