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
    @NotNull(message = "Booking ID không được để trống")
    private Long bookingId;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @Size(max = 50, message = "Phương thức thanh toán không được quá 50 ký tự")
    @Builder.Default
    private String paymentMethod = String.valueOf(PaymentMethod.CASH);

    @Size(max = 100, message = "Mã giao dịch không được quá 100 ký tự")
    private String transactionReference;

    @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
    private String customerNote;
}
