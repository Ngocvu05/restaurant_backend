package com.management.search_service.controller;

import com.management.search_service.document.DishDocument;
import com.management.search_service.model.Dish;
import com.management.search_service.service.DishImportService;
import com.management.search_service.service.DishSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/dishes")
public class DishSearchController {
    private final DishSearchService dishSearchService;
    private final DishImportService dishSyncService;

    @Autowired
    public DishSearchController(DishSearchService dishSearchService,
                                DishImportService dishSyncService) {
        this.dishSearchService = dishSearchService;
        this.dishSyncService = dishSyncService;
    }

    // === SEARCH ENDPOINTS ===

    @GetMapping("/search")
    public ResponseEntity<List<Dish>> searchByName(@RequestParam String keyword) {
        List<Dish> dishes = dishSearchService.searchByName(keyword);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<List<Dish>> searchAdvanced(@RequestParam String keyword) {
        List<Dish> dishes = dishSearchService.searchAdvanced(keyword);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/search/category")
    public ResponseEntity<List<Dish>> searchByCategory(@RequestParam String category) {
        List<Dish> dishes = dishSearchService.searchByCategory(category);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/search/price")
    public ResponseEntity<List<Dish>> searchByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<Dish> dishes = dishSearchService.searchByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/search/filters")
    public ResponseEntity<List<Dish>> searchWithFilters(
            @RequestParam String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<Dish> dishes = dishSearchService.searchWithFilters(keyword, category, minPrice, maxPrice);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/search/suggestions")
    public ResponseEntity<List<Dish>> searchSuggestions(@RequestParam String keyword) {
        List<Dish> dishes = dishSearchService.searchSuggestions(keyword);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Dish>> getAllDishes() {
        List<Dish> dishes = dishSearchService.getAllDishes();
        return ResponseEntity.ok(dishes);
    }

    // === SYNC ENDPOINTS ===

    @PostMapping("/sync/all")
    public ResponseEntity<String> syncAllDishes() {
        dishSyncService.syncAllDishes();
        return ResponseEntity.ok("All dishes synced successfully");
    }

    @PostMapping("/sync/{dishId}")
    public ResponseEntity<String> syncDish(@PathVariable Long dishId) {
        dishSyncService.syncDish(dishId);
        return ResponseEntity.ok("Dish synced successfully");
    }

    @PostMapping("/sync/category")
    public ResponseEntity<String> syncDishesByCategory(@RequestParam String category) {
        dishSyncService.syncDishesByCategory(category);
        return ResponseEntity.ok("Dishes synced by category successfully");
    }

    @DeleteMapping("/sync/{dishId}")
    public ResponseEntity<String> deleteDishFromIndex(@PathVariable Long dishId) {
        dishSyncService.deleteDishFromIndex(dishId);
        return ResponseEntity.ok("Dish deleted from index successfully");
    }

    @GetMapping
    public ResponseEntity<Page<DishDocument>> searchDishes(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<DishDocument> dishes = dishSearchService.searchDishes(query, pageable);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<DishDocument>> findByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "averageRating"));
        Page<DishDocument> dishes = dishSearchService.findByCategory(category, pageable);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/available")
    public ResponseEntity<Page<DishDocument>> findAvailableDishes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderCount"));
        Page<DishDocument> dishes = dishSearchService.findAvailableDishes(pageable);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/price-range")
    public ResponseEntity<Page<DishDocument>> findByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "price"));
        Page<DishDocument> dishes = dishSearchService.findByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<DishDocument>> findHighRatedDishes(
            @RequestParam(defaultValue = "4.0") BigDecimal minRating) {

        List<DishDocument> dishes = dishSearchService.findHighRatedDishes(minRating);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DishDocument> findById(@PathVariable Long id) {
        Optional<DishDocument> dish = dishSearchService.findById(id);
        return dish.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}