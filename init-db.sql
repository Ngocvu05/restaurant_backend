/*
 Navicat Premium Data Transfer

 Source Server         : restaurant
 Source Server Type    : MySQL
 Source Server Version : 80405 (8.4.5)
 Source Host           : localhost:3306
 Source Schema         : restaurant

 Target Server Type    : MySQL
 Target Server Version : 80405 (8.4.5)
 File Encoding         : 65001

 Date: 16/07/2025 16:48:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bookings
-- ----------------------------
DROP TABLE IF EXISTS `bookings`;
CREATE TABLE `bookings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `booking_time` datetime(6) NULL DEFAULT NULL,
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `number_of_guests` int NOT NULL,
  `number_of_people` int NOT NULL,
  `status` enum('CANCELLED','COMPLETED','CONFIRMED','PENDING','RESERVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `total_amount` decimal(38, 2) NULL DEFAULT NULL,
  `table_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK4uj6guqq3uqggk3nj3h302089`(`table_id` ASC) USING BTREE,
  INDEX `FKeyog2oic85xg7hsu2je2lx3s6`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK4uj6guqq3uqggk3nj3h302089` FOREIGN KEY (`table_id`) REFERENCES `tables` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKeyog2oic85xg7hsu2je2lx3s6` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bookings
-- ----------------------------
INSERT INTO `bookings` VALUES (1, '2025-06-25 13:27:13.000000', 'Birthday party', 4, 4, 'CONFIRMED', NULL, 1, 2);
INSERT INTO `bookings` VALUES (2, '2025-06-30 08:36:27.807000', 'hahah', 2, 0, 'COMPLETED', 737444.00, 1, 2);
INSERT INTO `bookings` VALUES (3, '2025-06-30 08:43:40.464000', 'kakak', 2, 0, 'CANCELLED', 309996.00, 1, 2);
INSERT INTO `bookings` VALUES (4, '2025-06-30 08:52:04.727000', 'test qr code', 2, 0, 'RESERVED', 743000.00, 1, 2);
INSERT INTO `bookings` VALUES (5, '2025-07-01 09:15:01.543000', 'aaa', 2, 0, 'PENDING', 60000.00, 1, 2);

/*-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chat_room_id` bigint NOT NULL,
  `message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sender_id` bigint NOT NULL,
  `sender_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sender_type` enum('USER','AI','SYSTEM') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'USER',
  `parent_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `type` enum('TEXT','IMAGE','FILE','SYSTEM') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'TEXT',
  `status` enum('SENT','DELIVERED','READ','FAILED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SENT',
  `is_ai_generated` tinyint(1) NULL DEFAULT 0,
  `is_read` tinyint(1) NULL DEFAULT 0,
  `deleted` tinyint(1) NULL DEFAULT 0,
  `metadata` json NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `message_id`(`message_id` ASC) USING BTREE,
  INDEX `parent_message_id`(`parent_message_id` ASC) USING BTREE,
  INDEX `idx_chat_room_id`(`chat_room_id` ASC) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_sender_type`(`sender_type` ASC) USING BTREE,
  INDEX `idx_deleted`(`deleted` ASC) USING BTREE,
  INDEX `idx_messages_room_created`(`chat_room_id` ASC, `created_at` DESC) USING BTREE,
  INDEX `idx_messages_unread`(`chat_room_id` ASC, `is_read` ASC, `created_at` DESC) USING BTREE,
  CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chat_messages_ibfk_2` FOREIGN KEY (`parent_message_id`) REFERENCES `chat_messages` (`message_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------

-- ----------------------------
-- Table structure for chat_rooms
-- ----------------------------
DROP TABLE IF EXISTS `chat_rooms`;
CREATE TABLE `chat_rooms`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `session_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `active` tinyint(1) NULL DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_session`(`user_id` ASC, `session_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_active`(`active` ASC) USING BTREE,
  INDEX `idx_rooms_user_updated`(`user_id` ASC, `updated_at` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_rooms
-- ----------------------------
INSERT INTO `chat_rooms` VALUES (1, 1, 'default_session', 'Default Chat Room', 1, '2025-07-16 08:30:46', '2025-07-16 08:30:46');
*/
-- ----------------------------
-- Table structure for dishes
-- ----------------------------
DROP TABLE IF EXISTS `dishes`;
CREATE TABLE `dishes`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `available` bit(1) NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `order_count` int NOT NULL,
  `price` decimal(38, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 107 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dishes
-- ----------------------------
INSERT INTO `dishes` VALUES (1, 'Khai vị', '2025-06-25 13:26:46.000000', 'Món khai vị truyền thống với tôm và thịt heo tươi', b'1', 'Gỏi cuốn tôm thịt', 19, 30000.00);
INSERT INTO `dishes` VALUES (2, 'Khai vị', '2025-06-25 13:26:46.000000', 'Chả giò chiên giòn rụm, ăn kèm nước mắm chua ngọt', b'1', 'Chả giò rế', 26, 25000.00);
INSERT INTO `dishes` VALUES (3, 'Món chính', '2025-06-25 13:26:46.000000', 'Cơm tấm với sườn nướng đậm đà và trứng ốp la', b'1', 'Cơm sườn nướng', 50, 55000.00);
INSERT INTO `dishes` VALUES (4, 'Món chính', '2025-06-25 13:26:46.000000', 'Phở nước dùng đậm vị với tái, nạm, gân, bò viên', b'1', 'Phở bò đặc biệt', 54, 60000.00);
INSERT INTO `dishes` VALUES (5, 'Món chính', '2025-06-25 13:26:46.000000', 'Món đặc sản miền Trung cay nồng và thơm ngon', b'1', 'Bún bò Huế', 46, 58000.00);
INSERT INTO `dishes` VALUES (6, 'Tráng miệng', '2025-06-25 13:26:46.000000', 'Chè mát lạnh với khúc bạch hạnh nhân và vải', b'1', 'Chè khúc bạch', 20, 28000.00);
INSERT INTO `dishes` VALUES (7, 'Tráng miệng', '2025-06-25 13:26:46.000000', 'Bánh mềm mịn tan chảy với lớp caramel ngọt ngào', b'1', 'Bánh flan caramel', 25, 25000.00);
INSERT INTO `dishes` VALUES (8, 'Đồ uống', '2025-06-25 13:26:46.000000', 'Trà trái cây mát lạnh, thơm vị cam và sả', b'1', 'Trà đào cam sả', 37, 30000.00);
INSERT INTO `dishes` VALUES (9, 'Đồ uống', '2025-06-25 13:26:46.000000', 'Sinh tố bơ nguyên chất, béo ngậy', b'1', 'Sinh tố bơ', 25, 32000.00);
INSERT INTO `dishes` VALUES (10, 'Đồ uống', '2025-06-25 13:26:46.000000', 'Cà phê truyền thống, vị đậm đà, thơm nồng', b'1', 'Cà phê sữa đá', 41, 25000.00);
INSERT INTO `dishes` VALUES (11, 'Tráng miệng', '2025-06-27 11:40:57.625658', 'ngon lắm', b'1', 'món mới nha', 11, 444.00);
INSERT INTO `dishes` VALUES (12, 'Tráng miệng', '2025-06-27 13:20:13.831195', 'hot hot thot', b'1', 'món mới nha 22', 1, 555.00);
INSERT INTO `dishes` VALUES (13, 'asdas', '2025-06-27 13:21:48.711639', 'asdasd', b'1', 'món mới nha 223', 0, 222.00);
INSERT INTO `dishes` VALUES (14, 'asdas', '2025-06-27 13:46:05.506620', 'ada', b'1', 'món mới nha 2234', 2, 21.00);
INSERT INTO `dishes` VALUES (15, '23423', '2025-06-27 14:00:39.799683', 'sfsdf', b'1', 'món mới nha444', 0, 444.00);
INSERT INTO `dishes` VALUES (16, 'asd', '2025-06-27 14:04:01.716983', 'asdas', b'1', 'món mới nha444', 0, 221.00);
INSERT INTO `dishes` VALUES (17, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 11', b'1', 'Món ăn số 11', 34, 43000.00);
INSERT INTO `dishes` VALUES (18, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 12', b'1', 'Món ăn số 12', 10, 36000.00);
INSERT INTO `dishes` VALUES (19, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 13', b'1', 'Món ăn số 13', 26, 45000.00);
INSERT INTO `dishes` VALUES (20, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 14', b'1', 'Món ăn số 14', 41, 33000.00);
INSERT INTO `dishes` VALUES (21, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 15', b'1', 'Món ăn số 15', 4, 37000.00);
INSERT INTO `dishes` VALUES (22, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 16', b'1', 'Món ăn số 16', 36, 46000.00);
INSERT INTO `dishes` VALUES (23, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 17', b'1', 'Món ăn số 17', 10, 45000.00);
INSERT INTO `dishes` VALUES (24, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 18', b'1', 'Món ăn số 18', 9, 48000.00);
INSERT INTO `dishes` VALUES (25, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 19', b'1', 'Món ăn số 19', 18, 49000.00);
INSERT INTO `dishes` VALUES (26, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 20', b'1', 'Món ăn số 20', 2, 31000.00);
INSERT INTO `dishes` VALUES (27, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 21', b'1', 'Món ăn số 21', 39, 41000.00);
INSERT INTO `dishes` VALUES (28, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 22', b'1', 'Món ăn số 22', 34, 40000.00);
INSERT INTO `dishes` VALUES (29, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 23', b'1', 'Món ăn số 23', 33, 44000.00);
INSERT INTO `dishes` VALUES (30, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 24', b'1', 'Món ăn số 24', 27, 36000.00);
INSERT INTO `dishes` VALUES (31, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 25', b'1', 'Món ăn số 25', 35, 35000.00);
INSERT INTO `dishes` VALUES (32, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 26', b'1', 'Món ăn số 26', 44, 39000.00);
INSERT INTO `dishes` VALUES (33, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 27', b'1', 'Món ăn số 27', 20, 37000.00);
INSERT INTO `dishes` VALUES (34, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 28', b'1', 'Món ăn số 28', 15, 46000.00);
INSERT INTO `dishes` VALUES (35, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 29', b'1', 'Món ăn số 29', 48, 37000.00);
INSERT INTO `dishes` VALUES (36, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 30', b'1', 'Món ăn số 30', 4, 39000.00);
INSERT INTO `dishes` VALUES (37, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 31', b'1', 'Món ăn số 31', 39, 42000.00);
INSERT INTO `dishes` VALUES (38, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 32', b'1', 'Món ăn số 32', 27, 47000.00);
INSERT INTO `dishes` VALUES (39, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 33', b'1', 'Món ăn số 33', 8, 37000.00);
INSERT INTO `dishes` VALUES (40, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 34', b'1', 'Món ăn số 34', 11, 45000.00);
INSERT INTO `dishes` VALUES (41, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 35', b'1', 'Món ăn số 35', 48, 34000.00);
INSERT INTO `dishes` VALUES (42, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 36', b'1', 'Món ăn số 36', 19, 48000.00);
INSERT INTO `dishes` VALUES (43, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 37', b'1', 'Món ăn số 37', 31, 31000.00);
INSERT INTO `dishes` VALUES (44, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 38', b'1', 'Món ăn số 38', 18, 44000.00);
INSERT INTO `dishes` VALUES (45, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 39', b'1', 'Món ăn số 39', 37, 37000.00);
INSERT INTO `dishes` VALUES (46, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 40', b'1', 'Món ăn số 40', 0, 38000.00);
INSERT INTO `dishes` VALUES (47, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 41', b'1', 'Món ăn số 41', 23, 40000.00);
INSERT INTO `dishes` VALUES (48, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 42', b'1', 'Món ăn số 42', 34, 33000.00);
INSERT INTO `dishes` VALUES (49, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 43', b'1', 'Món ăn số 43', 1, 32000.00);
INSERT INTO `dishes` VALUES (50, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 44', b'1', 'Món ăn số 44', 38, 46000.00);
INSERT INTO `dishes` VALUES (51, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 45', b'1', 'Món ăn số 45', 47, 38000.00);
INSERT INTO `dishes` VALUES (52, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 46', b'1', 'Món ăn số 46', 28, 47000.00);
INSERT INTO `dishes` VALUES (53, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 47', b'1', 'Món ăn số 47', 27, 42000.00);
INSERT INTO `dishes` VALUES (54, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 48', b'1', 'Món ăn số 48', 4, 50000.00);
INSERT INTO `dishes` VALUES (55, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 49', b'1', 'Món ăn số 49', 46, 47000.00);
INSERT INTO `dishes` VALUES (56, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 50', b'1', 'Món ăn số 50', 5, 30000.00);
INSERT INTO `dishes` VALUES (57, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 51', b'1', 'Món ăn số 51', 41, 48000.00);
INSERT INTO `dishes` VALUES (58, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 52', b'1', 'Món ăn số 52', 6, 47000.00);
INSERT INTO `dishes` VALUES (59, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 53', b'1', 'Món ăn số 53', 25, 39000.00);
INSERT INTO `dishes` VALUES (60, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 54', b'1', 'Món ăn số 54', 35, 36000.00);
INSERT INTO `dishes` VALUES (61, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 55', b'1', 'Món ăn số 55', 19, 45000.00);
INSERT INTO `dishes` VALUES (62, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 56', b'1', 'Món ăn số 56', 27, 45000.00);
INSERT INTO `dishes` VALUES (63, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 57', b'1', 'Món ăn số 57', 35, 32000.00);
INSERT INTO `dishes` VALUES (64, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 58', b'1', 'Món ăn số 58', 45, 33000.00);
INSERT INTO `dishes` VALUES (65, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 59', b'1', 'Món ăn số 59', 42, 39000.00);
INSERT INTO `dishes` VALUES (66, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 60', b'1', 'Món ăn số 60', 9, 36000.00);
INSERT INTO `dishes` VALUES (67, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 61', b'1', 'Món ăn số 61', 36, 48000.00);
INSERT INTO `dishes` VALUES (68, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 62', b'1', 'Món ăn số 62', 20, 30000.00);
INSERT INTO `dishes` VALUES (69, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 63', b'1', 'Món ăn số 63', 16, 37000.00);
INSERT INTO `dishes` VALUES (70, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 64', b'1', 'Món ăn số 64', 37, 35000.00);
INSERT INTO `dishes` VALUES (71, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 65', b'1', 'Món ăn số 65', 10, 47000.00);
INSERT INTO `dishes` VALUES (72, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 66', b'1', 'Món ăn số 66', 47, 34000.00);
INSERT INTO `dishes` VALUES (73, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 67', b'1', 'Món ăn số 67', 42, 47000.00);
INSERT INTO `dishes` VALUES (74, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 68', b'1', 'Món ăn số 68', 43, 45000.00);
INSERT INTO `dishes` VALUES (75, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 69', b'1', 'Món ăn số 69', 34, 43000.00);
INSERT INTO `dishes` VALUES (76, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 70', b'1', 'Món ăn số 70', 43, 35000.00);
INSERT INTO `dishes` VALUES (77, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 71', b'1', 'Món ăn số 71', 8, 49000.00);
INSERT INTO `dishes` VALUES (78, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 72', b'1', 'Món ăn số 72', 25, 35000.00);
INSERT INTO `dishes` VALUES (79, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 73', b'1', 'Món ăn số 73', 46, 43000.00);
INSERT INTO `dishes` VALUES (80, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 74', b'1', 'Món ăn số 74', 34, 36000.00);
INSERT INTO `dishes` VALUES (81, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 75', b'1', 'Món ăn số 75', 26, 35000.00);
INSERT INTO `dishes` VALUES (82, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 76', b'1', 'Món ăn số 76', 43, 47000.00);
INSERT INTO `dishes` VALUES (83, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 77', b'1', 'Món ăn số 77', 45, 36000.00);
INSERT INTO `dishes` VALUES (84, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 78', b'1', 'Món ăn số 78', 1, 42000.00);
INSERT INTO `dishes` VALUES (85, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 79', b'1', 'Món ăn số 79', 34, 30000.00);
INSERT INTO `dishes` VALUES (86, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 80', b'1', 'Món ăn số 80', 34, 42000.00);
INSERT INTO `dishes` VALUES (87, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 81', b'1', 'Món ăn số 81', 24, 46000.00);
INSERT INTO `dishes` VALUES (88, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 82', b'1', 'Món ăn số 82', 5, 41000.00);
INSERT INTO `dishes` VALUES (89, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 83', b'1', 'Món ăn số 83', 31, 33000.00);
INSERT INTO `dishes` VALUES (90, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 84', b'1', 'Món ăn số 84', 31, 47000.00);
INSERT INTO `dishes` VALUES (91, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 85', b'1', 'Món ăn số 85', 13, 46000.00);
INSERT INTO `dishes` VALUES (92, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 86', b'1', 'Món ăn số 86', 37, 49000.00);
INSERT INTO `dishes` VALUES (93, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 87', b'1', 'Món ăn số 87', 9, 49000.00);
INSERT INTO `dishes` VALUES (94, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 88', b'1', 'Món ăn số 88', 44, 33000.00);
INSERT INTO `dishes` VALUES (95, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 89', b'1', 'Món ăn số 89', 1, 36000.00);
INSERT INTO `dishes` VALUES (96, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 90', b'1', 'Món ăn số 90', 44, 47000.00);
INSERT INTO `dishes` VALUES (97, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 91', b'1', 'Món ăn số 91', 12, 34000.00);
INSERT INTO `dishes` VALUES (98, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 92', b'1', 'Món ăn số 92', 15, 32000.00);
INSERT INTO `dishes` VALUES (99, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 93', b'1', 'Món ăn số 93', 41, 39000.00);
INSERT INTO `dishes` VALUES (100, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 94', b'1', 'Món ăn số 94', 13, 40000.00);
INSERT INTO `dishes` VALUES (101, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 95', b'1', 'Món ăn số 95', 17, 39000.00);
INSERT INTO `dishes` VALUES (102, 'Khai vị', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 96', b'1', 'Món ăn số 96', 10, 32000.00);
INSERT INTO `dishes` VALUES (103, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 97', b'1', 'Món ăn số 97', 14, 40000.00);
INSERT INTO `dishes` VALUES (104, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 98', b'1', 'Món ăn số 98', 3, 41000.00);
INSERT INTO `dishes` VALUES (105, 'Món chính', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 99', b'1', 'Món ăn số 99', 26, 40000.00);
INSERT INTO `dishes` VALUES (106, 'Đồ uống', '2025-07-09 07:06:52.000000', 'Mô tả món ăn số 100', b'1', 'Món ăn số 100', 15, 32000.00);

-- ----------------------------
-- Table structure for images
-- ----------------------------
DROP TABLE IF EXISTS `images`;
CREATE TABLE `images`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `is_avatar` bit(1) NOT NULL,
  `uploaded_at` datetime(6) NULL DEFAULT NULL,
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dish_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKedtd63h4lhqsm05mqcmrclpy9`(`dish_id` ASC) USING BTREE,
  INDEX `FK13ljqfrfwbyvnsdhihwta8cpr`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK13ljqfrfwbyvnsdhihwta8cpr` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKedtd63h4lhqsm05mqcmrclpy9` FOREIGN KEY (`dish_id`) REFERENCES `dishes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of images
-- ----------------------------
INSERT INTO `images` VALUES (40, b'1', '2025-07-02 15:28:28.941876', 'https://res.cloudinary.com/dismwddmg/image/upload/v1751444908/restaurant/upload/images/f1ba1443-ab12-4127-8537-6f1a12c57ec6_salad.jpg.jpg', NULL, 2);
INSERT INTO `images` VALUES (41, b'1', '2025-07-02 15:33:53.148751', 'https://res.cloudinary.com/dismwddmg/image/upload/v1751445232/restaurant/upload/images/3490584b-a747-4c47-982e-9d7648958166_pizza.jpg.jpg', NULL, 1);
INSERT INTO `images` VALUES (42, b'0', '2025-07-02 15:33:55.525668', 'https://res.cloudinary.com/dismwddmg/image/upload/v1751445234/restaurant/upload/images/3cfe4052-5349-4a8b-9851-9dcae9465008_pizza.jpg.jpg', NULL, 2);
INSERT INTO `images` VALUES (43, b'1', '2025-07-02 15:33:55.547503', 'https://res.cloudinary.com/dismwddmg/image/upload/v1751445234/restaurant/upload/images/3cfe4052-5349-4a8b-9851-9dcae9465008_pizza.jpg.jpg', NULL, 6);

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_read` tinyint(1) NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `to_user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `FK9u6rfqx9lueovqy0a5mcccsfg`(`to_user_id` ASC) USING BTREE,
  CONSTRAINT `FK9u6rfqx9lueovqy0a5mcccsfg` FOREIGN KEY (`to_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notifications
-- ----------------------------
INSERT INTO `notifications` VALUES (1, 'Hello Admin', 0, NULL, 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (2, 'Hello Admin', 1, NULL, 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (3, 'Hello Admin', 0, NULL, 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (4, 'Hello Admin', 1, '2025-07-01 09:46:02', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (5, 'Hello Admin', 0, '2025-07-01 10:04:20', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (6, 'Hello Admin', 0, '2025-07-01 10:36:35', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (7, 'Hello Admin', 0, '2025-07-01 10:43:29', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (8, 'Hello Admin', 0, '2025-07-01 10:54:11', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (9, 'Hello Admin', 0, '2025-07-01 10:56:10', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (10, 'Hello Admin', 0, '2025-07-01 10:57:11', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (11, 'Hello Admin', 0, '2025-07-01 11:04:55', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (12, 'Hello Admin', 0, '2025-07-01 11:07:28', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (13, 'Hello Admin', 0, '2025-07-01 11:20:11', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (14, 'Hello Admin', 0, '2025-07-01 11:22:49', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (15, 'Hello Admin', 0, '2025-07-01 11:26:52', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (16, 'Hello Admin', 0, '2025-07-01 11:37:25', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (17, 'Hello Admin', 0, '2025-07-01 11:42:07', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (18, 'Hello Admin', 0, '2025-07-01 11:42:25', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (19, 'Hello Admin', 0, '2025-07-01 13:03:41', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (20, 'Hello Admin', 0, '2025-07-01 13:09:35', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (21, 'Hello Admin', 0, '2025-07-01 13:17:58', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (22, 'Hello Admin', 0, '2025-07-01 13:18:59', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (23, 'Hello Admin', 1, '2025-07-01 14:17:46', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (24, 'Hello Admin', 1, '2025-07-01 14:20:35', 1, 'new notification', NULL);
INSERT INTO `notifications` VALUES (25, 'Hello Admin', 1, '2025-07-01 14:25:10', 1, 'New Notification', NULL);
INSERT INTO `notifications` VALUES (26, 'Hello Admin', 1, '2025-07-01 14:25:19', 1, 'New Notification', NULL);
INSERT INTO `notifications` VALUES (28, 'Người dùng mới đã tạo tài khoản: testpush1', 1, '2025-07-01 16:08:42', 1, 'New Account', 1);
INSERT INTO `notifications` VALUES (29, 'Người dùng mới đã tạo tài khoản: testpush1', 0, '2025-07-01 16:08:42', 3, 'New Account', 1);
INSERT INTO `notifications` VALUES (30, 'Người dùng customer đã đặt bàn.', 1, '2025-07-01 16:15:25', 1, 'Đặt bàn mới', 1);
INSERT INTO `notifications` VALUES (31, 'Người dùng customer đã đặt bàn.', 0, '2025-07-01 16:15:25', 3, 'Đặt bàn mới', 1);
INSERT INTO `notifications` VALUES (32, 'Admin đã thêm một bàn mới.', 1, '2025-07-02 15:28:29', 1, 'Thêm ảnh mới', 1);
INSERT INTO `notifications` VALUES (33, 'Admin đã thêm một bàn mới.', 0, '2025-07-02 15:28:29', 1, 'Thêm ảnh mới', 3);
INSERT INTO `notifications` VALUES (34, 'Người dùng mới đã tạo tài khoản: newuser', 1, '2025-07-02 15:33:56', NULL, 'New Account', 1);
INSERT INTO `notifications` VALUES (35, 'Người dùng mới đã tạo tài khoản: newuser', 0, '2025-07-02 15:33:56', NULL, 'New Account', 3);

-- ----------------------------
-- Table structure for order_history
-- ----------------------------
DROP TABLE IF EXISTS `order_history`;
CREATE TABLE `order_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `quantity` int NOT NULL,
  `served` bit(1) NULL DEFAULT NULL,
  `total_amount` decimal(38, 2) NULL DEFAULT NULL,
  `booking_id` bigint NULL DEFAULT NULL,
  `dish_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK1i4v6ae89s6n93oj2ygrm8dcq`(`booking_id` ASC) USING BTREE,
  INDEX `FK6m0tpp3jmic2v0yk4evx6fux1`(`dish_id` ASC) USING BTREE,
  INDEX `FK4voclnbr2965u9qn6c8pknive`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK1i4v6ae89s6n93oj2ygrm8dcq` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK4voclnbr2965u9qn6c8pknive` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK6m0tpp3jmic2v0yk4evx6fux1` FOREIGN KEY (`dish_id`) REFERENCES `dishes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_history
-- ----------------------------

-- ----------------------------
-- Table structure for payments
-- ----------------------------
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(38, 2) NULL DEFAULT NULL,
  `payment_method` enum('BANK_TRANSFER','CARD','CASH','MOMO','VNPAY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `payment_time` datetime(6) NULL DEFAULT NULL,
  `status` enum('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED', 'REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `booking_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKc52o2b1jkxttngufqp3t7jr3h`(`booking_id` ASC) USING BTREE,
  CONSTRAINT `FKc52o2b1jkxttngufqp3t7jr3h` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of payments
-- ----------------------------
INSERT INTO `payments` VALUES (1, 240.00, 'CASH', '2025-06-25 13:27:13.000000', 'PAID', 1);

-- ----------------------------
-- Table structure for preorders
-- ----------------------------
DROP TABLE IF EXISTS `preorders`;
CREATE TABLE `preorders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `quantity` int NOT NULL,
  `booking_id` bigint NOT NULL,
  `dish_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK31b2jdstgs4frww2vflamlwns`(`booking_id` ASC) USING BTREE,
  INDEX `FK24579qbh6eki7tkuupth4w7n8`(`dish_id` ASC) USING BTREE,
  CONSTRAINT `FK24579qbh6eki7tkuupth4w7n8` FOREIGN KEY (`dish_id`) REFERENCES `dishes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK31b2jdstgs4frww2vflamlwns` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of preorders
-- ----------------------------
INSERT INTO `preorders` VALUES (1, 'Extra cheese', 2, 1, 1);
INSERT INTO `preorders` VALUES (2, NULL, 1, 2, 1);
INSERT INTO `preorders` VALUES (3, NULL, 2, 2, 3);
INSERT INTO `preorders` VALUES (4, NULL, 3, 2, 5);
INSERT INTO `preorders` VALUES (5, NULL, 3, 2, 4);
INSERT INTO `preorders` VALUES (6, NULL, 5, 2, 9);
INSERT INTO `preorders` VALUES (7, NULL, 1, 2, 8);
INSERT INTO `preorders` VALUES (8, NULL, 1, 2, 6);
INSERT INTO `preorders` VALUES (9, NULL, 1, 2, 11);
INSERT INTO `preorders` VALUES (10, NULL, 1, 2, 7);
INSERT INTO `preorders` VALUES (11, NULL, 3, 3, 1);
INSERT INTO `preorders` VALUES (12, NULL, 4, 3, 2);
INSERT INTO `preorders` VALUES (13, NULL, 2, 3, 5);
INSERT INTO `preorders` VALUES (14, NULL, 9, 3, 11);
INSERT INTO `preorders` VALUES (15, NULL, 1, 4, 1);
INSERT INTO `preorders` VALUES (16, NULL, 2, 4, 2);
INSERT INTO `preorders` VALUES (17, NULL, 3, 4, 3);
INSERT INTO `preorders` VALUES (18, NULL, 1, 4, 4);
INSERT INTO `preorders` VALUES (19, NULL, 3, 4, 5);
INSERT INTO `preorders` VALUES (20, NULL, 1, 4, 6);
INSERT INTO `preorders` VALUES (21, NULL, 2, 4, 7);
INSERT INTO `preorders` VALUES (22, NULL, 3, 4, 8);
INSERT INTO `preorders` VALUES (23, NULL, 3, 4, 9);
INSERT INTO `preorders` VALUES (24, NULL, 2, 5, 1);

-- ----------------------------
-- Table structure for refresh_token
-- ----------------------------
DROP TABLE IF EXISTS `refresh_token`;
CREATE TABLE `refresh_token`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expiry_date` datetime(6) NULL DEFAULT NULL,
  `token` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKr4k4edos30bx9neoq81mdvwph`(`token` ASC) USING BTREE,
  UNIQUE INDEX `UKf95ixxe7pa48ryn1awmh2evt7`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FKjtx87i0jvq2svedphegvdwcuy` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of refresh_token
-- ----------------------------

-- ----------------------------
-- Table structure for tables
-- ----------------------------
DROP TABLE IF EXISTS `tables`;
CREATE TABLE `tables`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `capacity` int NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` enum('AVAILABLE','BOOKED','OCCUPIED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `table_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tables
-- ----------------------------
INSERT INTO `tables` VALUES (1, 4, 'Bàn tròn nhỏ, phù hợp cho gia đình nhỏ', 'AVAILABLE', 'T1');
INSERT INTO `tables` VALUES (2, 6, 'bàn dài, chứa được 6 người, phù hợp với khách đi theo nhóm', 'BOOKED', 'T2');

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` enum('ADMIN','CUSTOMER','STAFF') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK182xa1gitcxqhaq6nn3n2kmo3`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_roles
-- ----------------------------
INSERT INTO `user_roles` VALUES (1, 'ADMIN');
INSERT INTO `user_roles` VALUES (3, 'CUSTOMER');
INSERT INTO `user_roles` VALUES (2, 'STAFF');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `full_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKr43af9ap4edm43mmtq01oddj6`(`username` ASC) USING BTREE,
  INDEX `FKh555fyoyldpyaltlb7jva35j2`(`role_id` ASC) USING BTREE,
  CONSTRAINT `FKh555fyoyldpyaltlb7jva35j2` FOREIGN KEY (`role_id`) REFERENCES `user_roles` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '123 Admin St', '2025-06-25 13:26:46.000000', 'dinhngocvuit@gmail.com', 'Admin User', '$2a$10$/MIrJqi2rExvSW6.tc7/hO587VjHxUOjO7CUbYzVFQsRWKExsfA.e', '0123456789', 'admin', 1);
INSERT INTO `users` VALUES (2, '456 Customer Ave', '2025-06-25 13:26:46.000000', 'ngocvu.ngoc06@gmail.com', 'Customer A', '$2a$10$/MIrJqi2rExvSW6.tc7/hO587VjHxUOjO7CUbYzVFQsRWKExsfA.e', '0987654321', 'customer', 3);
INSERT INTO `users` VALUES (3, 'Thu Duc', '2025-06-26 09:00:10.484677', 'dinhngocvuit@gmail.com', 'Ngoc Vu', '$2a$10$/MIrJqi2rExvSW6.tc7/hO587VjHxUOjO7CUbYzVFQsRWKExsfA.e', '0962805614', 'admin333', 1);
INSERT INTO `users` VALUES (4, 'Thu Duc', '2025-07-01 15:18:11.825185', 'ngocvu.ngoc06@gmail.com', 'Push', '$2a$10$/MIrJqi2rExvSW6.tc7/hO587VjHxUOjO7CUbYzVFQsRWKExsfA.e', '0987654332', 'testpush', 3);
INSERT INTO `users` VALUES (5, 'Thu Duc', '2025-07-01 16:08:41.616760', 'ngocvu.ngoc06@gmail.com', 'Ngoc Vu', '$2a$10$/MIrJqi2rExvSW6.tc7/hO587VjHxUOjO7CUbYzVFQsRWKExsfA.e', '231231', 'testpush1', 3);
INSERT INTO `users` VALUES (6, 'Thu Duc', '2025-07-02 15:33:53.182822', 'ngocvu.ngoc06@gmail.com', 'Ngoc Vu', '$2a$10$/MIrJqi2rExvSW6.tc7/hO587VjHxUOjO7CUbYzVFQsRWKExsfA.e', '0987654321', 'newuser', 3);

-- ----------------------------------
ALTER TABLE payments
    MODIFY COLUMN payment_method
        ENUM('CASH','CARD','BANK_TRANSFER','MOMO','VNPAY')
        NOT NULL;

-- ----------------------------
-- Procedure structure for generate_dishes
-- ----------------------------
DROP PROCEDURE IF EXISTS `generate_dishes`;
delimiter ;;
CREATE PROCEDURE `generate_dishes`()

;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
