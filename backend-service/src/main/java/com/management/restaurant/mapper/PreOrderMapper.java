package com.management.restaurant.mapper;

import com.management.restaurant.dto.PreOrderDTO;
import com.management.restaurant.model.PreOrder;

public interface PreOrderMapper {
    PreOrderDTO toDTO(PreOrder preorder);

    PreOrder toEntity(PreOrderDTO dto);
}