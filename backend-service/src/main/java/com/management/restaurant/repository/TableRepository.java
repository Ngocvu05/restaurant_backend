package com.management.restaurant.repository;

import com.management.restaurant.common.TableStatus;
import com.management.restaurant.model.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Long> {
    List<TableEntity> findByStatus(TableStatus status);

    List<TableEntity> findByCapacityGreaterThanEqual(int capacity);
}