-- =====================================================
-- MIGRATION: Update refresh_token table
-- Add audit fields, soft delete, and additional tracking
-- =====================================================

USE restaurant;

SET FOREIGN_KEY_CHECKS = 0;

-- ===== REFRESH_TOKEN TABLE MIGRATION =====

-- Step 1: Add new columns
ALTER TABLE refresh_token
    -- Timestamps
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT 'Token creation timestamp' AFTER token,
    ADD COLUMN created_by VARCHAR(100) DEFAULT 'system'
        COMMENT 'Username who created this token' AFTER created_at,
    ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
        COMMENT 'Last update timestamp' AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100)
        COMMENT 'Username who last updated this token' AFTER updated_at,

    -- Soft delete
    ADD COLUMN deleted_at TIMESTAMP NULL
        COMMENT 'Soft delete timestamp' AFTER updated_by,
    ADD COLUMN deleted_by VARCHAR(100)
        COMMENT 'Username who deleted this token' AFTER deleted_at,

    -- Token status and metadata
    ADD COLUMN revoked BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT 'Is token revoked' AFTER deleted_by,
    ADD COLUMN revoked_at TIMESTAMP NULL
        COMMENT 'When token was revoked' AFTER revoked,
    ADD COLUMN revoked_by VARCHAR(100)
        COMMENT 'Who revoked the token' AFTER revoked_at,
    ADD COLUMN revoked_reason VARCHAR(255)
        COMMENT 'Reason for revocation' AFTER revoked_by,

    -- Security tracking
    ADD COLUMN ip_address VARCHAR(45)
        COMMENT 'IP address when token was created' AFTER revoked_reason,
    ADD COLUMN user_agent VARCHAR(500)
        COMMENT 'User agent when token was created' AFTER ip_address,
    ADD COLUMN device_id VARCHAR(255)
        COMMENT 'Device identifier' AFTER user_agent,
    ADD COLUMN device_name VARCHAR(100)
        COMMENT 'Device name (e.g., Chrome on Windows)' AFTER device_id,

    -- Token family for refresh token rotation
    ADD COLUMN token_family VARCHAR(100)
        COMMENT 'Token family ID for rotation tracking' AFTER device_name,
    ADD COLUMN parent_token_id BIGINT
        COMMENT 'Previous token in refresh chain' AFTER token_family,

    -- Usage tracking
    ADD COLUMN last_used_at TIMESTAMP NULL
        COMMENT 'Last time token was used' AFTER parent_token_id,
    ADD COLUMN use_count INT NOT NULL DEFAULT 0
        COMMENT 'Number of times token was used' AFTER last_used_at;

-- Step 2: Update existing data
UPDATE refresh_token
SET created_by = 'system',
    updated_by = 'system',
    revoked = FALSE,
    use_count = 0
WHERE created_by IS NULL;

-- Step 3: Add indexes for performance
ALTER TABLE refresh_token
    ADD INDEX idx_refresh_token_user_id (user_id),
    ADD INDEX idx_refresh_token_created_at (created_at),
    ADD INDEX idx_refresh_token_expiry_date (expiry_date),
    ADD INDEX idx_refresh_token_deleted_at (deleted_at),
    ADD INDEX idx_refresh_token_revoked (revoked),
    ADD INDEX idx_refresh_token_token_family (token_family),
    ADD INDEX idx_refresh_token_ip_address (ip_address),
    ADD INDEX idx_refresh_token_last_used_at (last_used_at);

-- Step 4: Add foreign key for parent token (self-referencing)
ALTER TABLE refresh_token
    ADD CONSTRAINT fk_refresh_token_parent
        FOREIGN KEY (parent_token_id)
            REFERENCES refresh_token(id)
            ON DELETE SET NULL;

-- Step 5: Create view for active tokens only (excluding soft deleted and revoked)
CREATE OR REPLACE VIEW v_active_refresh_tokens AS
SELECT
    rt.*,
    u.username,
    u.email,
    u.full_name
FROM refresh_token rt
         INNER JOIN users u ON rt.user_id = u.id
