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

 Date: 16/07/2025 16:49:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `is_ai_generated` bit(1) NULL DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `metadata` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `parent_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sender_id` bigint NOT NULL,
  `sender_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sender_type` enum('USER','ASSISTANT','SYSTEM') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('SENT','DELIVERED','READ','FAILED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` enum('TEXT','IMAGE','FILE','SYSTEM','AI_RESPONSE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `chat_room_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_av2veaoqxgr0pvttqd5egcot2`(`message_id` ASC) USING BTREE,
  INDEX `idx_chat_room_id`(`chat_room_id` ASC) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_messages_room_created`(`chat_room_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `FKbcsxusjp1v4rd8879fhvq8ssb` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------
INSERT INTO `chat_messages` VALUES (1, 'Chán quá bạn ơi', '2025-07-16 08:32:33.271927', b'0', b'0', b'0', '7b52d696-2fbd-489b-98ef-a98c3cf0d190', NULL, NULL, 1, 'User 1', 'USER', 'SENT', 'TEXT', '2025-07-16 08:32:33.271941', 1);
INSERT INTO `chat_messages` VALUES (2, 'I\'m happy to help! It seems like you\'re having a bit of a tough day. Don\'t worry, I\'m here to assist you with any questions or concerns you may have about our restaurant. Whether it\'s about ordering food, making a reservation, or something else, I\'m here to help.\n\nCan you please tell me what\'s on your mind? What do you need help with today?', '2025-07-16 08:32:34.440403', b'0', b'1', b'0', '4c007b84-1bee-4ed8-a172-02c8c281f68d', NULL, NULL, 1, 'AI Assistant', 'ASSISTANT', 'SENT', 'AI_RESPONSE', '2025-07-16 08:32:34.440420', 1);
INSERT INTO `chat_messages` VALUES (3, 'I\'m happy to help! It seems like you\'re having a bit of a tough day. Don\'t worry, I\'m here to assist you with any questions or concerns you may have about our restaurant. Whether it\'s about ordering food, making a reservation, or something else, I\'m here to help.\n\nCan you please tell me what\'s on your mind? What do you need help with today?', '2025-07-16 08:32:34.493669', b'0', b'1', b'0', '4e6cd79a-28d3-41bc-8077-00842fea1a91', NULL, NULL, 1, 'AI Assistant', 'ASSISTANT', 'SENT', 'TEXT', '2025-07-16 08:32:34.493684', 1);
INSERT INTO `chat_messages` VALUES (4, 'aa', '2025-07-16 09:02:22.579832', b'0', b'0', b'0', '71b161be-2dfc-4c6e-9e04-2beee72050fb', NULL, NULL, 1, 'User 1', 'USER', 'SENT', 'TEXT', '2025-07-16 09:02:22.579851', 2);
INSERT INTO `chat_messages` VALUES (5, 'Hello! It seems like there might have been a small mistake. Could you please rephrase or provide more information about what you need help with? Are you looking to place an order, check on the status of a reservation, or perhaps ask about a specific menu item? I\'m here to assist you and provide support for your restaurant needs.', '2025-07-16 09:02:23.391268', b'0', b'1', b'0', '52ad63e2-b74e-4180-98b8-2702f880084c', NULL, NULL, 1, 'AI Assistant', 'ASSISTANT', 'SENT', 'AI_RESPONSE', '2025-07-16 09:02:23.391281', 2);
INSERT INTO `chat_messages` VALUES (6, 'Hello! It seems like there might have been a small mistake. Could you please rephrase or provide more information about what you need help with? Are you looking to place an order, check on the status of a reservation, or perhaps ask about a specific menu item? I\'m here to assist you and provide support for your restaurant needs.', '2025-07-16 09:02:23.419442', b'0', b'1', b'0', '4dff861c-481a-44e3-aba3-1cb011625a35', NULL, NULL, 1, 'AI Assistant', 'ASSISTANT', 'SENT', 'TEXT', '2025-07-16 09:02:23.419449', 2);
INSERT INTO `chat_messages` VALUES (7, 'hello', '2025-07-16 09:34:11.536209', b'0', b'0', b'0', 'e61af7eb-79b0-40e8-8e14-8617ca67f5d4', NULL, NULL, 2, 'User 2', 'USER', 'SENT', 'TEXT', '2025-07-16 09:34:11.536215', 3);
INSERT INTO `chat_messages` VALUES (8, 'Hello! Welcome to our restaurant management system. I\'m happy to assist you with any questions or concerns you may have. Whether you\'re a customer, a server, or a manager, I\'m here to help. Please feel free to ask me anything about our menu, orders, reservations, or general restaurant operations. I\'ll do my best to provide you with accurate and helpful information. What can I help you with today?', '2025-07-16 09:34:12.627568', b'0', b'1', b'0', 'acd00a6c-4920-4a12-87d6-46600651b3ce', NULL, NULL, 2, 'AI Assistant', 'ASSISTANT', 'SENT', 'AI_RESPONSE', '2025-07-16 09:34:12.627575', 3);
INSERT INTO `chat_messages` VALUES (9, 'Hello! Welcome to our restaurant management system. I\'m happy to assist you with any questions or concerns you may have. Whether you\'re a customer, a server, or a manager, I\'m here to help. Please feel free to ask me anything about our menu, orders, reservations, or general restaurant operations. I\'ll do my best to provide you with accurate and helpful information. What can I help you with today?', '2025-07-16 09:34:12.663115', b'0', b'1', b'0', 'c6801786-42c3-45f6-9cc2-8ed23f673366', NULL, NULL, 2, 'AI Assistant', 'ASSISTANT', 'SENT', 'TEXT', '2025-07-16 09:34:12.663127', 3);
INSERT INTO `chat_messages` VALUES (10, 'bạn có thẻ nói tiếng việt không?', '2025-07-16 09:34:29.293720', b'0', b'0', b'0', '0bb9c76b-3bc0-4ab9-853f-5c39571acffc', NULL, NULL, 2, 'User 2', 'USER', 'SENT', 'TEXT', '2025-07-16 09:34:29.293726', 3);
INSERT INTO `chat_messages` VALUES (11, 'Cảm ơn! Tôi có thể nói tiếng Việt! Tôi là một trợ lý AI giúp đỡ hệ thống quản trị nhà hàng. Bạn có thể hỏi tôi về các vấn đề như đặt hàng, món ăn, đặt chỗ, và các hoạt động của nhà hàng. Tôi sẽ cố gắng trả lời hết sức!', '2025-07-16 09:34:31.555529', b'0', b'1', b'0', '28206b1e-be6a-48b2-aa04-f2a7bea0691e', NULL, NULL, 2, 'AI Assistant', 'ASSISTANT', 'SENT', 'AI_RESPONSE', '2025-07-16 09:34:31.555535', 3);
INSERT INTO `chat_messages` VALUES (12, 'Cảm ơn! Tôi có thể nói tiếng Việt! Tôi là một trợ lý AI giúp đỡ hệ thống quản trị nhà hàng. Bạn có thể hỏi tôi về các vấn đề như đặt hàng, món ăn, đặt chỗ, và các hoạt động của nhà hàng. Tôi sẽ cố gắng trả lời hết sức!', '2025-07-16 09:34:31.622746', b'0', b'1', b'0', '2ba227f5-c636-44f8-85ac-a82ab1e34883', NULL, NULL, 2, 'AI Assistant', 'ASSISTANT', 'SENT', 'TEXT', '2025-07-16 09:34:31.622753', 3);

-- ----------------------------
-- Table structure for chat_participants
-- ----------------------------
DROP TABLE IF EXISTS `chat_participants`;
CREATE TABLE `chat_participants`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `joined_at` datetime(6) NULL DEFAULT NULL,
  `last_active_at` datetime(6) NOT NULL,
  `last_read_at` datetime(6) NULL DEFAULT NULL,
  `left_at` datetime(6) NULL DEFAULT NULL,
  `role` enum('OWNER','ADMIN','MEMBER','AI_BOT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('ACTIVE','INACTIVE','BANNED','LEFT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `chat_room_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKk0pkxdxk2whcy4j9lovmexvkq`(`chat_room_id` ASC) USING BTREE,
  CONSTRAINT `FKk0pkxdxk2whcy4j9lovmexvkq` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
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
  `created_at` datetime(6) NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `room_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `session_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('ACTIVE','INACTIVE','ARCHIVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `type` enum('PRIVATE','GROUP','AI') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK7ew9642abd4gt5cur86pq9y0b`(`user_id` ASC, `session_id` ASC) USING BTREE,
  UNIQUE INDEX `UK_tmf6ectlc88brf0myju8ry0yt`(`room_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_rooms
-- ----------------------------
INSERT INTO `chat_rooms` VALUES (1, '2025-07-16 08:32:33.170320', 'AI assistant chat', 'Chat with AI', 'room-1752551791890', 'a320e17c-1334-4b11-9819-1a3bfd2e1db1', 'ACTIVE', 'AI', '2025-07-16 08:32:33.170355', 1);
INSERT INTO `chat_rooms` VALUES (2, '2025-07-16 09:02:22.542648', 'AI assistant chat', 'Chat with AI', 'chat-1752656537333', 'chat-1752656537333', 'ACTIVE', 'AI', '2025-07-16 09:02:22.542666', 1);
INSERT INTO `chat_rooms` VALUES (3, '2025-07-16 09:34:11.477066', 'AI assistant chat', 'Chat with AI', 'chat-1752658444023', 'chat-1752658444023', 'ACTIVE', 'AI', '2025-07-16 09:34:11.477094', 2);

SET FOREIGN_KEY_CHECKS = 1;
