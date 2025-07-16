package com.example.search_service.controller;

import com.example.search_service.model.Dish;
import com.example.search_service.service.DishImportService;
import com.example.search_service.service.DishSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
}