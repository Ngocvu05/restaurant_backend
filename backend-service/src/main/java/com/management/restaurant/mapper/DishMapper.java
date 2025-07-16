package com.management.restaurant.mapper;

import com.management.restaurant.admin.dto.DishAdminDTO;
import com.management.restaurant.dto.DishDTO;
import com.management.restaurant.model.Dish;
import org.mapstruct.Mapping;

public interface DishMapper {
    @Mapping(target = "imageUrls",
            expression = "java(dish.getImages() != null ? dish.getImages().stream().map(Image::getUrl).collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>())"
    )
    DishDTO toDTO(Dish dish);

    @Mapping(target = "images", ignore = true)     // handled in service
    @Mapping(target = "bookings", ignore = true)
        // usually handled elsewhere
    Dish toEntity(DishDTO dishDTO);

    DishAdminDTO toAdminDTO(Dish dish);
}
