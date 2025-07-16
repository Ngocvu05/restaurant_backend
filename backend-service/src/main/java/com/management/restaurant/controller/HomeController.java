package com.management.restaurant.controller;

import com.management.restaurant.dto.DishDTO;
import com.management.restaurant.mapper.DishMapper;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.Image;
import com.management.restaurant.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;

    @GetMapping("/dishes")
    public ResponseEntity<List<DishDTO>> getAllDishesForPublic() {
        List<Dish> dishes = dishRepository.findAll();
        List<DishDTO> result = dishes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<DishDTO>> getFeaturedDishes() {
        List<Dish> featured = dishRepository.findTop6ByOrderByCreatedAtDesc();
        List<DishDTO> result = featured.stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private DishDTO toDTO(Dish dish) {
        return DishDTO.builder()
                .id(dish.getId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(dish.getPrice())
                .imageUrls(dish.getImages().stream().map(Image::getUrl).toList())
                .category(dish.getCategory())
                .orderCount(dish.getOrderCount())
                .available(true)
                .build();
    }

    // 🆕 Món mới nhất
    @GetMapping("/latest-dishes")
    public ResponseEntity<List<DishDTO>> getLatestDishes() {
        List<Dish> dishes = dishRepository.findTop6ByOrderByCreatedAtDesc();
        List<DishDTO> result = dishes.stream()
                .map(dishMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/popular-dishes")
    public ResponseEntity<List<DishDTO>> getPopularDishes() {
        List<Dish> popular = dishRepository.findTop6ByOrderByOrderCountDesc();
        List<DishDTO> result = popular.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
