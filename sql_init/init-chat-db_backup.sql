-- Tạo database và user
CREATE DATABASE IF NOT EXISTS restaurant_chat;
CREATE USER IF NOT EXISTS 'restaurant_user'@'%' IDENTIFIED BY 'restaurant_pass';
GRANT ALL PRIVILEGES ON restaurant_chat.* TO 'restaurant_user'@'%';
FLUSH PRIVILEGES;

-- Sử dụng database
USE restaurant_chat;

-- Tạo bảng chat_rooms
CREATE TABLE chat_rooms (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            room_id VARCHAR(255) NOT NULL UNIQUE,
                            name VARCHAR(255) NOT NULL,
                            user_id BIGINT,
                            session_id VARCHAR(255) NOT NULL,
                            type ENUM('PRIVATE', 'GROUP', 'AI_SUPPORT', 'CUSTOMER_SERVICE') NOT NULL,
                            status ENUM('ACTIVE', 'INACTIVE', 'ARCHIVED', 'DELETED') DEFAULT 'ACTIVE',
                            description TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Tạo unique constraint
                            UNIQUE KEY unique_user_session (user_id, session_id),

    -- Tạo indexes
                            INDEX idx_room_id (room_id),
                            INDEX idx_user_id (user_id),
                            INDEX idx_session_id (session_id),
                            INDEX idx_status (status),
                            INDEX idx_created_at (created_at)
);

-- Tạo bảng chat_participants
CREATE TABLE chat_participants (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   chat_room_id BIGINT NOT NULL,
                                   user_id BIGINT NOT NULL,
                                   user_name VARCHAR(255) NOT NULL,
                                   role ENUM('ADMIN', 'MODERATOR', 'MEMBER', 'GUEST', 'AI_ASSISTANT') NOT NULL,
                                   status ENUM('ACTIVE', 'INACTIVE', 'BANNED', 'LEFT') DEFAULT 'ACTIVE',
                                   joined_at TIMESTAMP NULL,
                                   left_at TIMESTAMP NULL,
                                   last_read_at TIMESTAMP NULL,
                                   last_active_at TIMESTAMP NULL,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key constraint
                                   FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,

    -- Tạo indexes
                                   INDEX idx_chat_room_id (chat_room_id),
                                   INDEX idx_user_id (user_id),
                                   INDEX idx_role (role),
                                   INDEX idx_status (status),
                                   INDEX idx_joined_at (joined_at),
                                   INDEX idx_last_active_at (last_active_at)
);

-- Tạo bảng chat_messages
CREATE TABLE chat_messages (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               message_id VARCHAR(255) NOT NULL UNIQUE,
                               chat_room_id BIGINT NOT NULL,
                               sender_id BIGINT NOT NULL,
                               sender_name VARCHAR(255) NOT NULL,
                               type ENUM('TEXT', 'IMAGE', 'FILE', 'AUDIO', 'VIDEO', 'SYSTEM', 'NOTIFICATION') DEFAULT 'TEXT',
                               content TEXT NOT NULL,
                               parent_message_id VARCHAR(255),
                               status ENUM('SENT', 'DELIVERED', 'READ', 'FAILED', 'DELETED') DEFAULT 'SENT',
                               is_ai_generated BOOLEAN DEFAULT FALSE,
                               sender_type ENUM('USER', 'AI', 'ADMIN', 'GUEST') DEFAULT 'USER',
                               metadata TEXT,
                               is_read BOOLEAN DEFAULT FALSE,
                               deleted BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key constraints
                               FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
                               FOREIGN KEY (parent_message_id) REFERENCES chat_messages(message_id) ON DELETE SET NULL,

    -- Tạo indexes theo entity
                               INDEX idx_chat_room_id (chat_room_id),
                               INDEX idx_message_id (message_id),
                               INDEX idx_sender_id (sender_id),
                               INDEX idx_created_at (created_at),
                               INDEX idx_messages_room_created (chat_room_id, created_at),
                               INDEX idx_parent_message_id (parent_message_id),
                               INDEX idx_status (status),
                               INDEX idx_sender_type (sender_type),
                               INDEX idx_is_ai_generated (is_ai_generated),
                               INDEX idx_is_read (is_read),
                               INDEX idx_deleted (deleted)
);

-- Tạo trigger để tự động generate UUID cho message_id nếu không có
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

-- Tạo trigger để tự động generate UUID cho room_id nếu không có
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

-- Flush privileges
FLUSH PRIVILEGES;

-- Hiển thị thông tin tables đã tạo
SHOW TABLES;
DESCRIBE chat_rooms;
DESCRIBE chat_participants;
DESCRIBE chat_messages;