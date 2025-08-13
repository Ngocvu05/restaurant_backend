package com.management.search_service.listener;

import com.management.search_service.config.RabbitMQConfig;
import com.management.search_service.events.implement.ReviewEvent;
import com.management.search_service.service.ReviewSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventListener {
    private final ReviewSearchService reviewSearchService;

    @RabbitListener(queues = RabbitMQConfig.REVIEW_SEARCH_QUEUE)
    public void handleReviewEvent(ReviewEvent event) {
        try {
            log.info("Received review event: {} for review ID: {}", event.getEventType(), event.getReviewId());

            switch (ReviewEvent.Type.valueOf(event.getEventType())) {
                case REVIEW_CREATED:
                case REVIEW_UPDATED:
                case REVIEW_STATUS_CHANGED:
                    reviewSearchService.indexReview(event);
                    break;
                case REVIEW_DELETED:
                    reviewSearchService.deleteReview(event.getReviewId());
                    break;
                default:
                    log.warn("Unknown review event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing review event: {}", e.getMessage(), e);
            throw e;
        }
    }
}