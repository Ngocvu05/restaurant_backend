-- ============================================================================
-- Migration: Add OAuth2 Links Table (MySQL Compatible)
-- File: 06-add-oauth2-links-FIXED.sql
-- Version: V4 (Fixed for MySQL)
-- Date: 2026-02-11
--
-- Purpose: Create table for OAuth2 account linking
-- - Link multiple OAuth2 providers to one user account
-- - Support for Google, Facebook, GitHub, Apple, etc.
-- - Track provider tokens and usage
-- - Enable multi-provider authentication
--
-- CHANGES FROM V3:
-- - Fixed DROP INDEX IF EXISTS syntax (not supported in MySQL)
-- - Added proper index dropping procedure
-- - Simplified for MySQL compatibility
-- ============================================================================

USE restaurant;

-- ============================================================================
-- PART 0: Drop Table if You Want Clean Start (OPTIONAL - Uncomment if needed)
-- ============================================================================

-- WARNING: This will delete all OAuth2 links data!
-- Only uncomment if you want to start fresh
-- DROP TABLE IF EXISTS oauth2_links;

-- ============================================================================
-- PART 1: Create oauth2_links Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS oauth2_links (
    -- Primary key
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- User reference
                                            user_id BIGINT NOT NULL,

    -- OAuth2 provider information
                                            provider VARCHAR(50) NOT NULL COMMENT 'OAuth2 provider name (google, facebook, github, apple)',
                                            provider_user_id VARCHAR(255) NOT NULL COMMENT 'Unique user ID from OAuth2 provider',
                                            provider_email VARCHAR(255) NULL COMMENT 'Email from OAuth2 provider',
                                            provider_display_name VARCHAR(255) NULL COMMENT 'Display name from OAuth2 provider',
                                            provider_picture_url VARCHAR(500) NULL COMMENT 'Profile picture URL from OAuth2 provider',

    -- Token storage (optional - for calling provider APIs)
                                            access_token VARCHAR(1000) NULL COMMENT 'OAuth2 access token (should be encrypted)',
                                            refresh_token VARCHAR(1000) NULL COMMENT 'OAuth2 refresh token (should be encrypted)',
                                            token_expires_at TIMESTAMP NULL COMMENT 'Token expiry timestamp',
                                            scopes VARCHAR(500) NULL COMMENT 'OAuth2 scopes granted',

    -- Tracking fields
                                            linked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When this OAuth2 link was created',
                                            last_used_at TIMESTAMP NULL COMMENT 'Last time this OAuth2 provider was used to login',
                                            is_primary BOOLEAN DEFAULT FALSE COMMENT 'Is this the primary OAuth2 provider for the user',

    -- Additional metadata
                                            metadata TEXT NULL COMMENT 'Additional metadata from provider (JSON format)',

    -- Audit fields (inherited from SoftDeletableEntity pattern)
                                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                            deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp',
                                            created_by VARCHAR(255) NULL,
                                            updated_by VARCHAR(255) NULL,
                                            deleted_by VARCHAR(255) NULL,

    -- Foreign key constraint
                                            CONSTRAINT fk_oauth2_links_user
                                                FOREIGN KEY (user_id)
                                                    REFERENCES users(id)
                                                    ON DELETE CASCADE

    -- NOTE: Unique constraint removed - will cause issues with soft delete
    -- Instead, enforce uniqueness in application layer
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='OAuth2 account links - allows multiple providers per user';

-- ============================================================================
-- PART 2: Create Indexes for Performance (MySQL Compatible)
-- ============================================================================

-- MySQL doesn't support DROP INDEX IF EXISTS ... ON table
-- So we use a different approach: Check and drop

-- Procedure to safely drop index if exists
DELIMITER //

DROP PROCEDURE IF EXISTS drop_index_if_exists//

CREATE PROCEDURE drop_index_if_exists(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64)
)
BEGIN
    DECLARE index_exists INT DEFAULT 0;

    -- Check if index exists
    SELECT COUNT(*) INTO index_exists
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND INDEX_NAME = p_index_name;

    -- Drop if exists
    IF index_exists > 0 THEN
        SET @sql = CONCAT('DROP INDEX ', p_index_name, ' ON ', p_table_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('Dropped index: ', p_index_name) AS message;
    ELSE
        SELECT CONCAT('Index does not exist: ', p_index_name) AS message;
    END IF;
END//

DELIMITER ;

-- Now drop all indexes if they exist
CALL drop_index_if_exists('oauth2_links', 'idx_oauth2_links_user_id');
CALL drop_index_if_exists('oauth2_links', 'idx_oauth2_links_provider');
CALL drop_index_if_exists('oauth2_links', 'idx_oauth2_links_provider_user_id');
CALL drop_index_if_exists('oauth2_links', 'idx_oauth2_links_provider_email');
CALL drop_index_if_exists('oauth2_links', 'idx_oauth2_links_token_expiry');
CALL drop_index_if_exists('oauth2_links', 'idx_oauth2_links_last_used');
CALL drop_index_if_exists('oauth2_links', 'idx_oauth2_links_deleted');
CALL drop_index_if_exists('oauth2_links', 'idx_oauth2_links_user_active');

-- Now create indexes (they don't exist anymore)
CREATE INDEX idx_oauth2_links_user_id
    ON oauth2_links(user_id);

CREATE INDEX idx_oauth2_links_provider
    ON oauth2_links(provider);

CREATE INDEX idx_oauth2_links_provider_user_id
    ON oauth2_links(provider, provider_user_id);

CREATE INDEX idx_oauth2_links_provider_email
    ON oauth2_links(provider, provider_email);

CREATE INDEX idx_oauth2_links_token_expiry
    ON oauth2_links(token_expires_at);

CREATE INDEX idx_oauth2_links_last_used
    ON oauth2_links(last_used_at);

CREATE INDEX idx_oauth2_links_deleted
    ON oauth2_links(deleted_at);

CREATE INDEX idx_oauth2_links_user_active
    ON oauth2_links(user_id, deleted_at);

-- ============================================================================
-- PART 3: Create Stored Procedures
-- ============================================================================

DELIMITER //

-- Procedure to cleanup expired OAuth2 tokens
DROP PROCEDURE IF EXISTS cleanup_expired_oauth2_tokens//

CREATE PROCEDURE cleanup_expired_oauth2_tokens()
BEGIN
    DECLARE cleaned_count INT DEFAULT 0;

    -- Clear expired access and refresh tokens
    UPDATE oauth2_links
    SET access_token = NULL,
        refresh_token = NULL
    WHERE token_expires_at < CURRENT_TIMESTAMP
      AND (access_token IS NOT NULL OR refresh_token IS NOT NULL);

    SET cleaned_count = ROW_COUNT();

    SELECT cleaned_count AS tokens_cleaned;
END//

-- Procedure to get OAuth2 statistics
DROP PROCEDURE IF EXISTS get_oauth2_stats//

CREATE PROCEDURE get_oauth2_stats()
BEGIN
    SELECT
        provider,
        COUNT(DISTINCT user_id) as user_count,
        COUNT(*) as total_links,
        ROUND(COUNT(*) / COUNT(DISTINCT user_id), 2) as avg_links_per_user,
        SUM(CASE WHEN is_primary = TRUE THEN 1 ELSE 0 END) as primary_count,
        SUM(CASE WHEN last_used_at > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY)
                     THEN 1 ELSE 0 END) as active_30_days
    FROM oauth2_links
    WHERE deleted_at IS NULL
    GROUP BY provider
    ORDER BY user_count DESC;
END//

-- Procedure to find users with multiple OAuth2 providers
DROP PROCEDURE IF EXISTS get_multi_provider_users//

CREATE PROCEDURE get_multi_provider_users()
BEGIN
    SELECT
        u.id,
        u.username,
        u.email,
        COUNT(o.id) as provider_count,
        GROUP_CONCAT(o.provider ORDER BY o.provider SEPARATOR ', ') as providers,
        GROUP_CONCAT(
                CASE WHEN o.is_primary = TRUE THEN o.provider ELSE NULL END
        ) as primary_provider
    FROM users u
             INNER JOIN oauth2_links o ON u.id = o.user_id
    WHERE o.deleted_at IS NULL
    GROUP BY u.id, u.username, u.email
    HAVING COUNT(o.id) > 1
    ORDER BY provider_count DESC;
END//

-- Procedure to find inactive OAuth2 links
DROP PROCEDURE IF EXISTS get_inactive_oauth2_links//

CREATE PROCEDURE get_inactive_oauth2_links(IN days_threshold INT)
BEGIN
    SELECT
        u.username,
        o.provider,
        o.linked_at,
        o.last_used_at,
        DATEDIFF(CURRENT_TIMESTAMP, COALESCE(o.last_used_at, o.linked_at)) as days_inactive
    FROM oauth2_links o
             INNER JOIN users u ON o.user_id = u.id
    WHERE o.deleted_at IS NULL
      AND (
        o.last_used_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL days_threshold DAY)
            OR (o.last_used_at IS NULL AND o.linked_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL days_threshold DAY))
        )
    ORDER BY days_inactive DESC;
