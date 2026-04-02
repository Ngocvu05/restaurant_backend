-- ============================================================================
-- Migration: Add User Security & Tracking Fields (COMPLETE VERSION)
-- File: 05-add-user-security-fields.sql
-- Version: V2.1 (Includes Login Tracking)
-- Date: 2026-02-12
--
-- Purpose: Add ALL security and tracking features to users table
-- - Failed login tracking & account locking
-- - Password reset tokens
-- - Email verification
-- - Two-factor authentication (2FA)
-- - Login tracking (IP, device, user agent)
-- - Force password change
-- - Login counter
-- ============================================================================

USE restaurant;

-- ============================================================================
-- PART 1: Create Helper Procedure to Add Columns Safely
-- ============================================================================

DELIMITER //

DROP PROCEDURE IF EXISTS add_column_if_not_exists//

CREATE PROCEDURE add_column_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition VARCHAR(500)
)
BEGIN
    DECLARE column_count INT;

    SELECT COUNT(*) INTO column_count
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name;

    IF column_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name,
                          ' ADD COLUMN ', p_column_name, ' ', p_column_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        SELECT CONCAT('✅ Added column: ', p_column_name) AS result;
    ELSE
        SELECT CONCAT('⏭️  Column already exists: ', p_column_name) AS result;
    END IF;
END//

DELIMITER ;

-- ============================================================================
-- PART 2A: Add Security Columns
-- ============================================================================

-- Failed login tracking
CALL add_column_if_not_exists(
        'users',
        'failed_login_attempts',
        'INTEGER DEFAULT 0 COMMENT "Number of consecutive failed login attempts"'
     );

CALL add_column_if_not_exists(
        'users',
        'account_locked_until',
        'TIMESTAMP NULL COMMENT "Account locked until this timestamp (NULL if not locked)"'
     );

-- Password reset
CALL add_column_if_not_exists(
        'users',
        'reset_password_token',
        'VARCHAR(100) NULL COMMENT "Random token for password reset"'
     );

CALL add_column_if_not_exists(
        'users',
        'reset_token_expiry',
        'TIMESTAMP NULL COMMENT "Expiry time for password reset token"'
     );

-- Email verification
CALL add_column_if_not_exists(
        'users',
        'email_verified',
        'BOOLEAN DEFAULT FALSE COMMENT "Whether user has verified their email address"'
     );

CALL add_column_if_not_exists(
        'users',
        'email_verification_token',
        'VARCHAR(100) NULL COMMENT "Token for email verification"'
     );

CALL add_column_if_not_exists(
        'users',
        'email_verification_expiry',
        'TIMESTAMP NULL COMMENT "Expiry time for email verification token"'
     );

-- Two-factor authentication
CALL add_column_if_not_exists(
        'users',
        'two_factor_enabled',
        'BOOLEAN DEFAULT FALSE COMMENT "Whether 2FA is enabled for this user"'
     );

CALL add_column_if_not_exists(
        'users',
        'two_factor_secret',
        'VARCHAR(32) NULL COMMENT "Secret key for TOTP (Google Authenticator)"'
     );

CALL add_column_if_not_exists(
        'users',
        'backup_codes',
        'VARCHAR(500) NULL COMMENT "Comma-separated backup codes for 2FA recovery"'
     );

-- Password management
CALL add_column_if_not_exists(
        'users',
        'password_changed_at',
        'TIMESTAMP NULL COMMENT "Timestamp of last password change"'
     );

-- ============================================================================
-- PART 2B: Add Login Tracking Columns
-- ============================================================================

-- Last login timestamp
CALL add_column_if_not_exists(
        'users',
        'last_login_at',
        'TIMESTAMP NULL COMMENT "Timestamp of last successful login"'
     );

-- Force password change flag
CALL add_column_if_not_exists(
        'users',
        'force_password_change',
        'BOOLEAN DEFAULT FALSE COMMENT "Admin can force user to change password on next login"'
     );

-- Last login device information
CALL add_column_if_not_exists(
        'users',
        'last_login_device',
        'VARCHAR(255) NULL COMMENT "Device name/type from last login"'
     );

-- Last login IP address
CALL add_column_if_not_exists(
        'users',
        'last_login_ip',
        'VARCHAR(45) NULL COMMENT "IP address from last login (supports IPv6)"'
     );

-- Last login user agent
CALL add_column_if_not_exists(
        'users',
        'last_login_user_agent',
        'VARCHAR(500) NULL COMMENT "Browser/app user agent from last login"'
     );

