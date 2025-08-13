package com.management.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long tableId;
    private LocalDateTime bookingTime;
    private int numberOfGuests;
    private String note;
    private String status;
    private int numberOfPeople;
    private List<PreOrderDTO> preOrderDishes;
    private BigDecimal totalAmount;
}