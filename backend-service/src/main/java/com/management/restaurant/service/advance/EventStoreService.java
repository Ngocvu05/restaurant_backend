package com.management.restaurant.service.advance;

import com.management.restaurant.model.event.DomainEvent;
import com.management.restaurant.repository.EventStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventStoreService {
    private final EventStoreRepository eventRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    /**
     * Save event to event store
     */
    public void saveEvent(String aggregateId, String aggregateType,
                          String eventType, Object eventData, String userId) {
        try {
            Long version = eventRepository.countByAggregateId(aggregateId) + 1;

            String eventDataJson = objectMapper.writeValueAsString(eventData);

            DomainEvent event = DomainEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateId(aggregateId)
                    .aggregateType(aggregateType)
                    .eventType(eventType)
                    .eventData(eventDataJson)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .version(version)
                    .build();

            eventRepository.save(event);
            log.info("Event saved: {} for aggregate {}", eventType, aggregateId);

        } catch (Exception e) {
            log.error("Failed to save event", e);
            throw new RuntimeException("Event save failed", e);
        }
    }

    /**
     * Get all events for an aggregate (entity)
     */
    public List<DomainEvent> getEventsForAggregate(String aggregateId) {
        return eventRepository.findByAggregateIdOrderByVersionAsc(aggregateId);
    }

    /**
     * Rebuild entity state from events (Event Replay)
     */
    public <T> T rebuildState(String aggregateId, Class<T> stateClass) {
        List<DomainEvent> events = getEventsForAggregate(aggregateId);

        if (events.isEmpty()) {
            return null;
        }

        // Apply events sequentially to rebuild state
        // This is simplified - in production use proper event handlers
        log.info("Rebuilding state for aggregate {} from {} events",
                aggregateId, events.size());

        return null; // Implement based on your aggregate
    }

    /**
     * Get event history (audit trail)
     */
    public List<DomainEvent> getAuditTrail(String aggregateId) {
        return getEventsForAggregate(aggregateId);
    }

    /**
     * Time travel: Get state at specific point in time
     */
    public List<DomainEvent> getEventsUntil(String aggregateId, LocalDateTime pointInTime) {
        return getEventsForAggregate(aggregateId).stream()
                .filter(e -> e.getTimestamp().isBefore(pointInTime) ||
                        e.getTimestamp().isEqual(pointInTime))
                .toList();
    }
}