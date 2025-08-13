package com.management.search_service.listener;

import com.management.search_service.config.RabbitMQConfig;
import com.management.search_service.events.implement.UserEvent;
import com.management.search_service.service.UserSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {
    private final UserSearchService userSearchService;

    @RabbitListener(queues = RabbitMQConfig.USER_SEARCH_QUEUE)
    public void handleUserEvent(UserEvent event) {
        try {
            log.info("Received user event: {} for user ID: {}", event.getEventType(), event.getUserId());

            switch (UserEvent.Type.valueOf(event.getEventType())) {
                case USER_CREATED:
                case USER_UPDATED:
                case USER_STATUS_CHANGED:
                    userSearchService.indexUser(event);
                    break;
                case USER_DELETED:
                    userSearchService.deleteUser(event.getUserId());
                    break;
                default:
                    log.warn("Unknown user event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing user event: {}", e.getMessage(), e);
            throw e;
        }
    }
}