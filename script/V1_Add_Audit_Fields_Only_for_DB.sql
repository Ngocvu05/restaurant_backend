-- =====================================================
-- ADDON MIGRATION: Add Audit & Soft Delete Fields
-- Run this on existing database
-- Date: 2025-01-01
-- =====================================================

USE restaurant;
SET FOREIGN_KEY_CHECKS = 0;

-- ===== 1. USERS TABLE =====
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) COMMENT 'Username who created this record',
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
                                                                           ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) COMMENT 'Username who last updated this record',
                                                                           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp',
                                                                           ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100) COMMENT 'Username who deleted this record',
                                                                           ADD COLUMN IF NOT EXISTS status ENUM('ACTIVE','INACTIVE','BANNED') NOT NULL DEFAULT 'ACTIVE' COMMENT 'User status';

-- Add indexes if not exist
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

UPDATE users
SET created_by = COALESCE(created_by, 'system'),
    updated_by = COALESCE(updated_by, 'system')
WHERE created_by IS NULL OR updated_by IS NULL;

-- ===== 2. DISHES TABLE =====
ALTER TABLE dishes
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) COMMENT 'Username who created this record',
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
                                                                           ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) COMMENT 'Username who last updated this record',
                                                                           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp',
                                                                           ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100) COMMENT 'Username who deleted this record',
                                                                           ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic locking version',
                                                                           ADD COLUMN IF NOT EXISTS average_rating DECIMAL(3,2) DEFAULT 0.00 COMMENT 'Average rating',
                                                                           ADD COLUMN IF NOT EXISTS total_reviews INT NOT NULL DEFAULT 0 COMMENT 'Total reviews count';

CREATE INDEX IF NOT EXISTS idx_dishes_deleted_at ON dishes(deleted_at);
CREATE INDEX IF NOT EXISTS idx_dishes_created_at ON dishes(created_at);
CREATE INDEX IF NOT EXISTS idx_dishes_category ON dishes(category);
CREATE INDEX IF NOT EXISTS idx_dishes_available ON dishes(available);
CREATE INDEX IF NOT EXISTS idx_dishes_order_count ON dishes(order_count);
CREATE INDEX IF NOT EXISTS idx_dishes_average_rating ON dishes(average_rating);

UPDATE dishes
SET created_by = COALESCE(created_by, 'system'),
    updated_by = COALESCE(updated_by, 'system'),
    version = COALESCE(version, 0),
    average_rating = COALESCE(average_rating, 0.00),
    total_reviews = COALESCE(total_reviews, 0);

-- ===== 3. BOOKINGS TABLE =====
ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) COMMENT 'Username who created this record',
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
                                                                           ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) COMMENT 'Username who last updated this record',
                                                                           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp',
                                                                           ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100) COMMENT 'Username who deleted this record';

CREATE INDEX IF NOT EXISTS idx_bookings_deleted_at ON bookings(deleted_at);
CREATE INDEX IF NOT EXISTS idx_bookings_created_at ON bookings(created_at);
CREATE INDEX IF NOT EXISTS idx_bookings_booking_time ON bookings(booking_time);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);

UPDATE bookings
SET created_by = COALESCE(created_by, 'system'),
    updated_by = COALESCE(updated_by, 'system');

-- ===== 4. TABLES TABLE =====
-- Update enum first
ALTER TABLE `tables`
    MODIFY COLUMN status ENUM('AVAILABLE','OCCUPIED','RESERVED','MAINTENANCE','BOOKED') DEFAULT 'AVAILABLE';

ALTER TABLE `tables`
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) COMMENT 'Username who created this record',
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
                                                                           ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) COMMENT 'Username who last updated this record',
                                                                           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp',
                                                                           ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100) COMMENT 'Username who deleted this record';

CREATE INDEX IF NOT EXISTS idx_tables_deleted_at ON `tables`(deleted_at);
CREATE INDEX IF NOT EXISTS idx_tables_created_at ON `tables`(created_at);
CREATE INDEX IF NOT EXISTS idx_tables_status ON `tables`(status);
CREATE INDEX IF NOT EXISTS idx_tables_capacity ON `tables`(capacity);

UPDATE `tables`
SET created_by = COALESCE(created_by, 'system'),
    updated_by = COALESCE(updated_by, 'system');

-- ===== 5. PAYMENTS TABLE =====
-- Update enum to match Java entity
ALTER TABLE payments
    MODIFY COLUMN status ENUM('PENDING','COMPLETED','FAILED','CANCELLED','REFUNDED') DEFAULT 'PENDING';

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) COMMENT 'Username who created this record',
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
                                                                           ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) COMMENT 'Username who last updated this record',
                                                                           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp',
                                                                           ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100) COMMENT 'Username who deleted this record',
                                                                           ADD COLUMN IF NOT EXISTS transaction_reference VARCHAR(100) COMMENT 'Transaction reference ID',
                                                                           ADD COLUMN IF NOT EXISTS customer_note TEXT COMMENT 'Customer note',
                                                                           ADD COLUMN IF NOT EXISTS admin_note TEXT COMMENT 'Admin note',
                                                                           ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP NULL COMMENT 'When payment was processed',
                                                                           ADD COLUMN IF NOT EXISTS processed_by VARCHAR(100) COMMENT 'Admin who processed payment';

