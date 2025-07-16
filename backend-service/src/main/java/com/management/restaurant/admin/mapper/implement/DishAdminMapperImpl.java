package com.management.restaurant.admin.mapper.implement;

import com.management.restaurant.admin.dto.DishAdminDTO;
import com.management.restaurant.admin.mapper.DishAdminMapper;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.Image;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class DishAdminMapperImpl implements DishAdminMapper {
    public DishAdminDTO toDTO(Dish dish) {
        return DishAdminDTO.builder()
                .id(dish.getId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(dish.getPrice())
                .isAvailable(dish.getIsAvailable())
                .category(dish.getCategory())
                .imageUrls(dish.getImages() != null
                        ? dish.getImages().stream().map(Image::getUrl).collect(Collectors.toList())
                        : Collections.emptyList())
                .orderCount(dish.getOrderCount())
                .createdAt(dish.getCreatedAt())
                .build();
    }
}
