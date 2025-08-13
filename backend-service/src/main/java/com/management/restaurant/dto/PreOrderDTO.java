package com.management.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreOrderDTO {
    private Long id;
    private Long bookingId;
    private Long dishId;
    private int quantity;
    private String note;
}