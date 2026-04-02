package com.management.search_service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.search_service.document.UserDocument;
import com.management.search_service.events.implement.UserEvent;
import com.management.search_service.model.ProcessedEvent;
import com.management.search_service.repository.ProcessedEventRepository;
import com.management.search_service.repository.UserDocumentRepository;
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
public class UserEventConsumer {
    private final UserDocumentRepository userDocumentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private final EventMetricsService eventMetricsService;

    @RabbitListener(queues = "user.search.queue")
    @Transactional
    public void onReviewEvent(
            @Payload String eventJson,
            @Header(value = "eventId", required = false) String eventId) {

        long startTime = System.currentTimeMillis();

        try {
            log.info("📨 Received user event: {}", eventId);

            UserEvent event = objectMapper.readValue(eventJson, UserEvent.class);
            String finalEventId = eventId != null ? eventId : event.getEventId();

            // Check idempotency
            if (processedEventRepository.existsByEventId(finalEventId)) {
                log.info("⚠️ Event already processed: {}", finalEventId);
                return;
            }

            // Process based on event type
            switch (event.getEventType()) {
                case "user.created" -> handleUserCreated(event);
                case "user.updated" -> handleUserUpdated(event);
                case "user.deleted" -> handleUserDeleted(event);
                case "user.status.changed" -> handleReviewStatusChanged(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }

            // Mark as processed
            long duration = System.currentTimeMillis() - startTime;
            markAsProcessed(finalEventId, event.getEventType(), "USER", duration);
            // Record metrics
            eventMetricsService.recordEventProcessed(event.getEventType(), duration);
            log.info("✅ Processed in {}ms: {}", duration, finalEventId);
        } catch (Exception e) {
            log.error("❌ Failed to process review event", e);
            throw new RuntimeException("User event processing failed", e);
        }
    }

    private void handleUserCreated(UserEvent event) {
        log.info("Creating user document: {}", event.getUserId());
        UserDocument document = UserDocument.builder()
                .userId(event.getUserId())
                .fullName(event.getFullName())
                .email(event.getEmail())
                .phoneNumber(event.getPhoneNumber())
                .address(event.getAddress())
                .status(event.getStatus())
                .username(event.getUsername())
                .avatarUrl(event.getAvatarUrl())
                .roleName(event.getRoleName())
                .createdAt(event.getCreatedAt() != null ? event.getCreatedAt() : event.getTimestamp())
                .updatedAt(LocalDateTime.now())
                .build();
        userDocumentRepository.save(document);
        log.info("✅ Indexed user: {}", event.getUserId());
    }

    private void handleUserUpdated(UserEvent event) {
        log.info("Updating user document: {}", event.getUserId());
        userDocumentRepository.findByUserId(event.getUserId())
                .ifPresentOrElse(
                        doc -> {
                            doc.setUserId(event.getUserId());
                            doc.setFullName(event.getFullName());
                            doc.setEmail(event.getEmail());
                            doc.setAddress(event.getAddress() != null ? event.getAddress() : doc.getAddress());
                            doc.setPhoneNumber(event.getPhoneNumber());
                            doc.setAvatarUrl(event.getAvatarUrl());
                            doc.setStatus(event.getStatus());
                            doc.setRoleName(event.getRoleName());
                            doc.setUpdatedAt(LocalDateTime.now());
                            userDocumentRepository.save(doc);
                            log.info("✅ Updated user document");
                        },
                        () -> {
                            log.warn("⚠️ User not found, creating new document");
                            handleUserCreated(event);
                        }
                );
    }

    private void handleUserDeleted(UserEvent event) {
        log.info("Deleting user document: {}", event.getUserId());

        userDocumentRepository.findByUserId(event.getUserId())
                .ifPresentOrElse(
                        doc -> {
                            userDocumentRepository.delete(doc);
                            log.info("✅ Deleted user document");
                        },
                        () -> log.warn("⚠️ User not found for deletion: {}", event.getUserId())
                );
    }

    private void handleReviewStatusChanged(UserEvent event) {
        log.info("Updating user status: {}", event.getUserId());
        userDocumentRepository.findByUserId(event.getUserId())
                .ifPresentOrElse(
                        doc -> {
                            doc.setStatus(event.getStatus());
                            doc.setUpdatedAt(LocalDateTime.now());
                            userDocumentRepository.save(doc);
                            log.info("✅ Updated User status");
                        },
                        () -> {
                            log.warn("⚠️ User not found, creating");
                            handleUserCreated(event);
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