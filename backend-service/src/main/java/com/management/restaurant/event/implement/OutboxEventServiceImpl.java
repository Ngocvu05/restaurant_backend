package com.management.restaurant.event.implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.restaurant.event.OutboxEventService;
import com.management.restaurant.event.model.BookingEvent;
import com.management.restaurant.event.model.OutboxEvent;
import com.management.restaurant.event.model.UserEvent;
import com.management.restaurant.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.management.restaurant.config.RabbitConfig.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventServiceImpl implements OutboxEventService {
    private final OutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Save event to outbox (called within transaction)
     * CRITICAL: This MUST be @Transactional to ensure atomicity
     */
    @Transactional
    @Override
    public void saveBookingEvent(BookingEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .aggregateType("BOOKING")
                    .aggregateId(event.getBookingId())
                    .payload(payload)
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(outboxEvent);
            log.info("✅ Saved booking event to outbox: {}", event.getEventId());

        } catch (Exception e) {
            log.error("❌ Failed to save event to outbox", e);
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }

    @Transactional
    @Override
    public void saveUserEvent(UserEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .aggregateType("USER")
                    .aggregateId(event.getUserId())
                    .payload(payload)
                    .status("PENDING")
                    .build();

            outboxRepository.save(outboxEvent);
            log.info("✅ Saved user event to outbox: {}", event.getEventId());

        } catch (Exception e) {
            log.error("❌ Failed to save user event", e);
            throw new RuntimeException("Failed to save event", e);
        }
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    @Override
    public void processPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository
                .findPendingEvents(LocalDateTime.now());

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("📤 Processing {} pending events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                publishEvent(event);
                markAsPublished(event);
                log.info("✅ Published: {}", event.getEventId());

            } catch (Exception e) {
                handlePublishFailure(event, e);
            }
        }
    }

    private void publishEvent(OutboxEvent event) {
        String exchange = getExchangeName(event.getAggregateType());
        String routingKey = event.getEventType();

        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                event.getPayload(),
                message -> {
                    message.getMessageProperties().setHeader("eventId", event.getEventId());
                    message.getMessageProperties().setHeader("retryCount", event.getRetryCount());
                    message.getMessageProperties().setContentType("application/json");
                    return message;
                }
        );
    }

    private void markAsPublished(OutboxEvent event) {
        event.setStatus("PUBLISHED");
        event.setPublishedAt(LocalDateTime.now());
        outboxRepository.save(event);
    }

    private void handlePublishFailure(OutboxEvent event, Exception e) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setErrorMessage(truncate(e.getMessage(), 500));

        if (event.getRetryCount() >= event.getMaxRetries()) {
            event.setStatus("FAILED");
            log.error("❌ Event FAILED: {}", event.getEventId());
        } else {
            long delaySeconds = (long) Math.pow(2, event.getRetryCount()) * 60;
            event.setNextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds));
            log.warn("⚠️  Retry in {} seconds: {}", delaySeconds, event.getEventId());
        }
        outboxRepository.save(event);
    }

    private String getExchangeName(String aggregateType) {
        return switch (aggregateType) {
            case "BOOKING" -> BOOKING_EXCHANGE;
            case "USER" -> USER_EXCHANGE;
            case "DISH" -> DISH_EXCHANGE;
            case "REVIEW" -> REVIEW_EXCHANGE;
            default -> "default.exchange";
        };
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<OutboxEvent> oldEvents = outboxRepository.findOldPublishedEvents(cutoff);

        if (!oldEvents.isEmpty()) {
            outboxRepository.deleteAll(oldEvents);
            log.info("🗑️  Cleaned up {} old events", oldEvents.size());
        }
    }
}