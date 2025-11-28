package com.management.search_service.repository;

import com.management.search_service.model.Dish;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DishSearchRepository extends ElasticsearchRepository<Dish, String> {
    @Query("{\"match\": {\"name\": {\"query\": \"?0\", \"operator\": \"and\"}}}")
    List<Dish> findByNameContainingIgnoreCase(String name);

    // Alternative: Use multi_match for better results
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\", \"description\"], \"type\": \"best_fields\", \"fuzziness\": \"AUTO\"}}")
    List<Dish> searchByNameOrDescription(String keyword);

    List<Dish> findByCategory(String category);

    List<Dish> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Find by Name or Description
    List<Dish> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description);
}