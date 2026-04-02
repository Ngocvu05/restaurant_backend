-- ========================================
-- COMPREHENSIVE PERMISSION CHECK
-- ========================================
-- Run this with ROOT user

SELECT '========== CHECKING DATABASES ==========' AS '';

-- List all databases
SHOW DATABASES;

SELECT '========== CHECKING USERS ==========' AS '';

-- List users
SELECT User, Host, plugin, authentication_string
FROM mysql.user
WHERE User IN ('root', 'restaurant_user')
ORDER BY User;

SELECT '========== CHECKING restaurant_user PERMISSIONS ==========' AS '';

-- Show grants for restaurant_user
SHOW GRANTS FOR 'restaurant_user'@'%';

SELECT '========== CHECKING DATABASE SIZES ==========' AS '';

-- Show database sizes
SELECT
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)',
    COUNT(*) AS 'Tables'
FROM information_schema.tables
WHERE table_schema IN ('restaurant', 'analytics', 'restaurant_chat')
GROUP BY table_schema
ORDER BY table_schema;

SELECT '========== CHECKING ANALYTICS TABLES ==========' AS '';

-- List tables in analytics database
SELECT TABLE_NAME, TABLE_ROWS,
       ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'analytics'
ORDER BY TABLE_NAME;

SELECT '========== TESTING restaurant_user ACCESS ==========' AS '';

-- Test if restaurant_user can access analytics
-- Note: This must be run separately as restaurant_user
SELECT 'To test access, run these commands as restaurant_user:' AS 'Instructions';
SELECT 'USE analytics;' AS 'Command 1';
SELECT 'SHOW TABLES;' AS 'Command 2';
SELECT 'SELECT COUNT(*) FROM user_analytics;' AS 'Command 3';

SELECT '========== PERMISSIONS SUMMARY ==========' AS '';

-- Summary of permissions
SELECT
    GRANTEE,
    TABLE_SCHEMA,
    PRIVILEGE_TYPE
FROM information_schema.SCHEMA_PRIVILEGES
WHERE GRANTEE LIKE '%restaurant_user%'
ORDER BY TABLE_SCHEMA, PRIVILEGE_TYPE;