package com.management.restaurant.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Dish ID is required")
    private Long dishId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    // Getters and setters
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getDishId() {
        return dishId;
    }
    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}