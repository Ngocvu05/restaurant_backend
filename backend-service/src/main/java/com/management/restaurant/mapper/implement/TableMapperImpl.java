package com.management.restaurant.mapper.implement;

import com.management.restaurant.contains.TableStatus;
import com.management.restaurant.dto.TableDTO;
import com.management.restaurant.mapper.TableMapper;
import com.management.restaurant.model.TableEntity;
import org.springframework.stereotype.Component;

@Component
public class TableMapperImpl implements TableMapper {
    @Override
    public TableDTO toDTO(TableEntity table) {
        if (table == null) return null;

        return TableDTO.builder()
                .id(table.getId())
                .tableName(table.getTableName())
                .capacity(table.getCapacity())
                .status(table.getStatus() != null ? table.getStatus().name() : null)
                .description(table.getDescription())
                .build();
    }

    @Override
    public TableEntity toEntity(TableDTO dto) {
        if (dto == null) return null;

        return TableEntity.builder()
                .tableName(dto.getTableName())
                .capacity(dto.getCapacity())
                .status(dto.getStatus() != null ? TableStatus.valueOf(dto.getStatus()) : null)
                .description(dto.getDescription())
                .build();
    }
}