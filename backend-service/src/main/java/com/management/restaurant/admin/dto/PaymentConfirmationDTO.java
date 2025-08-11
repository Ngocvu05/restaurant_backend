package com.management.restaurant.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmationDTO {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionReference;
    private String customerNote;
    private String adminNote;
    private String status; // PENDING, CONFIRMED, REJECTED
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String processedBy;
    // Thông tin booking liên quan (để hiển thị)
    private String customerName;
    private Integer tableNumber;
    private LocalDateTime bookingTime;
    private String bookingStatus;
}
