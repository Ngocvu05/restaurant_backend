package com.management.restaurant.mapper.implement;

import com.management.restaurant.dto.cart.CartDTO;
import com.management.restaurant.dto.cart.CartItemDTO;
import com.management.restaurant.mapper.CartMapper;
import com.management.restaurant.model.Cart;
import com.management.restaurant.model.Dish;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapperImpl implements CartMapper {
    @Override
    public CartItemDTO toCartItemDTO(Cart cart) {
        if (cart == null) {
            return null;
        }

        return CartItemDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .dishId(cart.getDishId())
                .quantity(cart.getQuantity())
                .dishName(cart.getDish() != null ? cart.getDish().getName() : null)
                .unitPrice(cart.getDish() != null ? cart.getDish().getPrice() : null)
                .imageUrl(getFirstImageUrl(cart.getDish()))
                .build();
    }

    @Override
    public Cart toCartEntity(CartItemDTO cartItemDTO) {
        if (cartItemDTO == null) {
            return null;
        }

        return Cart.builder()
                .id(cartItemDTO.getId())
                .userId(cartItemDTO.getUserId())
                .dishId(cartItemDTO.getDishId())
                .quantity(cartItemDTO.getQuantity())
                // dish will be set by service layer
                // createdAt will use default value from @Builder.Default
                .build();
    }

    @Override
    public List<CartItemDTO> toCartItemDTOList(List<Cart> carts) {
        if (carts == null) {
            return null;
        }

        return carts.stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Cart> toCartEntityList(List<CartItemDTO> cartItemDTOs) {
        if (cartItemDTOs == null) {
            return null;
        }

        return cartItemDTOs.stream()
                .map(this::toCartEntity)
                .collect(Collectors.toList());
    }

    @Override
    public String getFirstImageUrl(Dish dish) {
        if (dish != null && dish.getImages() != null && !dish.getImages().isEmpty()) {
            return dish.getImages().get(0).getUrl();
        }
        return null;
    }

    @Override
    public CartDTO toCartDTO(List<Cart> carts) {
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