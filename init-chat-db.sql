CREATE DATABASE IF NOT EXISTS restaurant_chat;
GRANT ALL PRIVILEGES ON restaurant_chat.* TO 'restaurant_user'@'%';
FLUSH PRIVILEGES;

-- Create chat_rooms table
CREATE TABLE IF NOT EXISTS chat_rooms (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          user_id BIGINT NOT NULL,
                                          session_id VARCHAR(255) NOT NULL,
                                          name VARCHAR(255) NOT NULL,
                                          active BOOLEAN DEFAULT TRUE,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                          UNIQUE KEY uk_user_session (user_id, session_id),
                                          INDEX idx_user_id (user_id),
                                          INDEX idx_session_id (session_id),
                                          INDEX idx_active (active)
);

-- Create chat_messages table
CREATE TABLE IF NOT EXISTS chat_messages (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             chat_room_id BIGINT NOT NULL,
                                             message_id VARCHAR(255) NOT NULL UNIQUE,
                                             content TEXT NOT NULL,
                                             sender_id BIGINT NOT NULL,
                                             sender_name VARCHAR(255) NOT NULL,
                                             sender_type ENUM('USER', 'AI', 'SYSTEM') NOT NULL DEFAULT 'USER',
                                             parent_message_id VARCHAR(255) NULL,
                                             type ENUM('TEXT', 'IMAGE', 'FILE', 'SYSTEM') NOT NULL DEFAULT 'TEXT',
                                             status ENUM('SENT', 'DELIVERED', 'READ', 'FAILED') NOT NULL DEFAULT 'SENT',
                                             is_ai_generated BOOLEAN DEFAULT FALSE,
                                             is_read BOOLEAN DEFAULT FALSE,
                                             deleted BOOLEAN DEFAULT FALSE,
                                             metadata JSON NULL,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                             FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
                                             FOREIGN KEY (parent_message_id) REFERENCES chat_messages(message_id) ON DELETE SET NULL,

                                             INDEX idx_chat_room_id (chat_room_id),
                                             INDEX idx_message_id (message_id),
                                             INDEX idx_sender_id (sender_id),
                                             INDEX idx_created_at (created_at),
                                             INDEX idx_sender_type (sender_type),
                                             INDEX idx_deleted (deleted)
);

-- Insert default chat rooms if needed
INSERT IGNORE INTO chat_rooms (user_id, session_id, name, active) VALUES
    (1, 'default_session', 'Default Chat Room', TRUE);

-- Create indexes for better performance
CREATE INDEX idx_messages_room_created ON chat_messages(chat_room_id, created_at DESC);
CREATE INDEX idx_messages_unread ON chat_messages(chat_room_id, is_read, created_at DESC);
CREATE INDEX idx_rooms_user_updated ON chat_rooms(user_id, updated_at DESC);