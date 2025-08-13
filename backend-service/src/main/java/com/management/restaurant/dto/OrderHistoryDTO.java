package com.management.restaurant.dto;

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
public class OrderHistoryDTO {
    private Long id;
    private Long bookingId;
    private Long dishId;
    private int quantity;
    private Boolean served;
    private String note;
    private LocalDateTime createdAt;
    private Long userId;
    private BigDecimal totalAmount;
}