package com.management.restaurant.service.implement;

import com.management.restaurant.dto.TableDTO;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.mapper.TableMapper;
import com.management.restaurant.model.TableEntity;
import com.management.restaurant.repository.TableRepository;
import com.management.restaurant.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {
    private final TableRepository tableRepository;
    private final TableMapper tableMapper;

    @Override
    public List<TableDTO> getAllTables() {
        return tableRepository.findAll().stream()
                .map(tableMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TableDTO getTableById(Long id) {
        TableEntity entity = tableRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Table not found"));
        return tableMapper.toDTO(entity);
    }

    @Override
    public TableDTO createTable(TableDTO tableDTO) {
        TableEntity entity = tableMapper.toEntity(tableDTO);
        return tableMapper.toDTO(tableRepository.save(entity));
    }

    @Override
    public TableDTO updateTable(Long id, TableDTO tableDTO) {
        TableEntity existing = tableRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Table not found"));
        existing.setTableName(tableDTO.getTableName());
        existing.setCapacity(tableDTO.getCapacity());
        return tableMapper.toDTO(tableRepository.save(existing));
    }

    @Override
    public void deleteTable(Long id) {
        tableRepository.deleteById(id);
    }
}
