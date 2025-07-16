package com.management.restaurant.admin.controller;

import com.management.restaurant.admin.dto.DishAdminDTO;
import com.management.restaurant.admin.service.AdminDishService;
import com.management.restaurant.admin.service.NotificationService;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.Image;
import com.management.restaurant.repository.DishRepository;
import com.management.restaurant.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/dishes")
@RequiredArgsConstructor
public class AdminDishController {

    private final AdminDishService dishService;
    private final DishRepository dishRepository;
    private final ImageRepository imageRepository;
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
    public ResponseEntity<Dish> create(@RequestBody DishAdminDTO dto) {
        Dish dish = new Dish();
        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setPrice(dto.getPrice());
        dish.setCategory(dto.getCategory());
        dish.setIsAvailable(true);

        Dish savedDish = dishRepository.save(dish);
        associateImagesWithDish(dto.getImageUrls(), savedDish);

        notificationService.notifyAllAdmins("Thêm món ăn", "Admin đã thêm món mới: " + dish.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDish);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DishAdminDTO> update(@PathVariable Long id, @RequestBody DishAdminDTO dto) {
        DishAdminDTO updated = dishService.update(id, dto);

        // Gán lại ảnh nếu có
        dishRepository.findById(id).ifPresent(dish -> {
            associateImagesWithDish(dto.getImageUrls(), dish);
        });

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        dishService.delete(id);
        return ResponseEntity.ok("Deleted");
    }

    private void associateImagesWithDish(List<String> imageUrls, Dish dish) {
        if (imageUrls == null || imageUrls.isEmpty()) return;

        List<Image> imagesToUpdate = imageUrls.stream()
                .map(imageRepository::findByUrl)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(image -> image.setDish(dish))
                .toList();

        imageRepository.saveAll(imagesToUpdate);
    }
}
