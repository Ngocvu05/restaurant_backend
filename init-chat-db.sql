-- Tạo database và user
CREATE DATABASE IF NOT EXISTS restaurant_chat;
CREATE USER IF NOT EXISTS 'restaurant_user'@'%' IDENTIFIED BY 'restaurant_pass';
GRANT ALL PRIVILEGES ON restaurant_chat.* TO 'restaurant_user'@'%';
FLUSH PRIVILEGES;

-- Sử dụng database
USE restaurant_chat;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_rooms
-- ----------------------------
DROP TABLE IF EXISTS `chat_rooms`;
CREATE TABLE `chat_rooms` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_id` VARCHAR(255) NOT NULL UNIQUE,
  `name` VARCHAR(255) NOT NULL,
  `user_id` BIGINT,
  `session_id` VARCHAR(255) NOT NULL,
  `type` ENUM('PRIVATE', 'GROUP', 'AI_SUPPORT', 'CUSTOMER_SERVICE') NOT NULL,
  `status` ENUM('ACTIVE', 'INACTIVE', 'ARCHIVED', 'DELETED') DEFAULT 'ACTIVE',
  `description` TEXT,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `admin_id` BIGINT NULL DEFAULT NULL,
  `resolved` BIT(1) NULL DEFAULT NULL,
  UNIQUE KEY `unique_user_session` (`user_id`, `session_id`),
  INDEX `idx_room_id` (`room_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_session_id` (`session_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for chat_participants
-- ----------------------------
DROP TABLE IF EXISTS `chat_participants`;
CREATE TABLE `chat_participants` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `chat_room_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `user_name` VARCHAR(255) NOT NULL,
  `role` ENUM('ADMIN', 'MODERATOR', 'MEMBER', 'GUEST', 'AI_ASSISTANT') NOT NULL,
  `status` ENUM('ACTIVE', 'INACTIVE', 'BANNED', 'LEFT') DEFAULT 'ACTIVE',
  `joined_at` TIMESTAMP NULL,
  `left_at` TIMESTAMP NULL,
  `last_read_at` TIMESTAMP NULL,
  `last_active_at` TIMESTAMP NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_chat_room_id` (`chat_room_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_role` (`role`),
  INDEX `idx_status` (`status`),
  INDEX `idx_joined_at` (`joined_at`),
  INDEX `idx_last_active_at` (`last_active_at`),
  FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `message_id` VARCHAR(255) NOT NULL UNIQUE,
  `chat_room_id` BIGINT NOT NULL,
  `sender_id` BIGINT NOT NULL,
  `sender_name` VARCHAR(255) NOT NULL,
  `type` ENUM('TEXT','IMAGE','FILE','AUDIO','VIDEO','SYSTEM','NOTIFICATION') DEFAULT 'TEXT',
  `content` TEXT NOT NULL,
  `parent_message_id` VARCHAR(255) DEFAULT NULL,
  `status` ENUM('SENT','DELIVERED','READ','FAILED','DELETED') DEFAULT 'SENT',
  `is_ai_generated` BOOLEAN DEFAULT FALSE,
  `sender_type` ENUM('USER','AI','ADMIN','GUEST') DEFAULT 'USER',
  `metadata` TEXT,
  `is_read` BOOLEAN DEFAULT FALSE,
  `deleted` BOOLEAN DEFAULT FALSE,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_chat_room_id` (`chat_room_id`),
  INDEX `idx_message_id` (`message_id`),
  INDEX `idx_sender_id` (`sender_id`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_messages_room_created` (`chat_room_id`, `created_at`),
  INDEX `idx_parent_message_id` (`parent_message_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_sender_type` (`sender_type`),
  INDEX `idx_is_ai_generated` (`is_ai_generated`),
  INDEX `idx_is_read` (`is_read`),
  INDEX `idx_deleted` (`deleted`),
  FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  FOREIGN KEY (`parent_message_id`) REFERENCES `chat_messages` (`message_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Sample Data: chat_rooms
-- ----------------------------
INSERT INTO `chat_rooms` VALUES 
(1, 'chat-1753086221810', 'Chat with AI', 2, 'chat-1753086221810', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 08:29:19', '2025-07-21 08:40:15', NULL, NULL),
(2, 'chat-1753086958975', 'Chat with AI', NULL, 'chat-1753086958975', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 08:36:29', '2025-07-21 08:36:29', NULL, NULL),
(3, 'chat-1753088362991', 'Chat with AI', 2, 'chat-1753088362991', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 09:03:23', '2025-07-21 09:03:31', NULL, NULL),
(4, 'chat-1753088989973', 'Chat with AI', 2, 'chat-1753088989973', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 09:10:49', '2025-07-21 09:11:21', NULL, NULL),
(5, 'chat-1753089433181', 'Chat with AI', 2, 'chat-1753089433181', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 09:17:21', '2025-07-21 09:17:26', NULL, NULL),
(6, 'chat-1753089786982', 'Chat with AI', 2, 'chat-1753089786982', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 09:26:46', '2025-07-21 09:26:46', NULL, NULL);

-- ----------------------------
-- Sample Data: chat_messages
-- ----------------------------
INSERT INTO `chat_messages` VALUES 
(1, 'b4150dc2-ed9f-4f59-b065-17fe16ef1d20', 6, 2, 'User 2', 'TEXT', 'giờ thì sao?', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-21 09:26:46', '2025-07-21 09:26:46'),
(2, '37af27b9-8ee3-4bd4-b82d-47977d1f949c', 6, 2, 'AI Assistant', 'TEXT', '\"Chào! Hiện tại, các bàn đang được phục vụ. Chúng tôi có một số bàn trống vào lúc 7h tối. Bạn có thể đặt bàn hay cần hỗ trợ gì khác?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-21 09:26:52', '2025-07-21 09:26:52');

-- ----------------------------
-- Trigger: auto generate message_id
-- ----------------------------
DELIMITER $$
CREATE TRIGGER chat_messages_before_insert
BEFORE INSERT ON chat_messages
FOR EACH ROW
BEGIN
    IF NEW.message_id IS NULL OR NEW.message_id = '' THEN
        SET NEW.message_id = UUID();
    END IF;
END$$
DELIMITER ;

-- ----------------------------
-- Trigger: auto generate room_id
-- ----------------------------
DELIMITER $$
CREATE TRIGGER chat_rooms_before_insert
BEFORE INSERT ON chat_rooms
FOR EACH ROW
BEGIN
    IF NEW.room_id IS NULL OR NEW.room_id = '' THEN
        SET NEW.room_id = UUID();
    END IF;
END$$
DELIMITER ;

-- Cấp quyền chi tiết cho user
GRANT SELECT, INSERT, UPDATE, DELETE ON restaurant_chat.chat_rooms TO 'restaurant_user'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON restaurant_chat.chat_participants TO 'restaurant_user'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON restaurant_chat.chat_messages TO 'restaurant_user'@'%';
GRANT EXECUTE ON restaurant_chat.* TO 'restaurant_user'@'%';

-- Final setup
FLUSH PRIVILEGES;

-- Optional: Kiểm tra lại
SHOW TABLES;
DESCRIBE chat_rooms;
DESCRIBE chat_participants;
DESCRIBE chat_messages;
