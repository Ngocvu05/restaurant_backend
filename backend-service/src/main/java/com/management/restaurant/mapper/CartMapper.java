package com.management.restaurant.mapper;

import com.management.restaurant.dto.cart.CartDTO;
import com.management.restaurant.dto.cart.CartItemDTO;
import com.management.restaurant.model.Cart;
import com.management.restaurant.model.Dish;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

public interface CartMapper {
    // Map Cart entity to CartItemDTO
    @Mapping(target = "dishName", source = "dish.name")
    @Mapping(target = "unitPrice", source = "dish.price")
    @Mapping(target = "imageUrl", source = "dish", qualifiedByName = "getFirstImageUrl")
    CartItemDTO toCartItemDTO(Cart cart);

    // Map CartItemDTO to Cart entity
    @Mapping(target = "dish", ignore = true) // Will be set by service layer
    @Mapping(target = "createdAt", ignore = true) // Will use default value
    Cart toCartEntity(CartItemDTO cartItemDTO);

    // Map list of Cart entities to list of CartItemDTOs
    List<CartItemDTO> toCartItemDTOList(List<Cart> carts);

    // Map list of CartItemDTOs to list of Cart entities
    List<Cart> toCartEntityList(List<CartItemDTO> cartItemDTOs);

    // Helper method to get first image URL from dish
    @Named("getFirstImageUrl")
    default String getFirstImageUrl(Dish dish) {
        if (dish != null && dish.getImages() != null && !dish.getImages().isEmpty()) {
            return dish.getImages().get(0).getUrl();
        }
        return null;
    }

    // Group cart items by userId to create CartDTO
    default CartDTO toCartDTO(List<Cart> carts) {
        if (carts == null || carts.isEmpty()) {
            return null;
        }

        Cart firstCart = carts.get(0);
        List<CartItemDTO> items = toCartItemDTOList(carts);

        return CartDTO.builder()
                .userId(firstCart.getUserId())
                .createdAt(firstCart.getCreatedAt())
                .items(items)
                .build();
    }
}