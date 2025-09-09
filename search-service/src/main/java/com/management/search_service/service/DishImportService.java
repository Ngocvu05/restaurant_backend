package com.management.search_service.service;

import com.management.search_service.model.Dish;
import com.management.search_service.model.DishEntity;
import com.management.search_service.repository.DishRepository;
import com.management.search_service.repository.DishSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishImportService {

    private final DishRepository dishJpaRepository;
    private final DishSearchRepository dishSearchRepository;

    @Autowired
    public DishImportService(DishRepository dishJpaRepository,
                             DishSearchRepository dishSearchRepository) {
        this.dishJpaRepository = dishJpaRepository;
        this.dishSearchRepository = dishSearchRepository;
    }

    /**
     * Synchronize all data from the database to Elasticsearch.
     */
    public void syncAllDishes() {
        log.info("Starting full sync of dishes to Elasticsearch");

        // Fetch all dishes từ database
        List<DishEntity> dishEntities = dishJpaRepository.findAll();

        // Convert sang Elasticsearch documents
        List<Dish> dishes = dishEntities.stream()
                .map(Dish::fromEntity)
                .collect(Collectors.toList());

        // Delete all old indexes and recreate them.
        dishSearchRepository.deleteAll();

        // Storage Elasticsearch
        dishSearchRepository.saveAll(dishes);

        log.info("Synced {} dishes to Elasticsearch", dishes.size());
    }

    /**
     * Synchronize a specific dish.
     */
    public void syncDish(Long dishId) {
        log.info("Syncing dish with ID: {}", dishId);

        dishJpaRepository.findById(dishId)
                .ifPresentOrElse(
                        entity -> {
                            Dish dish = Dish.fromEntity(entity);
                            dishSearchRepository.save(dish);
                            log.info("Synced dish: {}", dish.getName());
                        },
                        () -> {
                            // If not found in the database, delete it from Elasticsearch.
                            dishSearchRepository.deleteById(String.valueOf(dishId));
                            log.info("Deleted dish with ID: {} from Elasticsearch", dishId);
                        }
                );
    }

    /**
     * Synchronize by category.
     */
    public void syncDishesByCategory(String category) {
        log.info("Syncing dishes by category: {}", category);

        List<DishEntity> dishEntities = dishJpaRepository.findByCategory(category);
        List<Dish> dishes = dishEntities.stream()
                .map(Dish::fromEntity)
                .collect(Collectors.toList());

        dishSearchRepository.saveAll(dishes);

        log.info("Synced {} dishes for category: {}", dishes.size(), category);
    }

    /**
     * Delete a dish from Elasticsearch.
     */
    public void deleteDishFromIndex(Long dishId) {
        dishSearchRepository.deleteById(String.valueOf(dishId));
        log.info("Deleted dish with ID: {} from Elasticsearch", dishId);
    }
}