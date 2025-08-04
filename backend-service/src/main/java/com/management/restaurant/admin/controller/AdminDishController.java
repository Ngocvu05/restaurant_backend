package com.management.restaurant.admin.controller;

import com.management.restaurant.admin.dto.DishAdminDTO;
import com.management.restaurant.admin.service.AdminDishService;
import com.management.restaurant.admin.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dishes")
@RequiredArgsConstructor
public class AdminDishController {
    private final AdminDishService dishService;
    private final NotificationService notificationService;

    @GetMapping
    public List<DishAdminDTO> getAll() {
        return dishService.getAll();
    }

    @GetMapping("/{id}")
    public DishAdminDTO getById(@PathVariable Long id) {
        return dishService.getById(id);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody DishAdminDTO dto) {
        dishService.create(dto);
        notificationService.notifyAllAdmins("Thêm món ăn", "Admin đã thêm món mới: " + dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body("Created new dish");
    }

    @PutMapping("/{id}")
    public ResponseEntity<DishAdminDTO> update(@PathVariable Long id, @RequestBody DishAdminDTO dto) {
        DishAdminDTO updated = dishService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        dishService.delete(id);
        return ResponseEntity.ok("Deleted");
    }


}
