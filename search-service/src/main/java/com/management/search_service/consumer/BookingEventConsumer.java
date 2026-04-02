package com.management.search_service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.search_service.document.BookingDocument;
import com.management.search_service.events.implement.BookingEvent;
import com.management.search_service.model.ProcessedEvent;
import com.management.search_service.repository.BookingDocumentRepository;
import com.management.search_service.repository.ProcessedEventRepository;
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
public class BookingEventConsumer {
    private final BookingDocumentRepository bookingDocumentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private final EventMetricsService eventMetricsService;

    @RabbitListener(queues = "booking.search.queue")
    @Transactional
    public void onBookingEvent(
            @Payload String eventJson,
            @Header(value = "eventId", required = false) String eventId) {

        long startTime = System.currentTimeMillis();

        try {
            log.info("📨 Received booking event: {}", eventId);

            BookingEvent event = objectMapper.readValue(eventJson, BookingEvent.class);
            String finalEventId = eventId != null ? eventId : event.getEventId();

            // Check idempotency
            if (processedEventRepository.existsByEventId(finalEventId)) {
                log.info("⚠️ Event already processed: {}", finalEventId);
                return;
            }

            // Process based on event type
            switch (event.getEventType()) {
                case "booking.created" -> handleBookingCreated(event);
                case "booking.status.changed" -> handleBookingStatusChanged(event);
                case "booking.cancelled" -> handleBookingCancelled(event);
                default -> log.warn("Unknown event: {}", event.getEventType());
            }

            // Mark as processed
            long duration = System.currentTimeMillis() - startTime;
            markAsProcessed(finalEventId, event.getEventType(), "BOOKING", duration);
            eventMetricsService.recordEventProcessed(event.getEventType(), duration);
            log.info("✅ Processed in {}ms: {}", duration, finalEventId);

        } catch (Exception e) {
            log.error("❌ Failed to process event", e);
            throw new RuntimeException("Processing failed", e);
        }
    }

    private void handleBookingCreated(BookingEvent event) {
        log.info("Creating booking document: {}", event.getBookingId());

        BookingDocument document = BookingDocument.builder()
                .id(event.getBookingId().toString())
                .bookingId(event.getBookingId())
                .userId(event.getUserId())
                .username(event.getUsername())
                .tableId(event.getTableId())
                .tableName(event.getTableName())
                .bookingTime(event.getBookingTime())
                .numberOfGuests(event.getNumberOfGuests())
                .status(event.getStatus())
                .note(event.getNote())
                .totalAmount(event.getTotalAmount())
                .version(event.getVersion())
                .createdAt(event.getTimestamp())
                .updatedAt(LocalDateTime.now())
                .build();

        bookingDocumentRepository.save(document);
        log.info("✅ Indexed booking: {}", event.getBookingId());
    }

    private void handleBookingStatusChanged(BookingEvent event) {
        log.info("Updating status: {} -> {}", event.getOldStatus(), event.getNewStatus());

        bookingDocumentRepository.findByBookingId(event.getBookingId())
                .ifPresentOrElse(
                        doc -> {
                            doc.setStatus(event.getNewStatus());
                            doc.setVersion(event.getVersion());
                            doc.setUpdatedAt(LocalDateTime.now());
                            bookingDocumentRepository.save(doc);
                            log.info("✅ Updated status");
                        },
                        () -> {
                            log.warn("⚠️ Booking not found, creating");
                            handleBookingCreated(event);
                        }
                );
    }

    private void handleBookingCancelled(BookingEvent event) {
        log.info("Deleting booking: {}", event.getBookingId());

        bookingDocumentRepository.findByBookingId(event.getBookingId())
                .ifPresent(doc -> {
                    bookingDocumentRepository.delete(doc);
                    log.info("✅ Deleted booking");
                });
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