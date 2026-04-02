-- Create analytics database
CREATE DATABASE analytics;
USE analytics;

-- Sales Reports Table
CREATE TABLE sales_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    report_date DATE NOT NULL,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_booking_id (booking_id),
    INDEX idx_report_date (report_date),
    INDEX idx_status (status)
);

-- Daily Statistics Table
CREATE TABLE daily_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE UNIQUE NOT NULL,
    total_bookings INT DEFAULT 0,
    total_revenue DECIMAL(12, 2) DEFAULT 0,
    total_customers INT DEFAULT 0,
    popular_dish_id BIGINT,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stat_date (stat_date)
);

-- Stored Procedure: Aggregate Daily Stats
DELIMITER $$
CREATE PROCEDURE aggregate_daily_stats(IN target_date DATE)
BEGIN
    DECLARE total_bookings_count INT;
    DECLARE total_revenue_sum DECIMAL(12, 2);
    DECLARE total_customers_count INT;

    -- Calculate totals from sales_reports
    SELECT
        COUNT(*),
        COALESCE(SUM(amount), 0),
        COUNT(DISTINCT booking_id)
    INTO
        total_bookings_count,
        total_revenue_sum,
        total_customers_count
    FROM sales_reports
    WHERE report_date = target_date;

    -- Insert or update daily statistics
    INSERT INTO daily_statistics (
        stat_date,
        total_bookings,
        total_revenue,
        total_customers
    )
    VALUES (
        target_date,
        total_bookings_count,
        total_revenue_sum,
        total_customers_count
    )
    ON DUPLICATE KEY UPDATE
        total_bookings = total_bookings_count,
        total_revenue = total_revenue_sum,
        total_customers = total_customers_count,
        version = version + 1;
END$$
DELIMITER ;

-- Test data
INSERT INTO sales_reports (booking_id, amount, report_date, status)
VALUES
    (1, 150000, '2025-01-01', 'COMPLETED'),
    (2, 200000, '2025-01-01', 'COMPLETED'),
    (3, 180000, '2025-01-02', 'COMPLETED');

CALL aggregate_daily_stats('2025-01-01');