-- Total login count
CALL add_column_if_not_exists(
        'users',
        'login_count',
        'INTEGER DEFAULT 0 COMMENT "Total number of successful logins"'
     );

-- ============================================================================
-- PART 3: Update Existing Data with Defaults
-- ============================================================================

-- Security defaults
UPDATE users
SET failed_login_attempts = 0
WHERE failed_login_attempts IS NULL;

UPDATE users
SET email_verified = FALSE
WHERE email_verified IS NULL;

UPDATE users
SET two_factor_enabled = FALSE
WHERE two_factor_enabled IS NULL;

UPDATE users
SET force_password_change = FALSE
WHERE force_password_change IS NULL;

UPDATE users
SET login_count = 0
WHERE login_count IS NULL;

-- Set password_changed_at to created_at for existing users
UPDATE users
SET password_changed_at = created_at
WHERE password_changed_at IS NULL AND created_at IS NOT NULL;

-- Set password_changed_at to current time if created_at is also NULL
UPDATE users
SET password_changed_at = CURRENT_TIMESTAMP
WHERE password_changed_at IS NULL;

-- ============================================================================
-- PART 4: Create Indexes for Performance
-- ============================================================================

DELIMITER //

DROP PROCEDURE IF EXISTS create_index_if_not_exists//

CREATE PROCEDURE create_index_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_definition VARCHAR(500)
)
BEGIN
    DECLARE index_count INT;

    SELECT COUNT(*) INTO index_count
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND INDEX_NAME = p_index_name;

    IF index_count = 0 THEN
        SET @sql = CONCAT('CREATE INDEX ', p_index_name,
                          ' ON ', p_table_name, ' ', p_index_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        SELECT CONCAT('✅ Created index: ', p_index_name) AS result;
    ELSE
        SELECT CONCAT('⏭️  Index already exists: ', p_index_name) AS result;
    END IF;
END//

DELIMITER ;

-- Security indexes
CALL create_index_if_not_exists('users', 'idx_users_reset_token', '(reset_password_token)');
CALL create_index_if_not_exists('users', 'idx_users_verification_token', '(email_verification_token)');
CALL create_index_if_not_exists('users', 'idx_users_email', '(email)');
CALL create_index_if_not_exists('users', 'idx_users_locked', '(account_locked_until)');
CALL create_index_if_not_exists('users', 'idx_users_last_login', '(last_login_at)');
CALL create_index_if_not_exists('users', 'idx_users_email_verified', '(email_verified)');
CALL create_index_if_not_exists('users', 'idx_users_status_deleted', '(status, deleted_at)');

-- Tracking indexes
CALL create_index_if_not_exists('users', 'idx_users_last_login_ip', '(last_login_ip)');
CALL create_index_if_not_exists('users', 'idx_users_force_password_change', '(force_password_change)');

-- ============================================================================
-- PART 5: Add Constraints
-- ============================================================================

-- Unique constraint on email
DELIMITER //

DROP PROCEDURE IF EXISTS add_unique_constraint_if_not_exists//

CREATE PROCEDURE add_unique_constraint_if_not_exists()
BEGIN
    DECLARE constraint_count INT;

    SELECT COUNT(*) INTO constraint_count
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND CONSTRAINT_NAME = 'users_email_unique';

    IF constraint_count = 0 THEN
        ALTER TABLE users ADD CONSTRAINT users_email_unique UNIQUE (email);
        SELECT '✅ Added unique constraint: users_email_unique' AS result;
    ELSE
        SELECT '⏭️  Constraint already exists: users_email_unique' AS result;
    END IF;
END//

DELIMITER ;

CALL add_unique_constraint_if_not_exists();

-- CHECK constraint (MySQL 8.0.16+)
DELIMITER //

DROP PROCEDURE IF EXISTS add_check_constraint_if_supported//

CREATE PROCEDURE add_check_constraint_if_supported()
BEGIN
    DECLARE mysql_major INT;
    DECLARE mysql_minor INT;

    SET mysql_major = CAST(SUBSTRING_INDEX(VERSION(), '.', 1) AS UNSIGNED);
    SET mysql_minor = CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(VERSION(), '.', 2), '.', -1) AS UNSIGNED);

    IF mysql_major > 8 OR (mysql_major = 8 AND mysql_minor >= 0) THEN
        SET @constraint_exists = (
            SELECT COUNT(*)
            FROM information_schema.TABLE_CONSTRAINTS
            WHERE CONSTRAINT_SCHEMA = DATABASE()
              AND TABLE_NAME = 'users'
              AND CONSTRAINT_NAME = 'check_failed_attempts_positive'
        );

        IF @constraint_exists = 0 THEN
            ALTER TABLE users
                ADD CONSTRAINT check_failed_attempts_positive
                    CHECK (failed_login_attempts >= 0);

            SELECT '✅ Added CHECK constraint: check_failed_attempts_positive' AS result;
        ELSE
            SELECT '⏭️  CHECK constraint already exists' AS result;
        END IF;
    ELSE
        SELECT '⚠️  CHECK constraints not supported in this MySQL version' AS result;
    END IF;
