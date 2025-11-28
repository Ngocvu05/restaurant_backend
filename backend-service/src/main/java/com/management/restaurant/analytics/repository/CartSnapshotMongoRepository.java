package com.management.restaurant.analytics.repository;

import com.management.restaurant.analytics.model.CartSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartSnapshotMongoRepository extends MongoRepository<CartSnapshot,String> {
    List<CartSnapshot> findByUserId(Long userId);

    Optional<CartSnapshot> findTopByUserIdOrderBySnapshotTimeDesc(Long userId);

    long deleteBySnapshotTimeBefore( LocalDateTime cutoffDate);
}
