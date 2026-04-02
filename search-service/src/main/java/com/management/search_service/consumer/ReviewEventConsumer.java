package com.management.search_service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.search_service.document.ReviewDocument;
import com.management.search_service.events.implement.ReviewEvent;
import com.management.search_service.model.ProcessedEvent;
import com.management.search_service.repository.ProcessedEventRepository;
import com.management.search_service.repository.ReviewDocumentRepository;
import com.management.search_service.service.EventMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventConsumer {
    private final ReviewDocumentRepository reviewDocumentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private final EventMetricsService eventMetricsService;

    @RabbitListener(queues = "review.search.queue")
    @Transactional
    public void onReviewEvent(
            @Payload String eventJson,
            @Header(value = "eventId", required = false) String eventId) {

        long startTime = System.currentTimeMillis();

        try {
            log.info("📨 Received review event: {}", eventId);

            ReviewEvent event = objectMapper.readValue(eventJson, ReviewEvent.class);
            String finalEventId = eventId != null ? eventId : event.getEventId();

            // Check idempotency
            if (processedEventRepository.existsByEventId(finalEventId)) {
                log.info("⚠️ Event already processed: {}", finalEventId);
                return;
            }

            // Process based on event type
            switch (event.getEventType()) {
                case "review.created" -> handleReviewCreated(event);
                case "review.updated" -> handleReviewUpdated(event);
                case "review.deleted" -> handleReviewDeleted(event);
                case "review.status.changed" -> handleReviewStatusChanged(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }

            // Mark as processed
            long duration = System.currentTimeMillis() - startTime;
            markAsProcessed(finalEventId, event.getEventType(), "REVIEW", duration);
            // Record metrics
            eventMetricsService.recordEventProcessed(event.getEventType(), duration);
            log.info("✅ Processed in {}ms: {}", duration, finalEventId);
        } catch (Exception e) {
            log.error("❌ Failed to process review event", e);
            throw new RuntimeException("Review event processing failed", e);
        }
    }

    private void handleReviewCreated(ReviewEvent event) {
        log.info("Creating review document: {}", event.getReviewId());
        ReviewDocument document = ReviewDocument.builder()
                .id(event.getReviewId().toString())
                .reviewId(event.getReviewId())
                .dishId(event.getDishId())
                .customerName(event.getCustomerName())
                .customerEmail(event.getCustomerEmail())
                .customerAvatar(event.getCustomerAvatar())
                .rating(event.getRating())
                .comment(event.getComment())
                .isActive(event.getIsActive())
                .isVerified(event.getIsVerified())
                .createdAt(event.getCreatedAt() != null ? event.getCreatedAt() : event.getTimestamp())
                .updatedAt(LocalDateTime.now())
                .build();

        reviewDocumentRepository.save(document);
        log.info("✅ Indexed review: {}", event.getReviewId());
    }

    private void handleReviewUpdated(ReviewEvent event) {
        log.info("Updating review document: {}", event.getReviewId());
        reviewDocumentRepository.findByReviewId(event.getReviewId())
                .ifPresentOrElse(
                        doc -> {
                            doc.setDishId(event.getDishId());
                            doc.setCustomerName(event.getCustomerName());
                            doc.setCustomerEmail(event.getCustomerEmail());
                            doc.setCustomerAvatar(event.getCustomerAvatar());
                            doc.setRating(event.getRating());
                            doc.setComment(event.getComment());
                            doc.setIsActive(event.getIsActive());
                            doc.setIsVerified(event.getIsVerified());
                            doc.setUpdatedAt(LocalDateTime.now());
                            reviewDocumentRepository.save(doc);
                            log.info("✅ Updated review document");
                        },
                        () -> {
                            log.warn("⚠️ Review not found, creating new document");
                            handleReviewCreated(event);
                        }
                );
    }

    private void handleReviewDeleted(ReviewEvent event) {
        log.info("Deleting review document: {}", event.getReviewId());
        reviewDocumentRepository.findByReviewId(event.getReviewId())
                .ifPresentOrElse(
                        doc -> {
                            reviewDocumentRepository.delete(doc);
                            log.info("✅ Deleted review document");
                        },
                        () -> log.warn("⚠️ Review not found for deletion: {}", event.getReviewId())
                );
    }

    private void handleReviewStatusChanged(ReviewEvent event) {
        log.info("Updating review status: {}", event.getReviewId());
        reviewDocumentRepository.findByReviewId(event.getReviewId())
                .ifPresentOrElse(
                        doc -> {
                            doc.setIsActive(event.getIsActive());
                            doc.setIsVerified(event.getIsVerified());
                            doc.setUpdatedAt(LocalDateTime.now());
                            reviewDocumentRepository.save(doc);
                            log.info("✅ Updated review status");
                        },
                        () -> {
                            log.warn("⚠️ Review not found, creating");
                            handleReviewCreated(event);
                        }
                );
    }

    private void markAsProcessed(String eventId, String eventType,
                                 String aggregateType, long durationMs) {
        ProcessedEvent processed = ProcessedEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .aggregateType(aggregateType)
                .processedAt(LocalDateTime.now())
                .processingDurationMs(durationMs)
                .build();

        processedEventRepository.save(processed);
    }
}