END//

DELIMITER ;

CALL add_check_constraint_if_supported();

-- ============================================================================
-- PART 6: Create Stored Procedures
-- ============================================================================

DELIMITER //

-- Cleanup expired tokens
DROP PROCEDURE IF EXISTS cleanup_expired_tokens//

CREATE PROCEDURE cleanup_expired_tokens()
BEGIN
    DECLARE deleted_count INT DEFAULT 0;

    -- Clear expired password reset tokens
    UPDATE users
    SET reset_password_token = NULL,
        reset_token_expiry = NULL
    WHERE reset_token_expiry < CURRENT_TIMESTAMP;

    SET deleted_count = deleted_count + ROW_COUNT();

    -- Clear expired email verification tokens
    UPDATE users
    SET email_verification_token = NULL,
        email_verification_expiry = NULL
    WHERE email_verification_expiry < CURRENT_TIMESTAMP;

    SET deleted_count = deleted_count + ROW_COUNT();

    -- Auto-unlock expired account locks
    UPDATE users
    SET account_locked_until = NULL,
        failed_login_attempts = 0
    WHERE account_locked_until < CURRENT_TIMESTAMP;

    SET deleted_count = deleted_count + ROW_COUNT();

    SELECT deleted_count AS tokens_cleaned;
END//

-- Security statistics
DROP PROCEDURE IF EXISTS get_user_security_stats//

CREATE PROCEDURE get_user_security_stats()
BEGIN
    SELECT
        COUNT(*) as total_users,
        SUM(CASE WHEN email_verified = TRUE THEN 1 ELSE 0 END) as verified_emails,
        SUM(CASE WHEN two_factor_enabled = TRUE THEN 1 ELSE 0 END) as with_2fa,
        SUM(CASE WHEN account_locked_until IS NOT NULL
            AND account_locked_until > CURRENT_TIMESTAMP THEN 1 ELSE 0 END) as locked_accounts,
        SUM(CASE WHEN failed_login_attempts >= 3 THEN 1 ELSE 0 END) as high_failed_attempts,
        SUM(CASE WHEN last_login_at IS NULL THEN 1 ELSE 0 END) as never_logged_in,
        SUM(CASE WHEN last_login_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 90 DAY)
                     THEN 1 ELSE 0 END) as inactive_90_days
    FROM users
    WHERE deleted_at IS NULL;
END//

-- Login tracking statistics
DROP PROCEDURE IF EXISTS get_login_tracking_stats//

CREATE PROCEDURE get_login_tracking_stats()
BEGIN
    SELECT
        -- Basic counts
        COUNT(*) as total_users,
        COUNT(DISTINCT last_login_ip) as unique_ips,
        COUNT(DISTINCT last_login_device) as unique_devices,

        -- Login activity
        SUM(login_count) as total_logins,
        ROUND(AVG(login_count), 2) as avg_logins_per_user,

        -- Password change requirements
        SUM(CASE WHEN force_password_change = TRUE THEN 1 ELSE 0 END) as users_need_password_change,

        -- Recent activity
        SUM(CASE WHEN last_login_at > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 HOUR)
                     THEN 1 ELSE 0 END) as active_24h,
        SUM(CASE WHEN last_login_at > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY)
                     THEN 1 ELSE 0 END) as active_7d,
        SUM(CASE WHEN last_login_at > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY)
                     THEN 1 ELSE 0 END) as active_30d,

        -- Never logged in
        SUM(CASE WHEN last_login_at IS NULL OR login_count = 0 THEN 1 ELSE 0 END) as never_logged_in
    FROM users
    WHERE deleted_at IS NULL;
END//

-- Find suspicious logins
DROP PROCEDURE IF EXISTS find_suspicious_logins//