WHERE rt.deleted_at IS NULL
  AND rt.revoked = FALSE
  AND rt.expiry_date > NOW();

-- Step 6: Create stored procedure to clean up expired tokens
DROP PROCEDURE IF EXISTS sp_cleanup_expired_refresh_tokens;

DELIMITER $$

CREATE PROCEDURE sp_cleanup_expired_refresh_tokens()
BEGIN
    DECLARE affected_rows INT DEFAULT 0;

    -- Soft delete expired tokens
    UPDATE refresh_token
    SET deleted_at = NOW(),
        deleted_by = 'system_cleanup',
        updated_at = NOW(),
        updated_by = 'system_cleanup'
    WHERE expiry_date < NOW()
      AND deleted_at IS NULL
      AND revoked = FALSE;

    SET affected_rows = ROW_COUNT();

    SELECT CONCAT('Cleaned up ', affected_rows, ' expired refresh tokens') AS result;

    -- Hard delete tokens older than 90 days
    DELETE FROM refresh_token
    WHERE deleted_at < DATE_SUB(NOW(), INTERVAL 90 DAY)
       OR (revoked = TRUE AND revoked_at < DATE_SUB(NOW(), INTERVAL 90 DAY));

END$$

DELIMITER ;

-- Step 7: Create stored procedure to revoke all tokens for a user
DROP PROCEDURE IF EXISTS sp_revoke_user_refresh_tokens;

DELIMITER $$

CREATE PROCEDURE sp_revoke_user_refresh_tokens(
    IN p_user_id BIGINT,
    IN p_revoked_by VARCHAR(100),
    IN p_reason VARCHAR(255)
)
BEGIN
    UPDATE refresh_token
    SET revoked = TRUE,
        revoked_at = NOW(),
        revoked_by = p_revoked_by,
        revoked_reason = p_reason,
        updated_at = NOW(),
        updated_by = p_revoked_by
    WHERE user_id = p_user_id
      AND deleted_at IS NULL
      AND revoked = FALSE;

    SELECT ROW_COUNT() AS tokens_revoked;
END$$

DELIMITER ;

-- Step 8: Create stored procedure to get token statistics
DROP PROCEDURE IF EXISTS sp_get_refresh_token_stats;

DELIMITER $$

CREATE PROCEDURE sp_get_refresh_token_stats()
BEGIN
    SELECT
        COUNT(*) AS total_tokens,
        COUNT(CASE WHEN deleted_at IS NULL AND revoked = FALSE THEN 1 END) AS active_tokens,
        COUNT(CASE WHEN revoked = TRUE THEN 1 END) AS revoked_tokens,
        COUNT(CASE WHEN deleted_at IS NOT NULL THEN 1 END) AS deleted_tokens,
        COUNT(CASE WHEN expiry_date < NOW() THEN 1 END) AS expired_tokens,
        COUNT(DISTINCT user_id) AS unique_users,
        COUNT(DISTINCT ip_address) AS unique_ip_addresses,
        AVG(use_count) AS avg_use_count,
        MAX(use_count) AS max_use_count
    FROM refresh_token;

    -- Top users by active tokens
    SELECT
        u.username,
        u.email,
        COUNT(*) AS active_token_count
    FROM refresh_token rt
             INNER JOIN users u ON rt.user_id = u.id
    WHERE rt.deleted_at IS NULL
      AND rt.revoked = FALSE
      AND rt.expiry_date > NOW()
    GROUP BY u.username, u.email
    ORDER BY active_token_count DESC
    LIMIT 10;
END$$

DELIMITER ;

-- Step 9: Create event to auto-cleanup expired tokens (runs daily)
DROP EVENT IF EXISTS evt_cleanup_expired_refresh_tokens;

CREATE EVENT evt_cleanup_expired_refresh_tokens
    ON SCHEDULE EVERY 1 DAY
        STARTS CURRENT_TIMESTAMP
    DO
    CALL sp_cleanup_expired_refresh_tokens();

