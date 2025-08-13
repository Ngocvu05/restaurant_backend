package com.management.restaurant.dto.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    private Long bookingId;
    private BigDecimal amount;
    private String orderInfo;
    private String paymentMethod;
    private String customerEmail;
    private String customerPhone;
}