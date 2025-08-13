package com.management.restaurant.mapper.implement;

import com.management.restaurant.admin.dto.DishAdminDTO;
import com.management.restaurant.dto.DishDTO;
import com.management.restaurant.mapper.DishMapper;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.Image;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DishMapperImpl implements DishMapper {
    // Convert Dish entity to DTO
    public DishDTO toDTO(Dish dish) {
        if (dish == null) return null;

        List<String> imageUrls = dish.getImages() != null
                ? dish.getImages().stream()
                .map(Image::getUrl)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return DishDTO.builder()
                .id(dish.getId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(dish.getPrice())
                .available(true)
                .category(dish.getCategory())
                .imageUrls(imageUrls)
                .orderCount(dish.getOrderCount())
                .build();
    }

    // Convert DTO to Dish entity (without handling images/bookings)
    public Dish toEntity(DishDTO dto) {
        if (dto == null) return null;

        return Dish.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .isAvailable(true)
                .category(dto.getCategory())
                .orderCount(dto.getOrderCount())
                .build();
    }

    @Override
    public DishAdminDTO toAdminDTO(Dish dish) {
        if (dish == null) return null;

        DishAdminDTO dto = new DishAdminDTO();
        dto.setId(dish.getId());
        dto.setName(dish.getName());
        dto.setPrice(dish.getPrice());
        dto.setDescription(dish.getDescription());
        dto.setCategory(dish.getCategory());

        // Map image URLs
        if (dish.getImages() != null) {
            List<String> imageUrls = dish.getImages()
                    .stream()
                    .map(Image::getUrl)
                    .collect(Collectors.toList());
            dto.setImageUrls(imageUrls);
        }
        return dto;
    }
}