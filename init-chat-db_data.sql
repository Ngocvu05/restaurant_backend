/*
 Navicat Premium Data Transfer

 Source Server         : restaurant
 Source Server Type    : MySQL
 Source Server Version : 80405 (8.4.5)
 Source Host           : localhost:3306
 Source Schema         : restaurant_chat

 Target Server Type    : MySQL
 Target Server Version : 80405 (8.4.5)
 File Encoding         : 65001

 Date: 24/07/2025 16:31:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `chat_room_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  `sender_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` enum('TEXT','IMAGE','FILE','AUDIO','VIDEO','SYSTEM','NOTIFICATION') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'TEXT',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `parent_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` enum('SENT','DELIVERED','READ','FAILED','DELETED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'SENT',
  `is_ai_generated` tinyint(1) NULL DEFAULT 0,
  `sender_type` enum('USER','AI','ADMIN','GUEST') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'USER',
  `metadata` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `is_read` tinyint(1) NULL DEFAULT 0,
  `deleted` tinyint(1) NULL DEFAULT 0,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_chat_room_id`(`chat_room_id` ASC) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_messages_room_created`(`chat_room_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_parent_message_id`(`parent_message_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sender_type`(`sender_type` ASC) USING BTREE,
  INDEX `idx_is_ai_generated`(`is_ai_generated` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_deleted`(`deleted` ASC) USING BTREE,
  CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chat_messages_ibfk_2` FOREIGN KEY (`parent_message_id`) REFERENCES `chat_messages` (`message_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------
INSERT INTO `chat_messages` VALUES (1, 'b4150dc2-ed9f-4f59-b065-17fe16ef1d20', 6, 2, 'User 2', 'TEXT', 'giờ thì sao?', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-21 09:26:46', '2025-07-21 09:26:46');
INSERT INTO `chat_messages` VALUES (2, '37af27b9-8ee3-4bd4-b82d-47977d1f949c', 6, 2, 'AI Assistant', 'TEXT', '\"Chào! Hiện tại, các bàn đang được phục vụ. Chúng tôi có một số bàn trống vào lúc 7h tối. Bạn có thể đặt bàn hay cần hỗ trợ gì khác?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-21 09:26:52', '2025-07-21 09:26:52');
INSERT INTO `chat_messages` VALUES (3, '0f4b8393-33e4-4762-9a86-daa97c4eac7c', 7, 2, 'User 2', 'TEXT', 'sau login thì sao ha', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 03:35:50', '2025-07-24 03:35:50');
INSERT INTO `chat_messages` VALUES (4, 'f044347e-64a4-4607-93b9-7c0219648de5', 7, 2, 'AI Assistant', 'TEXT', '\"Xin chào! Bạn đã đăng nhập thành công! Bây giờ, bạn có thể xem và quản lý các đơn hàng, menu, reserva và các hoạt động khác của quán ăn. Nếu bạn cần giúp đỡ về bất cứ điều gì, vui lòng hỏi tôi!\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-24 03:35:51', '2025-07-24 03:35:51');
INSERT INTO `chat_messages` VALUES (5, 'bf86e79c-3278-490b-9793-a25c0c4f75fa', 8, 2, 'User 2', 'TEXT', 'okay, giờ tôi sẽ kể cho bạn nghe nhé', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 03:51:51', '2025-07-24 03:51:51');
INSERT INTO `chat_messages` VALUES (6, 'b56d1463-0af8-45d8-83d8-96c6c48e0125', 8, 2, 'AI Assistant', 'TEXT', '\"Ahaha, okay! Tôi sẵn sàng nghe rồi!\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-24 03:51:53', '2025-07-24 03:51:53');
INSERT INTO `chat_messages` VALUES (7, '77c938b8-4a2c-4a4f-a904-b626d381781b', 9, 2, 'User 2', 'TEXT', 'vậy sao, nghe thú vị quá ', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 04:02:56', '2025-07-24 04:02:56');
INSERT INTO `chat_messages` VALUES (8, 'b6ae1c58-0fe4-4dc7-8eb3-df5438615bba', 9, 2, 'AI Assistant', 'TEXT', '\"Haha, cảm ơn bạn rất nhiều! Tôi cũng rất hân hạnh được giúp đỡ. Bạn có nhu cầu đặt hàng, đặt bàn hay hỏi về menu của chúng tôi không?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-24 04:02:57', '2025-07-24 04:02:57');
INSERT INTO `chat_messages` VALUES (9, '49d4f38d-c911-4d1b-ae70-6e40cf52345c', 10, 2, 'User 2', 'TEXT', 'giờ thử lại ha', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 04:13:07', '2025-07-24 04:13:07');
INSERT INTO `chat_messages` VALUES (10, '3399aad5-e532-41b3-8a82-f5da16fedef7', 12, 2, 'User 2', 'TEXT', 'thử thêm 1 lần nữa nha', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 04:32:30', '2025-07-24 04:32:30');
INSERT INTO `chat_messages` VALUES (11, '8d992fbf-34fd-4693-ae20-7c63dc5e1017', 12, 2, 'User 2', 'TEXT', 'ủa sao vậy?', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 04:32:30', '2025-07-24 04:32:30');
INSERT INTO `chat_messages` VALUES (14, 'fea6f713-2164-4108-8e03-09d7ad8fbc87', 16, 2, 'User 2', 'TEXT', 'are u ok?', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 06:42:03', '2025-07-24 06:42:03');
INSERT INTO `chat_messages` VALUES (15, 'bef0e204-4048-49ab-aee2-987e6bb58247', 16, 2, 'AI Assistant', 'TEXT', '\"Haha, yeah I\'m good! I\'m an AI assistant for your restaurant management system, ready to help you with any questions or tasks you need assistance with. What\'s on your mind?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-24 06:42:04', '2025-07-24 06:42:04');
INSERT INTO `chat_messages` VALUES (17, '717c385b-52b3-4c1c-8663-72cd844b60dd', 18, 2, 'User 2', 'TEXT', 'trông có vẻ khả quan đó', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 07:13:15', '2025-07-24 07:13:15');
INSERT INTO `chat_messages` VALUES (19, '7d1dd70d-8b8c-4f8a-9086-37b575498895', 20, 2, 'User 2', 'TEXT', 'giờ thì sao ta', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 07:20:10', '2025-07-24 07:20:10');
INSERT INTO `chat_messages` VALUES (20, '6724b620-37ab-4929-8b7d-2acc3b157868', 20, 2, 'AI Assistant', 'TEXT', '\"Chào! Hiện tại, chúng tôi đang hoạt động bình thường. Bạn có nhu cầu đặt món hay làm gì khác?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-24 07:20:11', '2025-07-24 07:20:11');
INSERT INTO `chat_messages` VALUES (21, '85eca2c4-4d2e-4b06-93ba-7b5ab58cb1c7', 21, 2, 'User 2', 'TEXT', 'ngon ngon', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 07:28:45', '2025-07-24 07:28:45');
INSERT INTO `chat_messages` VALUES (22, '81f837c2-3fe4-4b9f-82bc-4dcca18e325a', 21, 2, 'AI', 'TEXT', 'Xin chào! Bạn có thể giải thích ý nghĩa của \"ngon ngon\" không? Bạn có thể là một khách hàng muốn đặt bàn hay có yêu cầu về món ăn? Tôi sẵn sàng giúp đỡ!', NULL, 'SENT', 1, 'AI', NULL, 1, 0, '2025-07-24 07:28:45', '2025-07-24 07:28:45');
INSERT INTO `chat_messages` VALUES (23, '32c967a0-37f4-4070-879f-4df46c954425', 21, 2, 'User 2', 'TEXT', 'okay, hãy giúp tôi nào', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 07:28:57', '2025-07-24 07:28:57');
INSERT INTO `chat_messages` VALUES (24, '6bcbab09-9e50-49d2-9f9a-c349573cc340', 22, 2, 'User 2', 'TEXT', 'chào tạm biệt', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 07:40:28', '2025-07-24 07:40:28');
INSERT INTO `chat_messages` VALUES (25, '121405a9-e61d-4433-989f-39a7c983e8bf', 22, 2, 'AI', 'TEXT', 'Chào! Tạm biệt!', NULL, 'SENT', 1, 'AI', NULL, 1, 0, '2025-07-24 07:40:28', '2025-07-24 07:40:28');
INSERT INTO `chat_messages` VALUES (26, '004e42cd-52a1-41ad-b507-ef77fba293f6', 22, 2, 'User 2', 'TEXT', 'sao bạn không níu kéo tôi', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-24 07:40:36', '2025-07-24 07:40:36');
INSERT INTO `chat_messages` VALUES (27, 'e2924a54-25f7-4535-994b-9d10465a9e07', 22, 2, 'AI Assistant', 'TEXT', '\"Xin lỗi bạn, tôi không hiểu rõ về yêu cầu \'nіту kéo\'. Bạn có thể giải thích hơn về vấn đề gì bạn đang gặp hay cần trợ giúp với tôi không?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-24 07:40:37', '2025-07-24 07:40:37');

-- ----------------------------
-- Table structure for chat_participants
-- ----------------------------
DROP TABLE IF EXISTS `chat_participants`;
CREATE TABLE `chat_participants`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chat_room_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` enum('ADMIN','MODERATOR','MEMBER','GUEST','AI_ASSISTANT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('ACTIVE','INACTIVE','BANNED','LEFT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ACTIVE',
  `joined_at` timestamp NULL DEFAULT NULL,
  `left_at` timestamp NULL DEFAULT NULL,
  `last_read_at` timestamp NULL DEFAULT NULL,
  `last_active_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chat_room_id`(`chat_room_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_joined_at`(`joined_at` ASC) USING BTREE,
  INDEX `idx_last_active_at`(`last_active_at` ASC) USING BTREE,
  CONSTRAINT `chat_participants_ibfk_1` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_participants
-- ----------------------------

-- ----------------------------
-- Table structure for chat_rooms
-- ----------------------------
DROP TABLE IF EXISTS `chat_rooms`;
CREATE TABLE `chat_rooms`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `session_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` enum('PRIVATE','GROUP','AI_SUPPORT','CUSTOMER_SERVICE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('ACTIVE','INACTIVE','ARCHIVED','DELETED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ACTIVE',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `admin_id` bigint NULL DEFAULT NULL,
  `resolved` bit(1) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `room_id`(`room_id` ASC) USING BTREE,
  UNIQUE INDEX `unique_user_session`(`user_id` ASC, `session_id` ASC) USING BTREE,
  UNIQUE INDEX `UK7ew9642abd4gt5cur86pq9y0b`(`user_id` ASC, `session_id` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_rooms
-- ----------------------------
INSERT INTO `chat_rooms` VALUES (1, 'chat-1753086221810', 'Chat with AI', 2, 'chat-1753086221810', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 08:29:19', '2025-07-21 08:40:15', NULL, NULL);
INSERT INTO `chat_rooms` VALUES (2, 'chat-1753086958975', 'Chat with AI', NULL, 'chat-1753086958975', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 08:36:29', '2025-07-21 08:36:29', NULL, NULL);
INSERT INTO `chat_rooms` VALUES (3, 'chat-1753088362991', 'Chat with AI', 2, 'chat-1753088362991', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 09:03:23', '2025-07-21 09:03:31', NULL, NULL);
INSERT INTO `chat_rooms` VALUES (4, 'chat-1753088989973', 'Chat with AI', 2, 'chat-1753088989973', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 09:10:49', '2025-07-21 09:11:21', NULL, NULL);
INSERT INTO `chat_rooms` VALUES (5, 'chat-1753089433181', 'Chat with AI', 2, 'chat-1753089433181', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 09:17:21', '2025-07-21 09:17:26', NULL, NULL);
INSERT INTO `chat_rooms` VALUES (6, 'chat-1753089786982', 'Chat with AI', 2, 'chat-1753089786982', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 09:26:46', '2025-07-21 09:26:46', NULL, NULL);
INSERT INTO `chat_rooms` VALUES (7, 'chat-1753327779565', 'Chat with AI', 2, 'chat-1753327779565', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat2025-07-24T03:35:49.595533151', '2025-07-24 03:35:50', '2025-07-24 03:35:50', NULL, b'0');
INSERT INTO `chat_rooms` VALUES (8, 'chat-1753328905780', 'Chat with AI', 2, 'chat-1753328905780', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat2025-07-24T03:51:51.030457621', '2025-07-24 03:51:51', '2025-07-24 03:51:51', NULL, b'0');
INSERT INTO `chat_rooms` VALUES (9, 'chat-1753329722841', 'Chat with AI', 2, 'chat-1753329722841', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat2025-07-24T04:02:55.787827270', '2025-07-24 04:02:56', '2025-07-24 04:02:56', NULL, b'0');
INSERT INTO `chat_rooms` VALUES (10, '19d54edd-d22b-4c15-8439-f0bccc7de865', 'Chat Room for Session chat-1753330174990', 2, 'chat-1753330174990', 'AI_SUPPORT', 'ACTIVE', 'Chat room created for session chat-1753330174990', '2025-07-24 04:13:07', '2025-07-24 04:13:07', NULL, b'0');
INSERT INTO `chat_rooms` VALUES (12, '9f443c16-a612-429e-8555-6041c5baa31b', 'Chat Room for Session chat-1753331423799', 2, 'chat-1753331423799', 'AI_SUPPORT', 'ACTIVE', 'Chat room created for session chat-1753331423799', '2025-07-24 04:32:30', '2025-07-24 04:32:30', NULL, b'0');
INSERT INTO `chat_rooms` VALUES (16, 'chat-1753339302278', 'Chat with AI2025-07-24T06:42:02.938747865', 2, 'chat-1753339302278', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat 2025-07-24T06:42:02.938869398', '2025-07-24 06:42:03', '2025-07-24 06:42:03', NULL, b'0');
INSERT INTO `chat_rooms` VALUES (18, 'chat-1753341031548', 'Chat with AI2025-07-24T07:13:15.408098926', 2, 'chat-1753341031548', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat 2025-07-24T07:13:15.408252369', '2025-07-24 07:13:15', '2025-07-24 07:13:15', NULL, b'0');
INSERT INTO `chat_rooms` VALUES (20, 'chat-1753341499752', 'Chat with AI2025-07-24T07:20:09.735767656', 2, 'chat-1753341499752', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat 2025-07-24T07:20:09.736021417', '2025-07-24 07:20:10', '2025-07-24 07:20:10', NULL, b'0');
INSERT INTO `chat_rooms` VALUES (21, '9e7cef53-45d3-4d39-aa18-8bc07470a06a', 'Chat Room for Session chat-1753342113916', 2, 'chat-1753342113916', 'AI_SUPPORT', 'ACTIVE', 'Chat room created for session chat-1753342113916', '2025-07-24 07:28:45', '2025-07-24 07:28:45', NULL, b'0');
INSERT INTO `chat_rooms` VALUES (22, '6a486679-93a3-4990-8064-b608531ae8e0', 'Chat Room for Session chat-1753342733089', 2, 'chat-1753342733089', 'AI_SUPPORT', 'ACTIVE', 'Chat room created for session chat-1753342733089', '2025-07-24 07:40:28', '2025-07-24 07:40:28', NULL, b'0');

-- ----------------------------
-- Triggers structure for table chat_messages
-- ----------------------------
DROP TRIGGER IF EXISTS `chat_messages_before_insert`;
delimiter ;;
CREATE TRIGGER `chat_messages_before_insert` BEFORE INSERT ON `chat_messages` FOR EACH ROW BEGIN
    IF NEW.message_id IS NULL OR NEW.message_id = '' THEN
        SET NEW.message_id = UUID();
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table chat_rooms
-- ----------------------------
DROP TRIGGER IF EXISTS `chat_rooms_before_insert`;
delimiter ;;
CREATE TRIGGER `chat_rooms_before_insert` BEFORE INSERT ON `chat_rooms` FOR EACH ROW BEGIN
    IF NEW.room_id IS NULL OR NEW.room_id = '' THEN
        SET NEW.room_id = UUID();
    END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
