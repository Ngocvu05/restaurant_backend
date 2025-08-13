package com.management.search_service.repository;

import com.management.search_service.model.DishEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<DishEntity, Long> {
    List<DishEntity> findByCategory(String category);

    List<DishEntity> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<DishEntity> findByNameContainingIgnoreCase(String name);
}