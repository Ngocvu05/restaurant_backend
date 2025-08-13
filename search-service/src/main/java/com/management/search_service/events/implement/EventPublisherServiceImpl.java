package com.management.search_service.events.implement;

import com.management.search_service.config.RabbitMQConfig;
import com.management.search_service.events.BaseEvent;
import com.management.search_service.events.EventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherServiceImpl implements EventPublisherService {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishDishEvent(String routingKey, BaseEvent event) {
        try {
            enrichEvent(event, "user-service");
            rabbitTemplate.convertAndSend(RabbitMQConfig.DISH_EXCHANGE, routingKey, event);
            log.info("Published dish event: {} with routing key: {}", event.getEventType(), routingKey);
        } catch (Exception e) {
            log.error("Failed to publish dish event: {}", e.getMessage(), e);
        }
    }

    @Override
    public void publishUserEvent(String routingKey, BaseEvent event) {
        try {
            enrichEvent(event, "user-service");
            rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EXCHANGE, routingKey, event);
            log.info("Published user event: {} with routing key: {}", event.getEventType(), routingKey);
        } catch (Exception e) {
            log.error("Failed to publish user event: {}", e.getMessage(), e);
        }
    }

    @Override
    public void publishReviewEvent(String routingKey, BaseEvent event) {
        try {
            enrichEvent(event, "user-service");
            rabbitTemplate.convertAndSend(RabbitMQConfig.REVIEW_EXCHANGE, routingKey, event);
            log.info("Published review event: {} with routing key: {}", event.getEventType(), routingKey);
        } catch (Exception e) {
            log.error("Failed to publish review event: {}", e.getMessage(), e);
        }
    }

    private void enrichEvent(BaseEvent event, String source) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        if (event.getSource() == null) {
            event.setSource(source);
        }
    }
}