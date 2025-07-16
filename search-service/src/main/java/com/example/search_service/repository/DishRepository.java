package com.example.search_service.repository;

import com.example.search_service.model.DishEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<DishEntity, Long> {
    // Tìm kiếm theo category
    List<DishEntity> findByCategory(String category);

    // Tìm kiếm theo khoảng giá
    List<DishEntity> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Tìm kiếm theo tên (contains)
    List<DishEntity> findByNameContainingIgnoreCase(String name);
}
