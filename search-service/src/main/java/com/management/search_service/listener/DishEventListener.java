package com.management.search_service.listener;

import com.management.search_service.config.RabbitMQConfig;
import com.management.search_service.events.implement.DishEvent;
import com.management.search_service.service.DishSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DishEventListener {
    private final DishSearchService dishSearchService;

    @RabbitListener(queues = RabbitMQConfig.DISH_SEARCH_QUEUE)
    public void handleDishEvent(DishEvent event) {
        try {
            log.info("Received dish event: {} for dish ID: {}", event.getEventType(), event.getDishId());

            switch (DishEvent.Type.valueOf(event.getEventType())) {
                case DISH_CREATED:
                case DISH_UPDATED:
                case DISH_AVAILABILITY_CHANGED:
                case DISH_RATING_UPDATED:
                    dishSearchService.indexDish(event);
                    break;
                case DISH_DELETED:
                    dishSearchService.deleteDish(event.getDishId());
                    break;
                default:
                    log.warn("Unknown dish event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing dish event: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger retry mechanism
        }
    }
}
