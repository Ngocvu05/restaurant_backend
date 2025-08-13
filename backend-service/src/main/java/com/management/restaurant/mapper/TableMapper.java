package com.management.restaurant.mapper;

import com.management.restaurant.dto.TableDTO;
import com.management.restaurant.model.TableEntity;

public interface TableMapper {
    TableDTO toDTO(TableEntity entity);

    TableEntity toEntity(TableDTO dto);
}