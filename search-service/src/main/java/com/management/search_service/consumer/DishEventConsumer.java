package com.management.search_service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.search_service.document.DishDocument;
import com.management.search_service.events.implement.DishEvent;
import com.management.search_service.model.ProcessedEvent;
import com.management.search_service.repository.DishDocumentRepository;
import com.management.search_service.repository.ProcessedEventRepository;
import com.management.search_service.service.EventMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DishEventConsumer {
    private final DishDocumentRepository dishDocumentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private final EventMetricsService metricsService;

    @RabbitListener(queues = "dish.search.queue")
    @Transactional
    public void onDishEvent(
            @Payload String eventJson,
            @Header(value = "eventId", required = false) String eventId) {

        long startTime = System.currentTimeMillis();

        try {
            log.info("📨 Received dish event: {}", eventId);

            DishEvent event = objectMapper.readValue(eventJson, DishEvent.class);
            String finalEventId = eventId != null ? eventId : event.getEventId();

            // Check idempotency
            if (processedEventRepository.existsByEventId(finalEventId)) {
                log.info("⚠️ Event already processed: {}", finalEventId);
                return;
            }

            // Process based on event type
            switch (event.getEventType()) {
                case "dish.created" -> handleDishCreated(event);
                case "dish.updated" -> handleDishUpdated(event);
                case "dish.deleted" -> handleDishDeleted(event);
                case "dish.availability.changed" -> handleDishAvailabilityChanged(event);
                case "dish.rating.updated" -> handleDishRatingUpdated(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }

            // Mark as processed
            long duration = System.currentTimeMillis() - startTime;
            markAsProcessed(finalEventId, event.getEventType(), "DISH", duration);
            // Record metrics
            metricsService.recordEventProcessed(event.getEventType(), duration);
            log.info("✅ Processed in {}ms: {}", duration, finalEventId);
        } catch (Exception e) {
            log.error("❌ Failed to process dish event", e);
            throw new RuntimeException("Dish event processing failed", e);
        }
    }

    private void handleDishCreated(DishEvent event) {
        log.info("Creating dish document: {}", event.getDishId());
        DishDocument document = DishDocument.builder()
                .id(event.getDishId().toString())
                .dishId(event.getDishId())
                .name(event.getName())
                .description(event.getDescription())
                .price(event.getPrice())
                .isAvailable(event.getIsAvailable())
                .category(event.getCategory())
                .imageUrls(event.getImageUrls())
                .averageRating(BigDecimal.valueOf(event.getAverageRating() != null ?
                        event.getAverageRating().doubleValue() : 0.0))
                .totalReviews(event.getTotalReviews() != null ? event.getTotalReviews() : 0)
                .orderCount(event.getOrderCount() != null ? event.getOrderCount() : 0)
                .createdAt(event.getCreatedAt() != null ? event.getCreatedAt() : event.getTimestamp())
                .updatedAt(LocalDateTime.now())
                .build();

        dishDocumentRepository.save(document);
        log.info("✅ Indexed dish: {}", event.getDishId());
    }

    private void handleDishUpdated(DishEvent event) {
        log.info("Updating dish document: {}", event.getDishId());

        dishDocumentRepository.findByDishId(event.getDishId())
                .ifPresentOrElse(
                        doc -> {
                            doc.setName(event.getName());
                            doc.setDescription(event.getDescription());
                            doc.setPrice(event.getPrice());
                            doc.setIsAvailable(event.getIsAvailable());
                            doc.setCategory(event.getCategory());
                            doc.setImageUrls(event.getImageUrls());
                            if (event.getAverageRating() != null) {
                                doc.setAverageRating(BigDecimal.valueOf(event.getAverageRating().doubleValue()));
                            }
                            if (event.getTotalReviews() != null) {
                                doc.setTotalReviews(event.getTotalReviews());
                            }
                            if (event.getOrderCount() != null) {
                                doc.setOrderCount(event.getOrderCount());
                            }
                            doc.setUpdatedAt(LocalDateTime.now());
                            dishDocumentRepository.save(doc);
                            log.info("✅ Updated dish document");
                        },
                        () -> {
                            log.warn("⚠️ Dish not found, creating new document");
                            handleDishCreated(event);
                        }
                );
    }

    private void handleDishDeleted(DishEvent event) {
        log.info("Deleting dish document: {}", event.getDishId());

        dishDocumentRepository.findByDishId(event.getDishId())
                .ifPresentOrElse(
                        doc -> {
                            dishDocumentRepository.delete(doc);
                            log.info("✅ Deleted dish document");
                        },
                        () -> log.warn("⚠️ Dish not found for deletion: {}", event.getDishId())
                );
    }

    private void handleDishAvailabilityChanged(DishEvent event) {
        log.info("Updating dish availability: {} -> {}", event.getDishId(), event.getIsAvailable());
        dishDocumentRepository.findByDishId(event.getDishId())
                .ifPresentOrElse(
                        doc -> {
                            doc.setIsAvailable(event.getIsAvailable());
                            doc.setUpdatedAt(LocalDateTime.now());
                            dishDocumentRepository.save(doc);
                            log.info("✅ Updated availability");
                        },
                        () -> {
                            log.warn("⚠️ Dish not found, creating");
                            handleDishCreated(event);
                        }
                );
    }

    private void handleDishRatingUpdated(DishEvent event) {
        log.info("Updating dish rating: {}", event.getDishId());
        dishDocumentRepository.findByDishId(event.getDishId())
                .ifPresentOrElse(
                        doc -> {
                            if (event.getAverageRating() != null) {
                                doc.setAverageRating(BigDecimal.valueOf(event.getAverageRating().doubleValue()));
                            }
                            if (event.getTotalReviews() != null) {
                                doc.setTotalReviews(event.getTotalReviews());
                            }
                            doc.setUpdatedAt(LocalDateTime.now());
                            dishDocumentRepository.save(doc);
                            log.info("✅ Updated rating");
                        },
                        () -> {
                            log.warn("⚠️ Dish not found for rating update");
                            handleDishCreated(event);
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
