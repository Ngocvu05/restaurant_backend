package com.management.search_service.repository;

import com.management.search_service.model.Dish;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DishSearchRepository extends ElasticsearchRepository<Dish, String> {

    List<Dish> findByNameContainingIgnoreCase(String name);

    List<Dish> findByCategory(String category);

    List<Dish> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Find by Name or Description
    List<Dish> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description);
}
