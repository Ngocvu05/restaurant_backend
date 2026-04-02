CREATE TABLE IF NOT EXISTS outbox_events (
                                             id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                             event_id VARCHAR(255) NOT NULL,
                                             event_type VARCHAR(100) NOT NULL,
                                             aggregate_type VARCHAR(50) NOT NULL,  -- e.g. BOOKING, USER, DISH, REVIEW
                                             aggregate_id BIGINT NOT NULL,
                                             payload TEXT NOT NULL,
                                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             published_at TIMESTAMP NULL DEFAULT NULL,
                                             retry_count INT NOT NULL DEFAULT 0,
                                             max_retries INT NOT NULL DEFAULT 3,
                                             error_message TEXT,
                                             next_retry_at TIMESTAMP NULL DEFAULT NULL,
                                             metadata JSON,
                                             UNIQUE KEY uk_outbox_event_id (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for typical query patterns
-- 1) Query by status and ordering by created_at
CREATE INDEX idx_outbox_status_created_at ON outbox_events (status, created_at);

-- 2) Query by aggregate (to find events for a given aggregate)
CREATE INDEX idx_outbox_aggregate ON outbox_events (aggregate_type, aggregate_id);

-- 3) For selecting pending events due for retry:
--    WHERE status = 'PENDING' AND retry_count < max_retries AND next_retry_at <= NOW()
-- MySQL does not support partial indexes, so create a composite index that helps the above WHERE clause.
CREATE INDEX idx_outbox_retry ON outbox_events (status, next_retry_at, retry_count);

-- 4) Index by event_type
CREATE INDEX idx_outbox_event_type ON outbox_events (event_type);
