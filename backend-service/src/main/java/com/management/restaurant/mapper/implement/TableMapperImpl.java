package com.management.restaurant.mapper.implement;

import com.management.restaurant.common.TableStatus;
import com.management.restaurant.dto.TableDTO;
import com.management.restaurant.mapper.TableMapper;
import com.management.restaurant.model.TableEntity;
import com.management.restaurant.service.TableService;
import org.springframework.stereotype.Component;

@Component
public class TableMapperImpl implements TableMapper {
    @Override
    public TableDTO toDTO(TableEntity table) {
        if (table == null) return null;

        TableDTO dto = new TableDTO();
        dto.setId(table.getId());
        dto.setTableName(table.getTableName());
        dto.setCapacity(table.getCapacity());
        dto.setStatus(table.getStatus() != null ? table.getStatus().name() : null);
        dto.setDescription(table.getDescription());
        return dto;
    }

    @Override
    public TableEntity toEntity(TableDTO dto) {
        if (dto == null) return null;

        TableEntity entity = new TableEntity();
        entity.setId(dto.getId());
        entity.setTableName(dto.getTableName());
        entity.setCapacity(dto.getCapacity());
        entity.setStatus(dto.getStatus() != null ? TableStatus.valueOf(dto.getStatus()) : null);
        entity.setDescription(dto.getDescription());
        return entity;
    }
}
