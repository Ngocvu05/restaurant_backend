
-- Generated SQL Script for Restaurant Management Database
-- Generated on 2025-06-18 04:22:40

-- 1. USER_ROLE
CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. USERS
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone_number VARCHAR(50),
    address VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES user_roles(id)
);

-- 3. TABLE_ENTITY
CREATE TABLE tables (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL
);

-- 4. DISH
DROP TABLE IF EXISTS dishes;

CREATE TABLE dishes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    category VARCHAR(255)
);

-- 5. BOOKING
CREATE TABLE bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    table_id BIGINT NOT NULL,
    booking_time DATETIME NOT NULL,
    number_of_guests INT,
    note TEXT,
    status VARCHAR(50),
    number_of_people INT,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_booking_table FOREIGN KEY (table_id) REFERENCES tables(id)
);

-- 6. BOOKING_DISHES
CREATE TABLE booking_dishes (
    booking_id BIGINT,
    dish_id BIGINT,
    PRIMARY KEY (booking_id, dish_id),
    CONSTRAINT fk_bookingdish_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_bookingdish_dish FOREIGN KEY (dish_id) REFERENCES dishes(id)
);

-- 7. ORDER_HISTORY
CREATE TABLE order_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT,
    dish_id BIGINT,
    quantity INT NOT NULL,
    served BOOLEAN DEFAULT FALSE,
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2),
    CONSTRAINT fk_order_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_order_dish FOREIGN KEY (dish_id) REFERENCES dishes(id),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 8. PAYMENT
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    payment_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- 9. IMAGE
-- Bảng images (vì liên quan tới dishes)
DROP TABLE IF EXISTS images;

CREATE TABLE images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(500) NOT NULL,
    user_id BIGINT,
    dish_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (dish_id) REFERENCES dishes(id) ON DELETE CASCADE
);
-- Bảng trung gian giữa bookings và dishes (many-to-many)
DROP TABLE IF EXISTS booking_dishes;

CREATE TABLE booking_dishes (
    booking_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    PRIMARY KEY (booking_id, dish_id),
    CONSTRAINT fk_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_dish
        FOREIGN KEY (dish_id) REFERENCES dishes(id)
        ON DELETE CASCADE
);

-- Insert sample data into user_roles
INSERT INTO user_roles (id, name) VALUES
(1, 'ADMIN'),
(2, 'CUSTOMER');

-- Insert sample users
INSERT INTO users (id, username, password, full_name, email, phone_number, address, role_id, created_at) VALUES
(1, 'john_doe', 'password123', 'John Doe', 'john@example.com', '1234567890', '123 Main St', 2, NOW()),
(2, 'admin_user', 'adminpass', 'Admin User', 'admin@example.com', '0987654321', '456 Admin Rd', 1, NOW());

-- Insert tables
INSERT INTO tables (id, name, capacity) VALUES
(1, 'Table 1', 4),
(2, 'Table 2', 6);

-- Insert dishes
INSERT INTO dishes (id, name, description, price, category, available) VALUES
(1, 'Pho', 'Vietnamese beef noodle soup', 50000.00, 'MAIN', true),
(2, 'Spring Rolls', 'Fried spring rolls with pork', 30000.00, 'APPETIZER', true);

-- Insert bookings
INSERT INTO bookings (id, user_id, table_id, booking_time, number_of_guests, note, status, number_of_people) VALUES
(1, 1, 1, NOW(), 2, 'Birthday dinner', 'CONFIRMED', 2),
(2, 2, 2, NOW(), 4, 'Meeting', 'PENDING', 4);

-- Insert booking_dishes
INSERT INTO booking_dishes (booking_id, dish_id) VALUES
(1, 1),
(1, 2),
(2, 2);

-- Insert order_history
INSERT INTO order_history (id, booking_id, dish_id, quantity, served, note, created_at, user_id, total_amount) VALUES
(1, 1, 1, 2, true, 'Extra beef', NOW(), 1, 100000.00),
(2, 1, 2, 3, false, '', NOW(), 1, 90000.00);

-- Insert payments
INSERT INTO payments (id, booking_id, amount, payment_method, payment_time, status) VALUES
(1, 1, 190000.00, 'CASH', NOW(), 'PAID'),
(2, 2, 30000.00, 'CREDIT_CARD', NOW(), 'PENDING');
