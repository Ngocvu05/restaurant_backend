-- User Roles
INSERT INTO user_roles (id, name)
VALUES (1, 'ADMIN'),
       (2, 'STAFF'),
       (3, 'CUSTOMER');

-- Users
INSERT INTO users (id, username, password, full_name, email, phone_number, created_at, address, role_id)
VALUES (1, 'admin', 'admin123', 'Admin User', 'admin@example.com', '0123456789', NOW(), '123 Admin St', 1),
       (2, 'customer', 'customer123', 'Customer A', 'cust@example.com', '0987654321', NOW(), '456 Customer Ave', 3);

-- Tables
INSERT INTO tables (id, table_name, capacity, status)
VALUES (1, 'T1', 4, 'AVAILABLE'),
       (2, 'T2', 6, 'BOOKED');

-- Dishes
-- XÓA DỮ LIỆU CŨ (nếu cần)
DELETE
FROM images;
DELETE
FROM dishes;

-- MÓN KHAI VỊ
INSERT INTO dishes (id, name, description, price, available, category, created_at, order_count)
VALUES (1, 'Gỏi cuốn tôm thịt', 'Món khai vị truyền thống với tôm và thịt heo tươi', 30000, true, 'Khai vị', NOW(), 12),
       (2, 'Chả giò rế', 'Chả giò chiên giòn rụm, ăn kèm nước mắm chua ngọt', 25000, true, 'Khai vị', NOW(), 20);

-- MÓN CHÍNH
INSERT INTO dishes (id, name, description, price, available, category, created_at, order_count)
VALUES (3, 'Cơm sườn nướng', 'Cơm tấm với sườn nướng đậm đà và trứng ốp la', 55000, true, 'Món chính', NOW(), 45),
       (4, 'Phở bò đặc biệt', 'Phở nước dùng đậm vị với tái, nạm, gân, bò viên', 60000, true, 'Món chính', NOW(), 50),
       (5, 'Bún bò Huế', 'Món đặc sản miền Trung cay nồng và thơm ngon', 58000, true, 'Món chính', NOW(), 38);

-- TRÁNG MIỆNG
INSERT INTO dishes (id, name, description, price, available, category, created_at, order_count)
VALUES (6, 'Chè khúc bạch', 'Chè mát lạnh với khúc bạch hạnh nhân và vải', 28000, true, 'Tráng miệng', NOW(), 18),
       (7, 'Bánh flan caramel', 'Bánh mềm mịn tan chảy với lớp caramel ngọt ngào', 25000, true, 'Tráng miệng', NOW(),
        22);

-- ĐỒ UỐNG
INSERT INTO dishes (id, name, description, price, available, category, created_at, order_count)
VALUES (8, 'Trà đào cam sả', 'Trà trái cây mát lạnh, thơm vị cam và sả', 30000, true, 'Đồ uống', NOW(), 33),
       (9, 'Sinh tố bơ', 'Sinh tố bơ nguyên chất, béo ngậy', 32000, true, 'Đồ uống', NOW(), 17),
       (10, 'Cà phê sữa đá', 'Cà phê truyền thống, vị đậm đà, thơm nồng', 25000, true, 'Đồ uống', NOW(), 41);

-- Bookings
INSERT INTO bookings (id, booking_time, number_of_guests, number_of_people, status, table_id, user_id, note)
VALUES (1, NOW(), 4, 4, 'CONFIRMED', 1, 2, 'Birthday party');

-- Preorders
INSERT INTO preorders (id, booking_id, dish_id, quantity, note)
VALUES (1, 1, 1, 2, 'Extra cheese');

-- Payments
INSERT INTO payments (id, amount, payment_method, payment_time, status, booking_id)
VALUES (1, 240.00, 'CASH', NOW(), 'PAID', 1);
