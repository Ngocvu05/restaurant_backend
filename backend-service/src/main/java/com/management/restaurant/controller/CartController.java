package com.management.restaurant.controller;

import com.management.restaurant.dto.cart.*;
import com.management.restaurant.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Cart Management", description = "APIs for managing shopping cart")
public class CartController {
    private final CartService cartService;

    @Operation(summary = "Add item to cart", description = "Add a dish to user's cart with specified quantity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item added to cart successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Dish not found")
    })
    @PostMapping("/add")
    public ResponseEntity<DataResponses<CartItemDTO>> addToCart(
            @Valid @RequestBody AddToCartRequest request) {

        log.info("Request to add dish {} to cart for user {}", request.getDishId(), request.getUserId());

        CartItemDTO cartItem = cartService.addToCart(
                request.getUserId(),
                request.getDishId(),
                request.getQuantity()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DataResponses.success("Item added to cart successfully", cartItem));
    }

    @Operation(summary = "Get user's cart", description = "Retrieve all items in user's cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<DataResponses<CartDTO>> getCart(
            @Parameter(description = "User ID")
            @PathVariable @NotNull Long userId) {

        log.info("Request to get cart for user {}", userId);

        CartDTO cart = cartService.getCart(userId);

        return ResponseEntity.ok(DataResponses.success("Cart retrieved successfully", cart));
    }

    @Operation(summary = "Update cart item", description = "Update quantity of an item in cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @PutMapping("/update")
    public ResponseEntity<DataResponses<CartItemDTO>> updateCartItem(
            @Valid @RequestBody UpdateCartItemRequest request) {

        log.info("Request to update cart item for user {} dish {} to quantity {}",
                request.getUserId(), request.getDishId(), request.getQuantity());

        CartItemDTO cartItem = cartService.updateCartItem(
                request.getUserId(),
                request.getDishId(),
                request.getQuantity()
        );

        return ResponseEntity.ok(DataResponses.success("Cart item updated successfully", cartItem));
    }

    @Operation(summary = "Remove item from cart", description = "Remove a specific item from user's cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removed from cart successfully"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @DeleteMapping("/remove")
    public ResponseEntity<DataResponses<Void>> removeFromCart(
            @Parameter(description = "User ID") @RequestParam @NotNull Long userId,
            @Parameter(description = "Dish ID") @RequestParam @NotNull Long dishId) {

        log.info("Request to remove dish {} from cart for user {}", dishId, userId);

        cartService.removeFromCart(userId, dishId);

        return ResponseEntity.ok(DataResponses.success("Item removed from cart successfully", null));
    }

    @Operation(summary = "Clear cart", description = "Remove all items from user's cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart cleared successfully")
    })
    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<DataResponses<Void>> clearCart(
            @Parameter(description = "User ID")
            @PathVariable @NotNull Long userId) {

        log.info("Request to clear cart for user {}", userId);

        cartService.clearCart(userId);

        return ResponseEntity.ok(DataResponses.success("Cart cleared successfully", null));
    }

    @Operation(summary = "Get cart summary", description = "Get cart summary with totals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart summary retrieved successfully")
    })
    @GetMapping("/summary/{userId}")
    public ResponseEntity<DataResponses<CartSummaryDTO>> getCartSummary(
            @Parameter(description = "User ID")
            @PathVariable @NotNull Long userId) {

        log.info("Request to get cart summary for user {}", userId);

        CartSummaryDTO summary = cartService.getCartSummary(userId);

        return ResponseEntity.ok(DataResponses.success("Cart summary retrieved successfully", summary));
    }

    @Operation(summary = "Validate cart", description = "Validate cart items before checkout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart validation completed")
    })
    @GetMapping("/validate/{userId}")
    public ResponseEntity<DataResponses<Boolean>> validateCart(
            @Parameter(description = "User ID")
            @PathVariable @NotNull Long userId) {

        log.info("Request to validate cart for user {}", userId);

        boolean isValid = cartService.validateCart(userId);

        String message = isValid ? "Cart is valid" : "Cart validation failed";
        return ResponseEntity.ok(DataResponses.success(message, isValid));
    }

}