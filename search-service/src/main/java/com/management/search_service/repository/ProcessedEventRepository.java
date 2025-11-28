package com.management.search_service.repository;

import com.management.search_service.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventId(String eventId);

    @Query("SELECT p FROM ProcessedEvent p WHERE p.processedAt > :since " +
            "ORDER BY p.processedAt DESC")
    List<ProcessedEvent> findRecentlyProcessed(@Param("since") LocalDateTime since);

    @Query("SELECT p.eventType, COUNT(p) FROM ProcessedEvent p GROUP BY p.eventType")
    List<Object[]> countByEventType();
}