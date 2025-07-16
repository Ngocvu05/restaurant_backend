package com.management.restaurant.controller;

import com.management.restaurant.dto.TableDTO;
import com.management.restaurant.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class TableController {
    private final TableService tableService;

    @GetMapping
    public List<TableDTO> getAll() {
        return tableService.getAllTables();
    }

    @GetMapping("/{id}")
    public TableDTO getById(@PathVariable Long id) {
        return tableService.getTableById(id);
    }

    @PostMapping
    public TableDTO create(@RequestBody TableDTO dto) {
        return tableService.createTable(dto);
    }

    @PutMapping("/update{id}")
    public TableDTO update(@PathVariable Long id, @RequestBody TableDTO dto) {
        return tableService.updateTable(id, dto);
    }

    @DeleteMapping("/delete{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
