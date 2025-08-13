package com.management.restaurant.repository;

import com.management.restaurant.model.Dish;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findTop6ByOrderByCreatedAtDesc();

    List<Dish> findTop6ByOrderByOrderCountDesc();

    @Query("SELECT d FROM Dish d JOIN PreOrder p ON d.id = p.dish.id " +
            "GROUP BY d.id ORDER BY COUNT(p.id) DESC")
    List<Dish> findTop6PopularDishes(Pageable pageable);

    List<Dish> findByCategory(String category);

    List<Dish> findByIsAvailable(Boolean isAvailable);
}