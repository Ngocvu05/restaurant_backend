package com.management.restaurant.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long userId;
    private Long dishId;
    private int quantity;
    private String dishName;
    private String imageUrl;
    private BigDecimal unitPrice;
}
