package com.management.search_service.events;

public interface EventPublisherService {
    void publishDishEvent(String routingKey, BaseEvent event);
    void publishUserEvent(String routingKey, BaseEvent event);
    void publishReviewEvent(String routingKey, BaseEvent event);
}
