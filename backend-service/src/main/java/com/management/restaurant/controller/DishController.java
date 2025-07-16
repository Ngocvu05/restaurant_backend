package com.management.restaurant.controller;

import com.management.restaurant.dto.DishDTO;
import com.management.restaurant.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;

    @GetMapping
    public ResponseEntity<List<DishDTO>> getAll() {
        return ResponseEntity.ok(dishService.getAllDishes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DishDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dishService.getDishById(id));
    }

    @PostMapping
    public ResponseEntity<DishDTO> create(@RequestBody DishDTO dishDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dishService.createDish(dishDTO));
    }

    @PutMapping("/update{id}")
    public ResponseEntity<DishDTO> update(@PathVariable Long id, @RequestBody DishDTO dishDTO) {
        return ResponseEntity.ok(dishService.updateDish(id, dishDTO));
    }

    @DeleteMapping("/delete{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dishService.deleteDish(id);
        return ResponseEntity.noContent().build();
    }
}
