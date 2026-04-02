package com.management.restaurant.repository;

import com.management.restaurant.model.event.DomainEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EventStoreRepository extends MongoRepository<DomainEvent, String> {
    List<DomainEvent> findByAggregateIdOrderByVersionAsc(String aggregateId);
    List<DomainEvent> findByAggregateTypeOrderByTimestampDesc(String aggregateType);
    List<DomainEvent> findByUserIdOrderByTimestampDesc(String userId);
    Long countByAggregateId(String aggregateId);
}