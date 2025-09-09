package com.management.restaurant.service.implement;

import com.management.restaurant.dto.cart.CartDTO;
import com.management.restaurant.dto.cart.CartItemDTO;
import com.management.restaurant.dto.cart.CartSummaryDTO;
import com.management.restaurant.exception.ResourceNotFoundException;
import com.management.restaurant.mapper.CartMapper;
import com.management.restaurant.model.Cart;
import com.management.restaurant.model.Dish;
import com.management.restaurant.repository.CartRepository;
import com.management.restaurant.repository.DishRepository;
import com.management.restaurant.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final DishRepository dishRepository;
    private final CartMapper cartMapper;

    /**
     * Add item to cart
     */
    @Override
    public CartItemDTO addToCart(Long userId, Long dishId, int quantity) {
        log.info("Adding dish {} to cart for user {}", dishId, userId);

        // Validate dish exists and is available
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + dishId));

        if (!dish.getIsAvailable()) {
            throw new IllegalArgumentException("Dish is not available");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Check if item already exists in cart
        Optional<Cart> existingCart = cartRepository.findByUserIdAndDishId(userId, dishId);

        Cart cart;
        if (existingCart.isPresent()) {
            // Update quantity
            cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + quantity);
        } else {
            // Create new cart item
            cart = Cart.builder()
                    .userId(userId)
                    .dishId(dishId)
                    .quantity(quantity)
                    .build();
        }

        cart = cartRepository.save(cart);

        // Fetch cart with dish details
        cart = cartRepository.findByUserIdAndDishId(userId, dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        return cartMapper.toCartItemDTO(cart);
    }

    /**
     * Get user's cart
     */
    @Override
    @Transactional(readOnly = true)
    public CartDTO getCart(Long userId) {
        log.info("Getting cart for user {}", userId);

        List<Cart> cartItems = cartRepository.findByUserIdWithDish(userId);

        return cartMapper.toCartDTO(cartItems);
    }

    /**
     * Update cart item quantity
     */
    public CartItemDTO updateCartItem(Long userId, Long dishId, int quantity) {
        log.info("Updating cart item for user {} dish {} to quantity {}", userId, dishId, quantity);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Cart cart = cartRepository.findByUserIdAndDishId(userId, dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cart.setQuantity(quantity);
        cart = cartRepository.save(cart);

        return cartMapper.toCartItemDTO(cart);
    }

    /**
     * Remove item from cart
     */
    @Override
    public void removeFromCart(Long userId, Long dishId) {
        log.info("Removing dish {} from cart for user {}", dishId, userId);

        Cart cart = cartRepository.findByUserIdAndDishId(userId, dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cartRepository.delete(cart);
    }

    /**
     * Clear entire cart
     */
    @Override
    public void clearCart(Long userId) {
        log.info("Clearing cart for user {}", userId);

        cartRepository.deleteByUserId(userId);
    }

    /**
     * Get cart summary
     */
    @Transactional(readOnly = true)
    @Override
    public CartSummaryDTO getCartSummary(Long userId) {
        log.info("Getting cart summary for user {}", userId);

        List<Cart> cartItems = cartRepository.findByUserIdWithDish(userId);

        BigDecimal totalAmount = cartItems.stream()
                .filter(cart -> cart.getDish() != null)
                .map(cart -> cart.getDish().getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalItems = cartItems.size();
        long totalQuantity = cartItems.stream()
                .mapToLong(Cart::getQuantity)
                .sum();

        return CartSummaryDTO.builder()
                .userId(userId)
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .build();
    }

    /**
     * Validate cart before checkout
     */
    @Override
    @Transactional(readOnly = true)
    public boolean validateCart(Long userId) {
        log.info("Validating cart for user {}", userId);

        List<Cart> cartItems = cartRepository.findByUserIdWithDish(userId);

        if (cartItems.isEmpty()) {
            return false;
        }

        // Check if all dishes are still available
        return cartItems.stream()
                .allMatch(cart -> cart.getDish() != null && cart.getDish().getIsAvailable());
    }
}