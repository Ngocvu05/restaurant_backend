CREATE TABLE IF NOT EXISTS processed_events (
                                                id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                                event_id VARCHAR(255) NOT NULL,
                                                event_type VARCHAR(100) NOT NULL,
                                                aggregate_type VARCHAR(50) NOT NULL,
                                                processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                processing_duration_ms BIGINT,
                                                metadata JSON,
                                                UNIQUE KEY uk_processed_event_id (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes
CREATE INDEX idx_processed_at ON processed_events (processed_at);
CREATE INDEX idx_processed_aggregate ON processed_events (aggregate_type);


-- ============================================
-- USEFUL QUERIES FOR MONITORING
-- ============================================

-- Count events by status
-- SELECT status, COUNT(*) FROM outbox_events GROUP BY status;

-- Failed events in last 24 hours
-- SELECT * FROM outbox_events
-- WHERE status = 'FAILED'
-- AND created_at > NOW() - INTERVAL '24 hours'
-- ORDER BY created_at DESC;

-- Events pending for too long (> 5 minutes)
-- SELECT * FROM outbox_events
-- WHERE status = 'PENDING'
-- AND created_at < NOW() - INTERVAL '5 minutes'
-- ORDER BY created_at ASC
-- LIMIT 100;

-- Processing rate (events per hour)
-- SELECT
--     DATE_TRUNC('hour', published_at) as hour,
--     COUNT(*) as events_published
-- FROM outbox_events
-- WHERE published_at IS NOT NULL
-- GROUP BY hour
-- ORDER BY hour DESC
-- LIMIT 24;