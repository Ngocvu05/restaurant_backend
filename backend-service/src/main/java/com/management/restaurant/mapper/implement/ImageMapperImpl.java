package com.management.restaurant.mapper.implement;

import com.management.restaurant.dto.ImageDTO;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.mapper.ImageMapper;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.Image;
import com.management.restaurant.model.User;
import com.management.restaurant.repository.DishRepository;
import com.management.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageMapperImpl implements ImageMapper {
    private final UserRepository userRepository;
    private final DishRepository dishRepository;

    public ImageDTO toDTO(Image image) {
        if (image == null) return null;

        ImageDTO dto = new ImageDTO();
        dto.setId(image.getId());
        dto.setUrl(image.getUrl());
        dto.setUploadedAt(image.getUploadedAt());
        dto.setUserId(image.getUser() != null ? image.getUser().getId() : null);
        dto.setDishId(image.getDish() != null ? image.getDish().getId() : null);

        return dto;
    }

    public Image toEntity(ImageDTO dto) {
        if (dto == null) return null;

        Image image = new Image();
        image.setId(dto.getId());
        image.setUrl(dto.getUrl());
        image.setUploadedAt(dto.getUploadedAt());

        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + dto.getUserId()));
            image.setUser(user);
        }

        if (dto.getDishId() != null) {
            Dish dish = dishRepository.findById(dto.getDishId())
                    .orElseThrow(() -> new NotFoundException("Dish not found with id: " + dto.getDishId()));
            image.setDish(dish);
        }

        return image;
    }
}