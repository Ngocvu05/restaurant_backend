package com.management.restaurant.repository;

import com.management.restaurant.event.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'PENDING' " +
            "AND (o.nextRetryAt IS NULL OR o.nextRetryAt <= :now) " +
            "AND o.retryCount < o.maxRetries " +
            "ORDER BY o.createdAt ASC")
    List<OutboxEvent> findPendingEvents(@Param("now") LocalDateTime now);

    List<OutboxEvent> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'FAILED' " +
            "AND o.createdAt > :since ORDER BY o.createdAt DESC")
    List<OutboxEvent> findRecentFailedEvents(@Param("since") LocalDateTime since);

    long countByStatus(String status);

    List<OutboxEvent> findByAggregateTypeAndAggregateIdOrderByCreatedAtDesc(
            String aggregateType, Long aggregateId
    );

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'PUBLISHED' " +
            "AND o.publishedAt < :before")
    List<OutboxEvent> findOldPublishedEvents(@Param("before") LocalDateTime before);
}