-- Enable event scheduler if not enabled
SET GLOBAL event_scheduler = ON;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check table structure
DESCRIBE refresh_token;

-- Check indexes
SHOW INDEX FROM refresh_token;

-- Check views
SHOW FULL TABLES WHERE Table_type = 'VIEW' AND Tables_in_restaurant LIKE '%refresh_token%';

-- Check stored procedures
SHOW PROCEDURE STATUS WHERE Db = 'restaurant' AND Name LIKE '%refresh_token%';

-- Check events
SHOW EVENTS WHERE Db = 'restaurant' AND Name LIKE '%refresh_token%';

-- Get token statistics
CALL sp_get_refresh_token_stats();

-- =====================================================
-- TEST QUERIES
-- =====================================================

-- Test: View active tokens
SELECT * FROM v_active_refresh_tokens LIMIT 5;

-- Test: Revoke all tokens for a user
-- CALL sp_revoke_user_refresh_tokens(1, 'admin', 'Security test');

-- Test: Cleanup expired tokens
-- CALL sp_cleanup_expired_refresh_tokens();

-- =====================================================
-- ROLLBACK SCRIPT (if needed)
-- =====================================================
/*
-- Drop new columns
ALTER TABLE refresh_token
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS created_by,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS updated_by,
    DROP COLUMN IF EXISTS deleted_at,
    DROP COLUMN IF EXISTS deleted_by,
    DROP COLUMN IF EXISTS revoked,
    DROP COLUMN IF EXISTS revoked_at,
    DROP COLUMN IF EXISTS revoked_by,
    DROP COLUMN IF EXISTS revoked_reason,
    DROP COLUMN IF EXISTS ip_address,
    DROP COLUMN IF EXISTS user_agent,
    DROP COLUMN IF EXISTS device_id,
    DROP COLUMN IF EXISTS device_name,
    DROP COLUMN IF EXISTS token_family,
    DROP COLUMN IF EXISTS parent_token_id,
    DROP COLUMN IF EXISTS last_used_at,
    DROP COLUMN IF EXISTS use_count;

-- Drop foreign key
ALTER TABLE refresh_token DROP FOREIGN KEY IF EXISTS fk_refresh_token_parent;

-- Drop indexes
ALTER TABLE refresh_token
    DROP INDEX IF EXISTS idx_refresh_token_user_id,
    DROP INDEX IF EXISTS idx_refresh_token_created_at,
    DROP INDEX IF EXISTS idx_refresh_token_expiry_date,
    DROP INDEX IF EXISTS idx_refresh_token_deleted_at,
    DROP INDEX IF EXISTS idx_refresh_token_revoked,
    DROP INDEX IF EXISTS idx_refresh_token_token_family,
    DROP INDEX IF EXISTS idx_refresh_token_ip_address,
    DROP INDEX IF EXISTS idx_refresh_token_last_used_at;

-- Drop view
DROP VIEW IF EXISTS v_active_refresh_tokens;

-- Drop procedures
DROP PROCEDURE IF EXISTS sp_cleanup_expired_refresh_tokens;
DROP PROCEDURE IF EXISTS sp_revoke_user_refresh_tokens;
DROP PROCEDURE IF EXISTS sp_get_refresh_token_stats;

-- Drop event
DROP EVENT IF EXISTS evt_cleanup_expired_refresh_tokens;
*/

-- =====================================================
-- MIGRATION COMPLETED
-- =====================================================
-- Summary:
-- ✅ Added audit fields (created_at, created_by, updated_at, updated_by)
-- ✅ Added soft delete fields (deleted_at, deleted_by)
-- ✅ Added revocation tracking (revoked, revoked_at, revoked_by, revoked_reason)
-- ✅ Added security tracking (ip_address, user_agent, device_id, device_name)
-- ✅ Added token family for refresh token rotation
-- ✅ Added usage tracking (last_used_at, use_count)
-- ✅ Created indexes for performance
-- ✅ Created view for active tokens
-- ✅ Created stored procedures for management
-- ✅ Created automatic cleanup event
-- =====================================================