package com.management.restaurant.controller;

import com.management.restaurant.dto.PreOrderDTO;
import com.management.restaurant.service.PreOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/preorders")
@RequiredArgsConstructor
public class PreOrderController {
    private final PreOrderService preOrderService;

    @GetMapping
    public ResponseEntity<List<PreOrderDTO>> getAll() {
        return ResponseEntity.ok(preOrderService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PreOrderDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(preOrderService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PreOrderDTO> create(@RequestBody PreOrderDTO dto) {
        return ResponseEntity.ok(preOrderService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PreOrderDTO> update(@PathVariable Long id, @RequestBody PreOrderDTO dto) {
        return ResponseEntity.ok(preOrderService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        preOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}