CREATE INDEX IF NOT EXISTS idx_payments_deleted_at ON payments(deleted_at);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_booking_id ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payments_transaction_ref ON payments(transaction_reference);

UPDATE payments
SET created_by = COALESCE(created_by, 'system'),
    updated_by = COALESCE(updated_by, 'system');

-- ===== 6. ORDER_HISTORY TABLE =====
ALTER TABLE order_history
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) COMMENT 'Username who created this record',
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
                                                                           ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) COMMENT 'Username who last updated this record';

CREATE INDEX IF NOT EXISTS idx_order_history_created_at ON order_history(created_at);
CREATE INDEX IF NOT EXISTS idx_order_history_booking_id ON order_history(booking_id);
CREATE INDEX IF NOT EXISTS idx_order_history_user_id ON order_history(user_id);
CREATE INDEX IF NOT EXISTS idx_order_history_dish_id ON order_history(dish_id);

UPDATE order_history
SET created_by = COALESCE(created_by, 'system'),
    updated_by = COALESCE(updated_by, 'system');

-- ===== 7. PREORDERS TABLE =====
ALTER TABLE preorders
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) COMMENT 'Username who created this record',
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
                                                                           ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) COMMENT 'Username who last updated this record';

CREATE INDEX IF NOT EXISTS idx_preorders_created_at ON preorders(created_at);
CREATE INDEX IF NOT EXISTS idx_preorders_booking_id ON preorders(booking_id);
CREATE INDEX IF NOT EXISTS idx_preorders_dish_id ON preorders(dish_id);

UPDATE preorders
SET created_by = COALESCE(created_by, 'system'),
    updated_by = COALESCE(updated_by, 'system');

-- ===== 8. IMAGES TABLE =====
ALTER TABLE images
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) COMMENT 'Username who created this record',
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
                                                                           ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) COMMENT 'Username who last updated this record',
                                                                           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp',
                                                                           ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100) COMMENT 'Username who deleted this record';

CREATE INDEX IF NOT EXISTS idx_images_deleted_at ON images(deleted_at);
CREATE INDEX IF NOT EXISTS idx_images_uploaded_at ON images(uploaded_at);

UPDATE images
SET created_by = COALESCE(created_by, 'system'),
    updated_by = COALESCE(updated_by, 'system');

-- ===== 9. NOTIFICATIONS TABLE =====
ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) COMMENT 'Username who created this record',
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
                                                                           ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) COMMENT 'Username who last updated this record';

CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);

UPDATE notifications
SET created_by = COALESCE(created_by, 'system'),
    updated_by = COALESCE(updated_by, 'system');

-- ===== 10. USER_ROLES TABLE =====
ALTER TABLE user_roles
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) COMMENT 'Username who created this record',
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
                                                                           ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) COMMENT 'Username who last updated this record';

UPDATE user_roles
SET created_by = COALESCE(created_by, 'system'),
    updated_by = COALESCE(updated_by, 'system');

-- ===== 11. CREATE REVIEWS TABLE =====
CREATE TABLE IF NOT EXISTS `reviews` (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `dish_id` bigint NOT NULL COMMENT 'Dish being reviewed',
                                         `customer_name` varchar(100) NOT NULL COMMENT 'Customer name',
                                         `customer_email` varchar(255) DEFAULT NULL COMMENT 'Customer email',
                                         `customer_avatar` varchar(500) DEFAULT NULL COMMENT 'Customer avatar URL',
                                         `rating` int NOT NULL COMMENT 'Rating 1-5 stars',
                                         `comment` text NOT NULL COMMENT 'Review comment',
                                         `is_active` bit(1) NOT NULL DEFAULT b'1' COMMENT 'Is review active',
                                         `is_verified` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Is review verified',
                                         `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP address of reviewer',
                                         `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         `created_by` varchar(100) DEFAULT 'system',
                                         `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                         `updated_by` varchar(100) DEFAULT NULL,
                                         `deleted_at` timestamp NULL DEFAULT NULL,
                                         `deleted_by` varchar(100) DEFAULT NULL,
                                         PRIMARY KEY (`id`),
                                         KEY `idx_dish_id` (`dish_id`),
                                         KEY `idx_customer_email` (`customer_email`),
                                         KEY `idx_rating` (`rating`),
                                         KEY `idx_is_active` (`is_active`),
                                         KEY `idx_reviews_deleted_at` (`deleted_at`),
                                         KEY `idx_reviews_created_at` (`created_at`),
                                         CONSTRAINT `fk_reviews_dish` FOREIGN KEY (`dish_id`) REFERENCES `dishes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Customer reviews for dishes';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- VERIFICATION
-- =====================================================
SELECT 'Migration completed successfully!' as status;

-- Show added columns
SELECT
    TABLE_NAME,
    COUNT(*) as total_columns
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'restaurant'
  AND COLUMN_NAME IN ('created_by', 'updated_by', 'deleted_at', 'deleted_by')
GROUP BY TABLE_NAME
ORDER BY TABLE_NAME;