CREATE PROCEDURE find_suspicious_logins()
BEGIN
    -- High failed attempts
    SELECT
        'High Failed Attempts' as alert_type,
        u.username,
        u.email,
        u.failed_login_attempts,
        u.last_login_ip,
        u.account_locked_until
    FROM users u
    WHERE u.failed_login_attempts >= 3
      AND u.deleted_at IS NULL

    UNION ALL

    -- Multiple recent logins
    SELECT
        'Multiple Recent Logins' as alert_type,
        u.username,
        u.email,
        u.login_count,
        u.last_login_ip,
        NULL
    FROM users u
    WHERE u.login_count > 100
      AND u.last_login_at > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY)
      AND u.deleted_at IS NULL

    UNION ALL

    -- Needs password change
    SELECT
        'Needs Password Change' as alert_type,
        u.username,
        u.email,
        0,
        u.last_login_ip,
        NULL
    FROM users u
    WHERE u.force_password_change = TRUE
      AND u.deleted_at IS NULL

    ORDER BY alert_type, username;
END//

DELIMITER ;

-- ============================================================================
-- PART 7: Create Views
-- ============================================================================

DROP VIEW IF EXISTS v_user_login_activity;

CREATE VIEW v_user_login_activity AS
SELECT
    u.id,
    u.username,
    u.email,
    u.last_login_at,
    u.last_login_ip,
    u.last_login_device,
    u.login_count,
    u.failed_login_attempts,
    u.account_locked_until,
    u.force_password_change,
    CASE
        WHEN u.last_login_at IS NULL THEN 'Never Logged In'
        WHEN u.last_login_at > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 HOUR) THEN 'Active (24h)'
        WHEN u.last_login_at > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) THEN 'Active (7d)'
        WHEN u.last_login_at > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY) THEN 'Active (30d)'
        ELSE 'Inactive'
        END as activity_status,
    CASE
        WHEN u.account_locked_until IS NOT NULL AND u.account_locked_until > CURRENT_TIMESTAMP
            THEN 'Locked'
        WHEN u.status = 'INACTIVE' THEN 'Disabled'
        ELSE 'Active'
        END as account_status
FROM users u
WHERE u.deleted_at IS NULL;

-- ============================================================================
-- PART 8: Create Automatic Cleanup Event
-- ============================================================================

-- Enable event scheduler
SET GLOBAL event_scheduler = ON;

-- Drop event if exists
DROP EVENT IF EXISTS cleanup_expired_tokens_daily;

-- Create event - runs daily at 2 AM
CREATE EVENT cleanup_expired_tokens_daily
    ON SCHEDULE EVERY 1 DAY
        STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 1 DAY + INTERVAL 2 HOUR)
    DO
    CALL cleanup_expired_tokens();

-- ============================================================================
-- PART 9: Cleanup Helper Procedures
-- ============================================================================

DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS create_index_if_not_exists;
DROP PROCEDURE IF EXISTS add_unique_constraint_if_not_exists;
DROP PROCEDURE IF EXISTS add_check_constraint_if_supported;

-- ============================================================================
-- PART 10: Verification & Statistics
-- ============================================================================

-- Verify all columns were added
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME IN (
                      'failed_login_attempts',
                      'account_locked_until',
                      'reset_password_token',
                      'reset_token_expiry',
                      'email_verified',
                      'email_verification_token',
                      'email_verification_expiry',
                      'two_factor_enabled',
                      'two_factor_secret',
                      'backup_codes',
                      'last_login_at',
                      'password_changed_at',
                      'force_password_change',
                      'last_login_device',
                      'last_login_ip',
                      'last_login_user_agent',
                      'login_count'
    )
ORDER BY ORDINAL_POSITION;

-- Verify indexes were created
SELECT
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'users'
  AND INDEX_NAME LIKE 'idx_users_%'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- Get security statistics
CALL get_user_security_stats();

-- Get login tracking statistics
CALL get_login_tracking_stats();

-- ============================================================================
-- Migration Complete
-- ============================================================================

SELECT '✅ User security & tracking fields migration completed successfully!' AS status;
SELECT 'Total columns added: 17 (12 security + 5 tracking)' AS summary;
SELECT 'Run: CALL get_user_security_stats(); for security stats' AS help1;
SELECT 'Run: CALL get_login_tracking_stats(); for tracking stats' AS help2;
SELECT 'Run: CALL find_suspicious_logins(); for security alerts' AS help3;
SELECT 'Run: SELECT * FROM v_user_login_activity; to view user activity' AS help4;