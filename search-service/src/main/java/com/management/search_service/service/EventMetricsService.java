package com.management.search_service.service;

import com.management.search_service.repository.ProcessedEventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service to expose custom metrics for Prometheus/Grafana
 */
@Service
@Slf4j
public class EventMetricsService {
    private final MeterRegistry meterRegistry;
    private final ProcessedEventRepository processedEventRepository;

    // Counters for each event type
    private final Map<String, Counter> eventCounters = new ConcurrentHashMap<>();

    // Timer for processing duration
    private final Timer processingTimer;

    // Gauge for queue size
    private final Gauge queueSizeGauge;

    public EventMetricsService(MeterRegistry meterRegistry,
                               ProcessedEventRepository processedEventRepository) {
        this.meterRegistry = meterRegistry;
        this.processedEventRepository = processedEventRepository;

        // Register processing timer
        this.processingTimer = Timer.builder("event.processing.duration")
                .description("Time taken to process events")
                .tags("service", "search-service")
                .register(meterRegistry);

        // Register total events gauge
        this.queueSizeGauge = Gauge.builder("event.processed.total", this::getTotalProcessedEvents)
                .description("Total number of processed events")
                .tags("service", "search-service")
                .register(meterRegistry);

        // Register gauges for each event type
        registerEventTypeGauges();
    }

    /**
     * Record event processing
     */
    public void recordEventProcessed(String eventType, long durationMs) {
        // Increment counter for this event type
        getOrCreateCounter(eventType).increment();

        // Record processing duration
        processingTimer.record(durationMs, TimeUnit.MILLISECONDS);

        log.info("Recorded metrics for {} - duration: {}ms", eventType, durationMs);
    }

    /**
     * Get or create counter for event type
     */
    private Counter getOrCreateCounter(String eventType) {
        return eventCounters.computeIfAbsent(eventType, type ->
                Counter.builder("event.processed.count")
                        .description("Number of processed events by type")
                        .tags("event_type", type, "service", "search-service")
                        .register(meterRegistry)
        );
    }

    /**
     * Register gauges for event counts by type
     */
    private void registerEventTypeGauges() {
        Gauge.builder("event.dish.count", this, EventMetricsService::getDishEventCount)
                .description("Number of dish events processed")
                .tags("event_type", "dish")
                .register(meterRegistry);

        Gauge.builder("event.user.count", this, EventMetricsService::getUserEventCount)
                .description("Number of user events processed")
                .tags("event_type", "user")
                .register(meterRegistry);

        Gauge.builder("event.review.count", this, EventMetricsService::getReviewEventCount)
                .description("Number of review events processed")
                .tags("event_type", "review")
                .register(meterRegistry);

        Gauge.builder("event.booking.count", this, EventMetricsService::getBookingEventCount)
                .description("Number of booking events processed")
                .tags("event_type", "booking")
                .register(meterRegistry);
    }

    /**
     * Get counts for specific event types
     */
    private long getDishEventCount() {
        return getEventCountByPrefix("dish");
    }

    private long getUserEventCount() {
        return getEventCountByPrefix("user");
    }

    private long getReviewEventCount() {
        return getEventCountByPrefix("review");
    }

    private long getBookingEventCount() {
        return getEventCountByPrefix("booking");
    }

    private long getEventCountByPrefix(String prefix) {
        try {
            return processedEventRepository.countByEventTypeStartingWith(prefix);
        } catch (Exception e) {
            log.error("Error counting events for prefix: {}", prefix, e);
            return 0;
        }
    }

    /**
     * Get total processed events
     */
    private long getTotalProcessedEvents() {
        try {
            return processedEventRepository.count();
        } catch (Exception e) {
            log.error("Error getting total event count", e);
            return 0;
        }
    }

    /**
     * Get events processed in last hour
     */
    public long getEventsLastHour() {
        try {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            return processedEventRepository.countByProcessedAtAfter(oneHourAgo);
        } catch (Exception e) {
            log.error("Error getting events from last hour", e);
            return 0;
        }
    }

    /**
     * Get average processing time
     */
    public double getAverageProcessingTime() {
        try {
            return processedEventRepository.findAverageProcessingTime();
        } catch (Exception e) {
            log.error("Error getting average processing time", e);
            return 0.0;
        }
    }
}