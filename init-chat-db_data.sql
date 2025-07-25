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

 Date: 25/07/2025 16:12:38
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
) ENGINE = InnoDB AUTO_INCREMENT = 74 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
INSERT INTO `chat_messages` VALUES (28, 'fc232a59-457d-4f05-81ba-d9ebe89dca59', 23, 2, 'User 2', 'TEXT', 'chào buổi sáng', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 02:03:40', '2025-07-25 02:03:40');
INSERT INTO `chat_messages` VALUES (29, '10900668-33df-4af4-9aa2-98a478591e99', 23, 2, 'AI Assistant', 'TEXT', '\"Chào buổi sáng! Tôi rất hân hạnh được giúp đỡ quý khách. Bạn có nhu cầu gì về quản lý nhà hàng hôm nay?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 02:03:41', '2025-07-25 02:03:41');
INSERT INTO `chat_messages` VALUES (30, '19e54c50-02dc-4d02-a2cc-bc4a54be350b', 24, 2, 'User 2', 'TEXT', 'hello', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 02:03:47', '2025-07-25 02:03:47');
INSERT INTO `chat_messages` VALUES (31, 'f3325661-05e7-4d16-b1a7-27caebb6c9f4', 24, 2, 'AI Assistant', 'TEXT', 'Xin chào! Tôi là trợ lý AI của hệ thống quản lý nhà hàng. Tôi sẵn sàng giúp đỡ bạn với bất kỳ câu hỏi nào về đơn hàng, món ăn, đặt chỗ hay các vấn đề khác trong hoạt động của nhà hàng. Bạn có thể hỏi tôi gì?', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 02:03:47', '2025-07-25 02:03:47');
INSERT INTO `chat_messages` VALUES (32, '85087104-956b-4e9c-992c-d5fc2905436d', 25, 2, 'User 2', 'TEXT', 'chào bạn nhé groq', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 02:12:43', '2025-07-25 02:12:43');
INSERT INTO `chat_messages` VALUES (33, 'd172d3d9-6381-4f80-979f-654463fbbc29', 25, 2, 'AI Assistant', 'TEXT', 'Xin chào! Đừng gọi là \"bạn nhé\", hãy gọi là \"trợ lý AI\" nhé! Tôi sẵn sàng hỗ trợ bạn với bất kỳ vấn đề nào về quản lý nhà hàng. Bạn cần gì hôm nay?', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 02:12:49', '2025-07-25 02:12:49');
INSERT INTO `chat_messages` VALUES (34, '16bf0863-7f01-49ba-aed6-21339392bba8', 25, 2, 'User 2', 'TEXT', 'giờ chat thì sao ha', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 02:26:46', '2025-07-25 02:26:46');
INSERT INTO `chat_messages` VALUES (35, 'c3164214-a352-4c25-9d54-09ad355e7376', 25, 2, 'AI Assistant', 'TEXT', '\"Haha, rất vui khi chat với bạn! Muốn giúp gì về quản lý nhà hàng hay có thể hỏi gì về menu, đặt bàn, hay đơn hàng? Chúc chúng ta có một cuộc trò chuyện thú vị!\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 02:26:47', '2025-07-25 02:26:47');
INSERT INTO `chat_messages` VALUES (36, 'a0cc2c7b-5602-489c-99f8-788781b41306', 25, 2, 'User 2', 'TEXT', 'giờ chat thì sao ha', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 02:26:55', '2025-07-25 02:26:55');
INSERT INTO `chat_messages` VALUES (37, 'fd88a2af-3b6c-4f22-b0d1-a91438c1d107', 25, 2, 'AI Assistant', 'TEXT', '\"Chào bạn! Cảm ơn vì đã gửi tin nhắn cho tôi. Tôi đang sẵn sàng giúp đỡ bạn với bất kỳ vấn đề nào có關 đến quản lý nhà hàng. Bạn có nhu cầu hỏi gì về đơn hàng, thực đơn, đặt bàn hay hoạt động khác của nhà hàng?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 02:26:56', '2025-07-25 02:26:56');
INSERT INTO `chat_messages` VALUES (38, '122de062-608b-4d2f-841f-4609a0aecd00', 25, 2, 'User 2', 'TEXT', 'hôm nay bạn thấy thế nào', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 02:32:32', '2025-07-25 02:32:32');
INSERT INTO `chat_messages` VALUES (39, 'e5e78b2d-700e-4906-af4c-44d1d6c56144', 25, 2, 'AI Assistant', 'TEXT', '\"Ahaha, hôm nay tôi thấy rất tốt! Đang sẵn sàng giúp đỡ bạn với những vấn đề liên quan đến quản lý nhà hàng. Bạn cần giúp đỡ gì hôm nay?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 02:32:33', '2025-07-25 02:32:33');
INSERT INTO `chat_messages` VALUES (40, 'b8292658-a7e9-4b16-84f5-273748ee89c0', 25, 2, 'User 2', 'TEXT', 'okay, thử lại cái coi', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 02:38:07', '2025-07-25 02:38:07');
INSERT INTO `chat_messages` VALUES (41, 'ffbae849-222f-4e8e-9b60-9e7387ca900e', 25, 2, 'AI Assistant', 'TEXT', '\"I\'m not sure what you mean by \'cái coi\'. Could you please rephrase or provide more context about what you\'re trying to achieve? I\'m here to help with any questions or concerns you have about our restaurant, orders, menu, or reservations. Let me know how I can assist you!\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 02:38:09', '2025-07-25 02:38:09');
INSERT INTO `chat_messages` VALUES (42, '669bbcf6-c19e-4518-8dff-2103582c2ef4', 25, 2, 'User 2', 'TEXT', 'giờ thì sao ha', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 02:46:48', '2025-07-25 02:46:48');
INSERT INTO `chat_messages` VALUES (43, '1ae5a053-f394-4417-8336-871bdd6ce247', 25, 2, 'AI Assistant', 'TEXT', '\"Chào! Hiện tại không có vấn đề gì đặc biệt. Bạn có nhu cầu đặt món hay làm gì khác không?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 02:46:48', '2025-07-25 02:46:48');
INSERT INTO `chat_messages` VALUES (44, '662c95bc-6090-46d3-89c7-d6d3335b027d', 25, 2, 'User 2', 'TEXT', 'lại lần nữa nha', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 02:47:58', '2025-07-25 02:47:58');
INSERT INTO `chat_messages` VALUES (45, '20ed353d-4d7f-4595-baa3-441435433590', 25, 2, 'AI Assistant', 'TEXT', '\"Okay! Tôi không hiểu rõ bạn muốn gì, có thể bạn muốn hỏi về một món ăn, một đơn hàng hay thậm chí là một bảng dự trữ? Tôi sẵn sàng giúp đỡ!\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 02:47:58', '2025-07-25 02:47:58');
INSERT INTO `chat_messages` VALUES (46, 'e08e6a42-e270-47e0-afa6-ed26d4f33026', 25, 2, 'User 2', 'TEXT', 'hình như vẫn chưa được\'', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 02:48:37', '2025-07-25 02:48:37');
INSERT INTO `chat_messages` VALUES (47, '888a07c3-f5a7-462e-8679-c6b18d8ed3b9', 25, 2, 'AI Assistant', 'TEXT', '\"Vẫn chưa được gì? Cần giúp gì hay cần thông tin gì về thực đơn, đặt bàn hay đơn hàng? Tôi sẵn sàng hỗ trợ!\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 02:48:37', '2025-07-25 02:48:37');
INSERT INTO `chat_messages` VALUES (48, 'b84085b4-5298-4612-955a-cf7f647e45a4', 25, 2, 'User 2', 'TEXT', 'giờ thì được chưa nhỉ', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 03:06:01', '2025-07-25 03:06:01');
INSERT INTO `chat_messages` VALUES (49, 'dd76f505-6f0f-4e46-96d2-0899f24384d2', 25, 2, 'AI Assistant', 'TEXT', '\"Có thể. Hiện tại đang xử lý đơn hàng. Xin chờ một giây!\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 03:06:03', '2025-07-25 03:06:03');
INSERT INTO `chat_messages` VALUES (50, 'a882451b-eb81-4410-adf8-837e8f40cf55', 25, 2, 'User 2', 'TEXT', 'vậy khi nào bạn rảnh', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 03:08:06', '2025-07-25 03:08:06');
INSERT INTO `chat_messages` VALUES (51, '3f0dc591-9f2f-41b8-894f-ee8143865998', 25, 2, 'AI Assistant', 'TEXT', '\"Rảnh ngay bây giờ! Bạn cần giúp gì về quản lý nhà hàng? Đừng ngại hỏi!\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 03:08:08', '2025-07-25 03:08:08');
INSERT INTO `chat_messages` VALUES (52, '3e03484c-f237-493d-a6e1-bd9e7df2011a', 25, 2, 'User 2', 'TEXT', 'có đang mở websocket khôngta', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 03:08:49', '2025-07-25 03:08:49');
INSERT INTO `chat_messages` VALUES (53, '4c6737a4-84a6-4f9d-ab61-1cfc252bb866', 25, 2, 'AI Assistant', 'TEXT', '\"Có, chúng tôi đang sử dụng WebSocket để cung cấp các cập nhật thực-time cho khách hàng về các đơn hàng và tình trạng của chúng.\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 03:08:59', '2025-07-25 03:08:59');
INSERT INTO `chat_messages` VALUES (54, 'bc651bb6-09c2-4056-b61f-c5c6a287a6f7', 25, 2, 'User 2', 'TEXT', 'sửa đi sửa lại miết mà vẫn chưa xong', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 03:10:37', '2025-07-25 03:10:37');
INSERT INTO `chat_messages` VALUES (55, 'ca2974f5-b0c0-46e8-b482-7bc7ddb909dd', 25, 2, 'AI Assistant', 'TEXT', 'Vâng, hiểu bạn đang gặp vấn đề gì đó! Có thể tôi có thể giúp gì cho bạn? Bạn đang gặp vấn đề gì với đơn hàng, thực đơn, đặt bàn hay hoạt động khác của nhà hàng? Xin vui lòng giải thích chi tiết hơn để tôi có thể giúp bạn tốt nhất!', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 03:10:38', '2025-07-25 03:10:38');
INSERT INTO `chat_messages` VALUES (56, 'ea2ba11e-b6f7-41df-b544-cfbe7a4541da', 25, 2, 'User 2', 'TEXT', 'okay, sửa xong rồi nè, giờ thì sao ha', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 03:18:45', '2025-07-25 03:18:45');
INSERT INTO `chat_messages` VALUES (57, '3ca2d395-509c-4a70-a9cb-0e15d699e70e', 25, 2, 'AI Assistant', 'TEXT', '\"Ahaha, tốt rồi! Bạn đã sửa xong rồi. Bây giờ, chúng ta có thể kiểm tra lại các đơn hàng và đảm bảo rằng mọi thứ đang chạy đúng cách. Cần giúp gì nữa hay có vấn đề gì trong hệ thống?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 03:18:51', '2025-07-25 03:18:51');
INSERT INTO `chat_messages` VALUES (58, 'f8a317be-be6a-4d22-8808-213a920d7b4d', 24, 2, 'User 2', 'TEXT', 'giờ ngon lành rồi này', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 04:01:32', '2025-07-25 04:01:32');
INSERT INTO `chat_messages` VALUES (59, '55442832-36ad-4f79-9051-a92810bba526', 24, 2, 'AI Assistant', 'TEXT', '\"Xin chào! Bạn có nhu cầu đặt món hay đặt bàn hay có câu hỏi về thực đơn của chúng tôi không?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 04:01:33', '2025-07-25 04:01:33');
INSERT INTO `chat_messages` VALUES (60, 'cd35ac09-9832-47d8-be6a-381e9f242cae', 24, 2, 'User 2', 'TEXT', 'hình như có gì đó sai sai', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 04:01:45', '2025-07-25 04:01:45');
INSERT INTO `chat_messages` VALUES (61, '692616ab-160b-42bc-924d-2b63e76dfd60', 24, 2, 'AI Assistant', 'TEXT', '\"Xin lỗi, có thể bạn đang gặp vấn đề với một món ăn hay một đơn hàng? Tôi sẵn sàng giúp bạn giải quyết vấn đề đó. Hãy cho tôi biết chi tiết hơn về vấn đề bạn gặp phải!\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 04:01:46', '2025-07-25 04:01:46');
INSERT INTO `chat_messages` VALUES (62, 'c0984eee-99d6-480a-a4b0-2a8b60d02f8e', 25, 2, 'User 2', 'TEXT', 'giờ thì sao ha', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 04:04:49', '2025-07-25 04:04:49');
INSERT INTO `chat_messages` VALUES (63, 'f15c7655-5c19-44de-8803-ed5fed742bd2', 25, 2, 'AI Assistant', 'TEXT', '\"Chào! Hiện tại không có gì đặc biệt, mọi việc đều ổn. Bạn có nhu cầu gì về quán ăn hay giải đáp thắc mắc nào không?\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 04:04:50', '2025-07-25 04:04:50');
INSERT INTO `chat_messages` VALUES (64, '42d6bf67-f682-4350-8ea2-dd34bbe6815f', 25, 2, 'User 2', 'TEXT', 'sau khi apply code mới thì thế nào?', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 04:09:17', '2025-07-25 04:09:17');
INSERT INTO `chat_messages` VALUES (65, '4d34a47d-6c07-4f13-b13f-578e5703b427', 25, 2, 'AI Assistant', 'TEXT', '\"Vì code mới đã được áp dụng, các đơn hàng và thông tin sẽ được cập nhật tự động. Bạn có thể xem lại các mục \"Đơn hàng\" và \"Thống kê\" để kiểm tra các thông tin mới nhất. Nếu có vấn đề nào hay cần trợ giúp, vui lòng liên hệ với chúng tôi.\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 04:09:22', '2025-07-25 04:09:22');
INSERT INTO `chat_messages` VALUES (66, '1a35f085-04c2-4985-9be1-3d3595f49230', 25, 2, 'User 2', 'TEXT', 'nhà hàng bạn có mã giảm giá không?', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 04:09:36', '2025-07-25 04:09:36');
INSERT INTO `chat_messages` VALUES (67, '4c827dc2-5f32-4870-9283-5566819a8c21', 25, 2, 'AI Assistant', 'TEXT', '\"Rất tiếc, chúng tôi không có mã giảm giá tại thời điểm này. Tuy nhiên, chúng tôi thường xuyên có những chương trình giảm giá và ưu đãi đặc biệt cho khách hàng. Bạn có thể theo dõi page fanpage của chúng tôi để nhận được thông tin về các chương trình giảm giá mới nhất!\"', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 04:09:39', '2025-07-25 04:09:39');
INSERT INTO `chat_messages` VALUES (68, '431caea6-fc68-4748-b640-e26d7e19735c', 24, 2, 'User 2', 'TEXT', '😀', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 07:00:05', '2025-07-25 07:00:05');
INSERT INTO `chat_messages` VALUES (69, '5b3fb054-3f35-46ef-bbb4-88589a3109b2', 24, 2, 'AI Assistant', 'TEXT', '😊 Xin chào! 😊', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 07:00:07', '2025-07-25 07:00:07');
INSERT INTO `chat_messages` VALUES (70, 'ee02caf1-b754-4088-b9bb-97559fc78c64', 25, 2, 'User 2', 'TEXT', '😁', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 07:06:04', '2025-07-25 07:06:04');
INSERT INTO `chat_messages` VALUES (71, 'a7cff976-51e6-4d92-ae07-20f1395a1083', 25, 2, 'AI Assistant', 'TEXT', '😊 Xin chào! Bạn có nhu cầu gì với hệ thống quản lý nhà hàng của chúng tôi? 🤔', NULL, 'SENT', 1, 'AI', NULL, 0, 0, '2025-07-25 07:06:07', '2025-07-25 07:06:07');
INSERT INTO `chat_messages` VALUES (72, '80a73771-38bf-4a4c-9df1-ce2aaa791e9d', 26, 2, 'User 2', 'TEXT', 'chào bạn iu', NULL, 'SENT', 0, 'USER', NULL, 0, 0, '2025-07-25 07:22:52', '2025-07-25 07:22:52');
INSERT INTO `chat_messages` VALUES (73, '24e862ab-0514-4a72-89cb-7896c73ac9e0', 26, 2, 'AI', 'TEXT', '\"Xin chào! Tôi rất hân hạnh được giúp đỡ bạn. Bạn có nhu cầu gì về hệ thống quản lý nhà hàng của chúng tôi hôm nay?\"', NULL, 'SENT', 1, 'AI', NULL, 1, 0, '2025-07-25 07:22:53', '2025-07-25 07:22:53');

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
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_rooms
-- ----------------------------
INSERT INTO `chat_rooms` VALUES (6, 'chat-1753089786982', 'Chat with AI', 2, 'chat-1753089786982', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat', '2025-07-21 09:26:46', '2025-07-25 09:09:17', 1, NULL);
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
INSERT INTO `chat_rooms` VALUES (23, '9', 'Chat with AI2025-07-25T02:03:40.144309785', 2, '9', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat 2025-07-25T02:03:40.144482048', '2025-07-25 02:03:40', '2025-07-25 02:03:40', NULL, b'0');
INSERT INTO `chat_rooms` VALUES (24, '22', 'Chat with AI2025-07-25T02:03:46.672811685', 2, '22', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat 2025-07-25T02:03:46.672837030', '2025-07-25 02:03:47', '2025-07-25 02:03:47', NULL, b'0');
INSERT INTO `chat_rooms` VALUES (25, '24', 'Chat with AI2025-07-25T02:12:42.976894874', 2, '24', 'AI_SUPPORT', 'ACTIVE', 'AI assistant chat 2025-07-25T02:12:42.977005208', '2025-07-25 02:12:43', '2025-07-25 02:12:43', NULL, b'0');
INSERT INTO `chat_rooms` VALUES (26, 'abe113e3-4cd6-4fff-8c56-e03c13f29707', 'Chat Room for Session chat-1753428125661', 2, 'chat-1753428125661', 'AI_SUPPORT', 'ACTIVE', 'Chat room created for session chat-1753428125661', '2025-07-25 07:22:52', '2025-07-25 07:22:52', NULL, b'0');

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