END//

-- Procedure to set primary OAuth2 provider
DROP PROCEDURE IF EXISTS set_primary_oauth2_provider//

CREATE PROCEDURE set_primary_oauth2_provider(
    IN p_user_id BIGINT,
    IN p_provider VARCHAR(50)
)
BEGIN
    -- Remove primary status from all providers for this user
    UPDATE oauth2_links
    SET is_primary = FALSE,
        updated_at = CURRENT_TIMESTAMP
    WHERE user_id = p_user_id
      AND deleted_at IS NULL;

    -- Set new primary provider
    UPDATE oauth2_links
    SET is_primary = TRUE,
        updated_at = CURRENT_TIMESTAMP
    WHERE user_id = p_user_id
      AND provider = p_provider
      AND deleted_at IS NULL;

    SELECT ROW_COUNT() as updated;
END//

DELIMITER ;

-- ============================================================================
-- PART 4: Create Views for Common Queries
-- ============================================================================

-- Drop views if they exist (MySQL compatible way)
DROP VIEW IF EXISTS v_active_oauth2_links;
DROP VIEW IF EXISTS v_oauth2_provider_stats;

-- View for active OAuth2 links
CREATE VIEW v_active_oauth2_links AS
SELECT
    o.id,
    o.user_id,
    u.username,
    u.email as user_email,
    o.provider,
    o.provider_email,
    o.provider_display_name,
    o.linked_at,
    o.last_used_at,
    o.is_primary,
    CASE
        WHEN o.token_expires_at IS NULL THEN 'No Token'
        WHEN o.token_expires_at > CURRENT_TIMESTAMP THEN 'Valid'
        ELSE 'Expired'
        END as token_status
