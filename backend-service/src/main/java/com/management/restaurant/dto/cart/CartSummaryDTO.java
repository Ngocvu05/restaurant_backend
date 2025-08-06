package com.management.restaurant.dto.cart;

import java.math.BigDecimal;

public class CartSummaryDTO {
    private Long userId;
    private long totalItems;
    private long totalQuantity;
    private BigDecimal totalAmount;

    // Builder pattern
    public static CartSummaryDTOBuilder builder() {
        return new CartSummaryDTOBuilder();
    }

    public static class CartSummaryDTOBuilder {
        private Long userId;
        private long totalItems;
        private long totalQuantity;
        private BigDecimal totalAmount;

        public CartSummaryDTOBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public CartSummaryDTOBuilder totalItems(long totalItems) {
            this.totalItems = totalItems;
            return this;
        }

        public CartSummaryDTOBuilder totalQuantity(long totalQuantity) {
            this.totalQuantity = totalQuantity;
            return this;
        }

        public CartSummaryDTOBuilder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public CartSummaryDTO build() {
            CartSummaryDTO dto = new CartSummaryDTO();
            dto.userId = this.userId;
            dto.totalItems = this.totalItems;
            dto.totalQuantity = this.totalQuantity;
            dto.totalAmount = this.totalAmount;
            return dto;
        }
    }

    // Getters
    public Long getUserId() { return userId; }
    public long getTotalItems() { return totalItems; }
    public long getTotalQuantity() { return totalQuantity; }
    public BigDecimal getTotalAmount() { return totalAmount; }

}
