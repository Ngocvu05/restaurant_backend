package com.example.search_service.repository;

import com.example.search_service.model.Dish;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DishSearchRepository extends ElasticsearchRepository<Dish, String> {
    // Tìm kiếm theo tên
    List<Dish> findByNameContainingIgnoreCase(String name);

    // Tìm kiếm theo category
    List<Dish> findByCategory(String category);

    // Tìm kiếm theo khoảng giá
    List<Dish> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Tìm kiếm theo tên hoặc description
    List<Dish> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description);
}
