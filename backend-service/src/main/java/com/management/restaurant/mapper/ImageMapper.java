package com.management.restaurant.mapper;

import com.management.restaurant.dto.ImageDTO;
import com.management.restaurant.model.Image;

public interface ImageMapper {
    ImageDTO toDTO(Image image);

    Image toEntity(ImageDTO dto);
}