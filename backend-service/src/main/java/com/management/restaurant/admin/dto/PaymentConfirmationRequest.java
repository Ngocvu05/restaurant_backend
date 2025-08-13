package com.management.restaurant.admin.dto;

import com.management.restaurant.common.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmationRequest {
    @NotNull(message = "Booking ID must not be empty")
    private Long bookingId;

    @NotNull(message = "Amount must not be empty")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    @Builder.Default
    private String paymentMethod = String.valueOf(PaymentMethod.CASH);

    @Size(max = 100, message = "Transaction code must not exceed 100 characters")
    private String transactionReference;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String customerNote;
}
