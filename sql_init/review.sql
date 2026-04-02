-- ========================
-- CREATE TABLES
-- ========================

-- Roles
CREATE TABLE roles (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL
);

-- Users
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(100),
                       email VARCHAR(100) UNIQUE,
                       avatar_url VARCHAR(500),
                       provider VARCHAR(50),
                       provider_id VARCHAR(100),
                       role_id BIGINT,
                       status VARCHAR(20),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Dishes
CREATE TABLE dishes (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        description TEXT,
                        price DECIMAL(10, 2),
                        category VARCHAR(50),
                        image_url VARCHAR(500),
                        is_active BOOLEAN DEFAULT true,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- For review stats
                        average_rating DECIMAL(3,2) DEFAULT 0.00,
                        total_reviews INT DEFAULT 0
);

-- Reviews
CREATE TABLE reviews (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         dish_id BIGINT NOT NULL,
                         customer_name VARCHAR(100) NOT NULL,
                         customer_email VARCHAR(255),
                         customer_avatar VARCHAR(500),
                         rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         comment TEXT NOT NULL,
                         is_active BOOLEAN NOT NULL DEFAULT TRUE,
                         is_verified BOOLEAN NOT NULL DEFAULT FALSE,
                         ip_address VARCHAR(45),
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         CONSTRAINT fk_reviews_dish_id FOREIGN KEY (dish_id) REFERENCES dishes(id) ON DELETE CASCADE,
                         INDEX idx_reviews_dish_id (dish_id),
                         INDEX idx_reviews_rating (rating),
                         INDEX idx_reviews_created_at (created_at),
                         INDEX idx_reviews_is_active (is_active),
                         INDEX idx_reviews_is_verified (is_verified),
                         INDEX idx_reviews_customer_email (customer_email),
                         INDEX idx_reviews_ip_address (ip_address),
                         INDEX idx_reviews_dish_active (dish_id, is_active),
                         INDEX idx_reviews_dish_rating_active (dish_id, rating, is_active)
);

-- ========================
-- SAMPLE DATA
-- ========================

-- Roles
INSERT INTO roles (name) VALUES
                             ('ROLE_USER'), ('ROLE_ADMIN');

-- Users
INSERT INTO users (username, email, password, role_id, status)
VALUES ('admin', 'admin@example.com', '123456', 2, 'ACTIVE');

-- Dishes
INSERT INTO dishes (name, description, price, category, image_url)
VALUES
    ('Phở bò', 'Phở bò truyền thống', 50000, 'Món nước', 'pho.jpg'),
    ('Cơm tấm', 'Cơm tấm sườn bì chả', 45000, 'Cơm', 'comtam.jpg');

-- Reviews
INSERT INTO reviews (dish_id, customer_name, customer_email, rating, comment, is_active, is_verified) VALUES
                                                                                                          (1, 'Nguyễn Văn A', 'nguyenvana@email.com', 5, 'Món ăn rất ngon, phục vụ tuyệt vời!', true, true),
                                                                                                          (1, 'Trần Thị B', 'tranthib@email.com', 4, 'Chất lượng tốt, giá cả hợp lý.', true, true),
                                                                                                          (1, 'Lê Văn C', 'levanc@email.com', 5, 'Tuyệt vời! Sẽ quay lại lần nữa.', true, false),
                                                                                                          (2, 'Phạm Thị D', 'phamthid@email.com', 3, 'Bình thường, không có gì đặc biệt.', true, true),
                                                                                                          (2, 'Hoàng Văn E', 'hoangvane@email.com', 4, 'Ngon, nhưng hơi mặn một chút.', true, true);

-- ========================
-- UPDATE DISH RATING STATS
-- ========================
UPDATE dishes d
SET
    average_rating = (
        SELECT ROUND(AVG(r.rating), 2)
        FROM reviews r
        WHERE r.dish_id = d.id AND r.is_active = true
    ),
    total_reviews = (
        SELECT COUNT(*)
        FROM reviews r
        WHERE r.dish_id = d.id AND r.is_active = true
    )
WHERE d.id IN (SELECT DISTINCT dish_id FROM reviews);

-- ========================
-- VIEW
-- ========================
CREATE VIEW dish_review_stats AS
SELECT
    d.id as dish_id,
    d.name as dish_name,
    COUNT(r.id) as total_reviews,
    ROUND(AVG(r.rating), 2) as average_rating,
    SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END) as one_star,
    SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END) as two_star,
    SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END) as three_star,
    SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END) as four_star,
    SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END) as five_star,
    MAX(r.created_at) as last_review_date
FROM dishes d
         LEFT JOIN reviews r ON d.id = r.dish_id AND r.is_active = true
GROUP BY d.id, d.name;

-- ========================
-- TRIGGERS
-- ========================
DELIMITER //

CREATE TRIGGER update_dish_rating_after_review_insert
    AFTER INSERT ON reviews
    FOR EACH ROW
BEGIN
    UPDATE dishes
    SET
        average_rating = (
            SELECT ROUND(AVG(rating), 2)
            FROM reviews
            WHERE dish_id = NEW.dish_id AND is_active = true
        ),
        total_reviews = (
            SELECT COUNT(*)
            FROM reviews
            WHERE dish_id = NEW.dish_id AND is_active = true
        )
    WHERE id = NEW.dish_id;
END//

CREATE TRIGGER update_dish_rating_after_review_update
    AFTER UPDATE ON reviews
    FOR EACH ROW
BEGIN
    UPDATE dishes
    SET
        average_rating = (
            SELECT ROUND(AVG(rating), 2)
            FROM reviews
            WHERE dish_id = NEW.dish_id AND is_active = true
        ),
        total_reviews = (
            SELECT COUNT(*)
            FROM reviews
            WHERE dish_id = NEW.dish_id AND is_active = true
        )
    WHERE id = NEW.dish_id;
END//

CREATE TRIGGER update_dish_rating_after_review_delete
    AFTER DELETE ON reviews
    FOR EACH ROW
BEGIN
    UPDATE dishes
    SET
        average_rating = (
            SELECT ROUND(AVG(rating), 2)
            FROM reviews
            WHERE dish_id = OLD.dish_id AND is_active = true
        ),
        total_reviews = (
            SELECT COUNT(*)
            FROM reviews
            WHERE dish_id = OLD.dish_id AND is_active = true
        )
    WHERE id = OLD.dish_id;
END//

DELIMITER ;
