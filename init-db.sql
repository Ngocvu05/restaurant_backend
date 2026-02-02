/*
 Navicat Premium Data Transfer

 Modified: Added Audit & Soft Delete Support
 Date: 2025-01-01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- ORIGINAL SCHEMA + DATA (từ init-db.sql)
-- =====================================================

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
CREATE TABLE `refresh_token` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
                                 `token` VARCHAR(767) NOT NULL COMMENT 'Refresh token value (unique)',
                                 `user_id` BIGINT NOT NULL COMMENT 'User ID owning this token',
                                 `expiry_date` DATETIME(6) NOT NULL COMMENT 'Token expiration date',

    -- Audit fields
                                 `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Token creation timestamp',
                                 `created_by` VARCHAR(100) DEFAULT 'system' COMMENT 'Username who created this token',
                                 `updated_at` DATETIME(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp',
                                 `updated_by` VARCHAR(100) DEFAULT NULL COMMENT 'Username who last updated this token',

    -- Soft delete fields
                                 `deleted_at` DATETIME(6) NULL DEFAULT NULL COMMENT 'Soft delete timestamp',
                                 `deleted_by` VARCHAR(100) DEFAULT NULL COMMENT 'Username who deleted this token',

    -- Token revocation fields
                                 `revoked` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Is token revoked',
                                 `revoked_at` DATETIME(6) NULL DEFAULT NULL COMMENT 'When token was revoked',
                                 `revoked_by` VARCHAR(100) DEFAULT NULL COMMENT 'Who revoked the token',
                                 `revoked_reason` VARCHAR(255) DEFAULT NULL COMMENT 'Reason for revocation',

    -- Security tracking fields
                                 `ip_address` VARCHAR(45) DEFAULT NULL COMMENT 'IP address when token was created',
                                 `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'User agent when token was created',
                                 `device_id` VARCHAR(255) DEFAULT NULL COMMENT 'Device identifier',
                                 `device_name` VARCHAR(100) DEFAULT NULL COMMENT 'Device name (e.g., Chrome on Windows)',

    -- Token rotation fields
                                 `token_family` VARCHAR(100) DEFAULT NULL COMMENT 'Token family ID for rotation tracking',
                                 `parent_token_id` BIGINT DEFAULT NULL COMMENT 'Previous token in refresh chain',

    -- Usage tracking fields
                                 `last_used_at` DATETIME(6) NULL DEFAULT NULL COMMENT 'Last time token was used',
                                 `use_count` INT NOT NULL DEFAULT 0 COMMENT 'Number of times token was used',

    -- Constraints
                                 PRIMARY KEY (`id`) USING BTREE,
                                 UNIQUE KEY `uk_refresh_token_token` (`token`) USING BTREE,

    -- Indexes for performance
                                 INDEX `idx_refresh_token_user_id` (`user_id`) USING BTREE,
                                 INDEX `idx_refresh_token_expiry_date` (`expiry_date`) USING BTREE,
                                 INDEX `idx_refresh_token_revoked` (`revoked`) USING BTREE,
                                 INDEX `idx_refresh_token_deleted_at` (`deleted_at`) USING BTREE,
                                 INDEX `idx_refresh_token_created_at` (`created_at`) USING BTREE,
                                 INDEX `idx_refresh_token_token_family` (`token_family`) USING BTREE,
                                 INDEX `idx_refresh_token_ip_address` (`ip_address`) USING BTREE,
                                 INDEX `idx_refresh_token_last_used_at` (`last_used_at`) USING BTREE,

    -- Foreign keys
                                 CONSTRAINT `fk_refresh_token_user`
                                     FOREIGN KEY (`user_id`)
                                         REFERENCES `users` (`id`)
                                         ON DELETE CASCADE
                                         ON UPDATE RESTRICT,

                                 CONSTRAINT `fk_refresh_token_parent`
                                     FOREIGN KEY (`parent_token_id`)
                                         REFERENCES `refresh_token` (`id`)
                                         ON DELETE SET NULL
                                         ON UPDATE RESTRICT

) ENGINE=InnoDB
  AUTO_INCREMENT=1
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
    COMMENT='Refresh tokens for JWT authentication';

-- ----------------------------
-- Sample data for refresh_token (Optional)
-- ----------------------------
-- INSERT INTO `refresh_token` VALUES
-- (1, 'sample-token-1', 1, '2026-02-27 00:00:00.000000',
--  '2026-01-27 00:00:00.000000', 'system', NULL, NULL,
--  NULL, NULL, FALSE, NULL, NULL, NULL,
--  '192.168.1.100', 'Mozilla/5.0', 'device-1', 'Chrome on Windows',
--  'family-1', NULL, NULL, 0);

-- ----------------------------
-- Create view for active tokens
-- ----------------------------
CREATE OR REPLACE VIEW `v_active_refresh_tokens` AS
SELECT
    rt.*,
    u.username,
    u.email,
    u.full_name
FROM refresh_token rt
         INNER JOIN users u ON rt.user_id = u.id
WHERE rt.deleted_at IS NULL
  AND rt.revoked = FALSE
  AND rt.expiry_date > NOW(6);

-- ----------------------------
-- Stored Procedures
-- ----------------------------

-- Procedure 1: Clean up expired tokens
DROP PROCEDURE IF EXISTS `sp_cleanup_expired_refresh_tokens`;

DELIMITER $$

CREATE PROCEDURE `sp_cleanup_expired_refresh_tokens`()
BEGIN
    DECLARE affected_rows INT DEFAULT 0;

    -- Soft delete expired tokens
    UPDATE refresh_token
    SET deleted_at = NOW(6),
        deleted_by = 'system_cleanup',
        updated_at = NOW(6),
        updated_by = 'system_cleanup'
    WHERE expiry_date < NOW(6)
      AND deleted_at IS NULL
      AND revoked = FALSE;

    SET affected_rows = ROW_COUNT();

    -- Hard delete tokens older than 90 days
    DELETE FROM refresh_token
    WHERE deleted_at < DATE_SUB(NOW(6), INTERVAL 90 DAY)
       OR (revoked = TRUE AND revoked_at < DATE_SUB(NOW(6), INTERVAL 90 DAY));

    SELECT CONCAT('Cleaned up ', affected_rows, ' expired refresh tokens') AS result;
END$$

DELIMITER ;

-- Procedure 2: Revoke all tokens for a user
DROP PROCEDURE IF EXISTS `sp_revoke_user_refresh_tokens`;

DELIMITER $$

CREATE PROCEDURE `sp_revoke_user_refresh_tokens`(
    IN p_user_id BIGINT,
    IN p_revoked_by VARCHAR(100),
    IN p_reason VARCHAR(255)
)
BEGIN
    UPDATE refresh_token
    SET revoked = TRUE,
        revoked_at = NOW(6),
        revoked_by = p_revoked_by,
        revoked_reason = p_reason,
        updated_at = NOW(6),
        updated_by = p_revoked_by
    WHERE user_id = p_user_id
      AND deleted_at IS NULL
      AND revoked = FALSE;

    SELECT ROW_COUNT() AS tokens_revoked;
END$$

DELIMITER ;

-- Procedure 3: Get token statistics
DROP PROCEDURE IF EXISTS `sp_get_refresh_token_stats`;

DELIMITER $$

CREATE PROCEDURE `sp_get_refresh_token_stats`()
BEGIN
    -- Overall statistics
    SELECT
        COUNT(*) AS total_tokens,
        COUNT(CASE WHEN deleted_at IS NULL AND revoked = FALSE THEN 1 END) AS active_tokens,
        COUNT(CASE WHEN revoked = TRUE THEN 1 END) AS revoked_tokens,
        COUNT(CASE WHEN deleted_at IS NOT NULL THEN 1 END) AS deleted_tokens,
        COUNT(CASE WHEN expiry_date < NOW(6) THEN 1 END) AS expired_tokens,
        COUNT(DISTINCT user_id) AS unique_users,
        COUNT(DISTINCT ip_address) AS unique_ip_addresses,
        AVG(use_count) AS avg_use_count,
        MAX(use_count) AS max_use_count
    FROM refresh_token;

    -- Top users by active tokens
    SELECT
        u.username,
        u.email,
        COUNT(*) AS active_token_count
    FROM refresh_token rt
             INNER JOIN users u ON rt.user_id = u.id
    WHERE rt.deleted_at IS NULL
      AND rt.revoked = FALSE
      AND rt.expiry_date > NOW(6)
    GROUP BY u.username, u.email
    ORDER BY active_token_count DESC
    LIMIT 10;
END$$

DELIMITER ;

-- ----------------------------
-- Create Event for automatic cleanup
-- ----------------------------
DROP EVENT IF EXISTS `evt_cleanup_expired_refresh_tokens`;

CREATE EVENT `evt_cleanup_expired_refresh_tokens`
    ON SCHEDULE EVERY 1 DAY
        STARTS CURRENT_TIMESTAMP
    DO
    CALL sp_cleanup_expired_refresh_tokens();

-- Enable event scheduler
SET GLOBAL event_scheduler = ON;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Verify table structure
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'refresh_token'
ORDER BY ORDINAL_POSITION;

-- Verify indexes
SELECT
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX,
    NON_UNIQUE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'refresh_token'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- Verify foreign keys
SELECT
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'refresh_token'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- =====================================================
-- NOTES FOR USAGE
-- =====================================================
-- 1. Thay thế phần định nghĩa bảng refresh_token trong init-db.sql bằng nội dung file này
-- 2. Không cần chạy file migration riêng biệt
-- 3. Bảng đã có đầy đủ các fields theo RefreshToken.java entity:
--    - Audit fields: created_at, created_by, updated_at, updated_by
--    - Soft delete: deleted_at, deleted_by
--    - Revocation: revoked, revoked_at, revoked_by, revoked_reason
--    - Security: ip_address, user_agent, device_id, device_name
--    - Rotation: token_family, parent_token_id
--    - Tracking: last_used_at, use_count
-- 4. Tất cả các indexes đã được tạo sẵn
-- 5. Stored procedures và event scheduler đã được thiết lập
-- =====================================================

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
INSERT INTO `user_roles` VALUES (2, 'STAFF');
INSERT INTO `user_roles` VALUES (3, 'CUSTOMER');
INSERT INTO `user_roles` VALUES (4, 'SYSTEM');

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
INSERT INTO `users` VALUES (3, 'Thu Duc, Ho Chi Minh', '2025-06-26 09:00:10.484677', 'dinhngocvuit@gmail.com', 'Ngoc Vu', '$2a$10$/MIrJqi2rExvSW6.tc7/hO587VjHxUOjO7CUbYzVFQsRWKExsfA.e', '0962805614', 'admin333', 1);
INSERT INTO `users` VALUES (4, 'Kim Lien, Ha Noi', '2025-07-01 15:18:11.825185', 'ngocvu.ngoc06@gmail.com', 'Push', '$2a$10$/MIrJqi2rExvSW6.tc7/hO587VjHxUOjO7CUbYzVFQsRWKExsfA.e', '0987654332', 'testpush', 3);
INSERT INTO `users` VALUES (5, 'Xuan Loc, Dong Nai', '2025-07-01 16:08:41.616760', 'ngocvu.ngoc06@gmail.com', 'Ngoc Vu', '$2a$10$/MIrJqi2rExvSW6.tc7/hO587VjHxUOjO7CUbYzVFQsRWKExsfA.e', '231231', 'testpush1', 3);
INSERT INTO `users` VALUES (6, 'Tan Binh, Ho Chi Minh', '2025-07-02 15:33:53.182822', 'ngocvu.ngoc06@gmail.com', 'Ngoc Vu', '$2a$10$/MIrJqi2rExvSW6.tc7/hO587VjHxUOjO7CUbYzVFQsRWKExsfA.e', '0987654321', 'newuser', 3);

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


-- =====================================================
-- MIGRATION: Add Audit & Soft Delete Fields
-- Chạy sau khi INSERT data xong
-- =====================================================

-- ===== 1. USERS TABLE =====
ALTER TABLE users
    ADD COLUMN created_by VARCHAR(100) COMMENT 'Username who created this record' AFTER created_at,
    ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp' AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) COMMENT 'Username who last updated this record' AFTER updated_at,
    ADD COLUMN deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp' AFTER updated_by,
    ADD COLUMN deleted_by VARCHAR(100) COMMENT 'Username who deleted this record' AFTER deleted_at,
    ADD COLUMN status ENUM('ACTIVE','INACTIVE','BANNED') NOT NULL DEFAULT 'ACTIVE' COMMENT 'User status' AFTER deleted_by,
    ADD INDEX idx_users_deleted_at (deleted_at),
    ADD INDEX idx_users_created_at (created_at);

UPDATE users
SET created_by = 'system',
    updated_by = 'system'
WHERE created_by IS NULL;

-- ===== 2. DISHES TABLE =====
ALTER TABLE dishes
    ADD COLUMN created_by VARCHAR(100) COMMENT 'Username who created this record' AFTER created_at,
    ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp' AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) COMMENT 'Username who last updated this record' AFTER updated_at,
    ADD COLUMN deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp' AFTER updated_by,
    ADD COLUMN deleted_by VARCHAR(100) COMMENT 'Username who deleted this record' AFTER deleted_at,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic locking version' AFTER deleted_by,
    ADD COLUMN average_rating DECIMAL(3,2) DEFAULT 0.00 COMMENT 'Average rating' AFTER version,
    ADD COLUMN total_reviews INT NOT NULL DEFAULT 0 COMMENT 'Total reviews count' AFTER average_rating,
    ADD INDEX idx_dishes_deleted_at (deleted_at),
    ADD INDEX idx_dishes_created_at (created_at),
    ADD INDEX idx_dishes_category (category),
    ADD INDEX idx_dishes_available (available),
    ADD INDEX idx_dishes_order_count (order_count),
    ADD INDEX idx_dishes_average_rating (average_rating);

UPDATE dishes
SET created_by = 'system',
    updated_by = 'system',
    version = 0,
    average_rating = 0.00,
    total_reviews = 0
WHERE created_by IS NULL;

-- ===== 3. BOOKINGS TABLE =====
ALTER TABLE bookings
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp' AFTER total_amount,
    ADD COLUMN created_by VARCHAR(100) COMMENT 'Username who created this record' AFTER created_at,
    ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp' AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) COMMENT 'Username who last updated this record' AFTER updated_at,
    ADD COLUMN deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp' AFTER updated_by,
    ADD COLUMN deleted_by VARCHAR(100) COMMENT 'Username who deleted this record' AFTER deleted_at,
    ADD INDEX idx_bookings_deleted_at (deleted_at),
    ADD INDEX idx_bookings_created_at (created_at),
    ADD INDEX idx_bookings_booking_time (booking_time),
    ADD INDEX idx_bookings_status (status),
    ADD INDEX idx_bookings_user_id (user_id);

UPDATE bookings
SET created_by = 'system',
    updated_by = 'system',
    created_at = booking_time
WHERE created_by IS NULL;

-- ===== 4. TABLES TABLE =====
ALTER TABLE tables
    MODIFY COLUMN status ENUM('AVAILABLE','OCCUPIED','RESERVED','MAINTENANCE','BOOKED') DEFAULT 'AVAILABLE',
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp' AFTER table_name,
    ADD COLUMN created_by VARCHAR(100) COMMENT 'Username who created this record' AFTER created_at,
    ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp' AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) COMMENT 'Username who last updated this record' AFTER updated_at,
    ADD COLUMN deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp' AFTER updated_by,
    ADD COLUMN deleted_by VARCHAR(100) COMMENT 'Username who deleted this record' AFTER deleted_at,
    ADD INDEX idx_tables_deleted_at (deleted_at),
    ADD INDEX idx_tables_created_at (created_at),
    ADD INDEX idx_tables_status (status),
    ADD INDEX idx_tables_capacity (capacity);

UPDATE tables
SET created_by = 'system',
    updated_by = 'system'
WHERE created_by IS NULL;

-- ===== 5. PAYMENTS TABLE =====
ALTER TABLE payments
    MODIFY COLUMN status ENUM('PENDING','COMPLETED','FAILED','CANCELLED','REFUNDED') DEFAULT 'PENDING',
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp' AFTER booking_id,
    ADD COLUMN created_by VARCHAR(100) COMMENT 'Username who created this record' AFTER created_at,
    ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp' AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) COMMENT 'Username who last updated this record' AFTER updated_at,
    ADD COLUMN deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp' AFTER updated_by,
    ADD COLUMN deleted_by VARCHAR(100) COMMENT 'Username who deleted this record' AFTER deleted_at,
    ADD COLUMN transaction_reference VARCHAR(100) UNIQUE COMMENT 'Transaction reference ID' AFTER deleted_by,
    ADD COLUMN customer_note TEXT COMMENT 'Customer note' AFTER transaction_reference,
    ADD COLUMN admin_note TEXT COMMENT 'Admin note' AFTER customer_note,
    ADD COLUMN processed_at TIMESTAMP NULL COMMENT 'When payment was processed' AFTER admin_note,
    ADD COLUMN processed_by VARCHAR(100) COMMENT 'Admin who processed payment' AFTER processed_at,
    ADD INDEX idx_payments_deleted_at (deleted_at),
    ADD INDEX idx_payments_created_at (created_at),
    ADD INDEX idx_payments_status (status),
    ADD INDEX idx_payments_booking_id (booking_id),
    ADD INDEX idx_payments_transaction_ref (transaction_reference);

UPDATE payments
SET created_by = 'system',
    updated_by = 'system',
    created_at = payment_time
WHERE created_by IS NULL;

-- ===== 6. ORDER_HISTORY TABLE =====
-- Không cần soft delete vì là audit trail
ALTER TABLE order_history
    ADD COLUMN created_by VARCHAR(100) COMMENT 'Username who created this record' AFTER created_at,
    ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp' AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) COMMENT 'Username who last updated this record' AFTER updated_at,
    ADD INDEX idx_order_history_created_at (created_at),
    ADD INDEX idx_order_history_booking_id (booking_id),
    ADD INDEX idx_order_history_user_id (user_id),
    ADD INDEX idx_order_history_dish_id (dish_id);

UPDATE order_history
SET created_by = 'system',
    updated_by = 'system'
WHERE created_by IS NULL;

-- ===== 7. PREORDERS TABLE =====
-- Không cần soft delete vì là phần của booking
ALTER TABLE preorders
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp' AFTER dish_id,
    ADD COLUMN created_by VARCHAR(100) COMMENT 'Username who created this record' AFTER created_at,
    ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp' AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) COMMENT 'Username who last updated this record' AFTER updated_at,
    ADD INDEX idx_preorders_created_at (created_at),
    ADD INDEX idx_preorders_booking_id (booking_id),
    ADD INDEX idx_preorders_dish_id (dish_id);

UPDATE preorders
SET created_by = 'system',
    updated_by = 'system'
WHERE created_by IS NULL;

-- ===== 8. IMAGES TABLE =====
ALTER TABLE images
    ADD COLUMN created_by VARCHAR(100) COMMENT 'Username who created this record' AFTER uploaded_at,
    ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp' AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) COMMENT 'Username who last updated this record' AFTER updated_at,
    ADD COLUMN deleted_at TIMESTAMP NULL COMMENT 'Soft delete timestamp' AFTER updated_by,
    ADD COLUMN deleted_by VARCHAR(100) COMMENT 'Username who deleted this record' AFTER deleted_at,
    ADD INDEX idx_images_deleted_at (deleted_at),
    ADD INDEX idx_images_uploaded_at (uploaded_at);

UPDATE images
SET created_by = 'system',
    updated_by = 'system'
WHERE created_by IS NULL;

-- ===== 9. NOTIFICATIONS TABLE =====
-- Không cần soft delete vì là notification history
ALTER TABLE notifications
    ADD COLUMN created_by VARCHAR(100) COMMENT 'Username who created this record' AFTER created_at,
    ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp' AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) COMMENT 'Username who last updated this record' AFTER updated_at,
    ADD INDEX idx_notifications_created_at (created_at);

UPDATE notifications
SET created_by = 'system',
    updated_by = 'system'
WHERE created_by IS NULL;

-- ===== 10. USER_ROLES TABLE =====
ALTER TABLE user_roles
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    ADD COLUMN created_by VARCHAR(100) COMMENT 'Username who created this record',
    ADD COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    ADD COLUMN updated_by VARCHAR(100) COMMENT 'Username who last updated this record';

UPDATE user_roles
SET created_by = 'system',
    updated_by = 'system'
WHERE created_by IS NULL;

-- ===== 11. REVIEWS TABLE (Nếu có) =====
-- Tạo bảng reviews nếu chưa có
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

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check all tables have audit fields
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'restaurant'
  AND TABLE_NAME IN ('users', 'dishes', 'bookings', 'tables', 'payments', 'order_history', 'preorders', 'images', 'notifications')
  AND COLUMN_NAME IN ('created_at', 'created_by', 'updated_at', 'updated_by', 'deleted_at', 'deleted_by')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- Check indexes were created
SELECT
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'restaurant'
  AND (INDEX_NAME LIKE 'idx_%deleted_at' OR INDEX_NAME LIKE 'idx_%created_at')
ORDER BY TABLE_NAME, INDEX_NAME;

-- Count records in each table
SELECT
    'users' as table_name, COUNT(*) as record_count FROM users
UNION ALL
SELECT 'dishes', COUNT(*) FROM dishes
UNION ALL
SELECT 'bookings', COUNT(*) FROM bookings
UNION ALL
SELECT 'tables', COUNT(*) FROM tables
UNION ALL
SELECT 'payments', COUNT(*) FROM payments
UNION ALL
SELECT 'order_history', COUNT(*) FROM order_history
UNION ALL
SELECT 'preorders', COUNT(*) FROM preorders
UNION ALL
SELECT 'images', COUNT(*) FROM images
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- MIGRATION COMPLETED
-- =====================================================
-- Summary:
-- ✅ Added audit fields (created_by, updated_by) to all tables
-- ✅ Added soft delete fields (deleted_at, deleted_by) to relevant tables
-- ✅ Added indexes for performance
-- ✅ Added version field to dishes for optimistic locking
-- ✅ Updated payment status enum to match Java entity
-- ✅ Updated table status enum to include RESERVED and MAINTENANCE
-- ✅ Created reviews table with full audit support
-- ✅ All existing data updated with 'system' as creator
-- =====================================================