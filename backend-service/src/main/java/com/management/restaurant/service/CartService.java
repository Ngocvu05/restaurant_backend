package com.management.restaurant.service;

import com.management.restaurant.dto.cart.CartDTO;
import com.management.restaurant.dto.cart.CartItemDTO;
import com.management.restaurant.dto.cart.CartSummaryDTO;

public interface CartService {
    CartItemDTO addToCart(Long userId, Long dishId, int quantity);
    CartDTO getCart(Long userId);
    CartItemDTO updateCartItem(Long userId, Long dishId, int quantity);
    void removeFromCart(Long userId, Long dishId);
    void clearCart(Long userId);
    CartSummaryDTO getCartSummary(Long userId);
    boolean validateCart(Long userId);
}
