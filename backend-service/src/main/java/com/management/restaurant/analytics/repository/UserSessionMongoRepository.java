package com.management.restaurant.analytics.repository;

import com.management.restaurant.analytics.model.UserSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionMongoRepository extends MongoRepository<UserSession, String> {
    Optional<UserSession> findBySessionId(String sessionId);

    List<UserSession> findByUserId(Long userId);

    List<UserSession> findByUserIdAndIsActiveTrue(Long userId);

    long deleteByLastActivityBefore(LocalDateTime cutoffDate);

    @Query("{ 'userId': ?0, 'loginTime': { $gte: ?1, $lte: ?2 } }")
    List<UserSession> findUserSessionsInRange(
            Long userId, LocalDateTime start, LocalDateTime end
    );
}