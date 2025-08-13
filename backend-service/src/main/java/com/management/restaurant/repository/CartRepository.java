package com.management.restaurant.repository;

import com.management.restaurant.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    List<Cart> findByUserId(Long userId);

    Optional<Cart> findByUserIdAndDishId(Long userId, Long dishId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.dish WHERE c.userId = :userId")
    List<Cart> findByUserIdWithDish(@Param("userId") Long userId);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndDishId(Long userId, Long dishId);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(c.quantity) FROM Cart c WHERE c.userId = :userId")
    Long getTotalQuantityByUserId(@Param("userId") Long userId);
}