FROM oauth2_links o
         INNER JOIN users u ON o.user_id = u.id
WHERE o.deleted_at IS NULL
  AND u.deleted_at IS NULL;

-- View for OAuth2 provider statistics
CREATE VIEW v_oauth2_provider_stats AS
SELECT
    provider,
    COUNT(DISTINCT user_id) as total_users,
    COUNT(*) as total_links,
    ROUND(COUNT(*) / COUNT(DISTINCT user_id), 2) as avg_links_per_user,
    SUM(CASE WHEN is_primary = TRUE THEN 1 ELSE 0 END) as primary_count,
    SUM(CASE WHEN last_used_at > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY)
                 THEN 1 ELSE 0 END) as active_7_days,
    SUM(CASE WHEN last_used_at > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY)
                 THEN 1 ELSE 0 END) as active_30_days
FROM oauth2_links
WHERE deleted_at IS NULL
GROUP BY provider;

-- ============================================================================
-- PART 5: Verification Queries
-- ============================================================================

-- Verify table was created
SELECT
    'oauth2_links' as table_name,
    COUNT(*) as column_count
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'oauth2_links';

-- Verify indexes were created
SELECT
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE,
    SEQ_IN_INDEX
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'oauth2_links'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- Verify foreign key constraint
SELECT
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'oauth2_links'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- ============================================================================
-- PART 6: Test Queries (Optional)
-- ============================================================================

-- Get initial statistics (should return empty result)
CALL get_oauth2_stats();

-- ============================================================================
-- Migration Complete
-- ============================================================================

SELECT '✅ OAuth2 links table migration completed successfully!' AS status;
SELECT 'Run: CALL get_oauth2_stats(); to view statistics' AS next_step;
SELECT 'Run: SELECT * FROM v_active_oauth2_links; to view active links' AS view_links;