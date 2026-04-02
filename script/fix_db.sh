#!/bin/bash

echo "=================================="
echo "Fix Database Issues Script"
echo "=================================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if init-analytics-db.sql exists
if [ ! -f "init-analytics-db.sql" ]; then
    print_error "File init-analytics-db.sql not found!"
    print_info "Creating init-analytics-db.sql..."

    cat > init-analytics-db.sql << 'EOF'
-- Create analytics database
CREATE DATABASE IF NOT EXISTS analytics CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant full permissions to restaurant_user
GRANT ALL PRIVILEGES ON analytics.* TO 'restaurant_user'@'%';

-- Create some basic tables for analytics
USE analytics;

CREATE TABLE IF NOT EXISTS user_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSON,
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_timestamp (action_timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    metadata JSON,
    INDEX idx_order_id (order_id),
    INDEX idx_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS revenue_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    total_revenue DECIMAL(12,2) DEFAULT 0.00,
    total_orders INT DEFAULT 0,
    average_order_value DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

FLUSH PRIVILEGES;
EOF

    print_info "Created init-analytics-db.sql successfully!"
fi

# Step 1: Stop all services
print_info "Step 1: Stopping all services..."
docker-compose down

# Step 2: Remove MySQL data
print_warning "Step 2: Removing old MySQL data..."
read -p "This will delete all MySQL data. Are you sure? (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    print_error "Operation cancelled by user."
    exit 1
fi

sudo rm -rf ./mysql_data
print_info "MySQL data removed."

# Step 3: Start MySQL only
print_info "Step 3: Starting MySQL..."
docker-compose up -d mysql

# Step 4: Wait for MySQL to be healthy
print_info "Step 4: Waiting for MySQL to be ready..."
counter=0
max_attempts=30

while [ $counter -lt $max_attempts ]; do
    if docker exec mysql mysqladmin ping -h localhost --silent 2>/dev/null; then
        print_info "MySQL is ready!"
        break
    fi
    counter=$((counter + 1))
    echo -n "."
    sleep 2
done

if [ $counter -eq $max_attempts ]; then
    print_error "MySQL failed to start after $max_attempts attempts"
    exit 1
fi

# Wait additional 10 seconds for init scripts to complete
print_info "Waiting for initialization scripts to complete..."
sleep 10

# Step 5: Verify databases
print_info "Step 5: Verifying databases..."
docker exec mysql mysql -u restaurant_user -prestaurant_pass -e "SHOW DATABASES;" 2>/dev/null

# Step 6: Check permissions
print_info "Step 6: Checking user permissions..."
docker exec mysql mysql -u root -proot -e "SHOW GRANTS FOR 'restaurant_user'@'%';" 2>/dev/null

# Step 7: Test analytics database access
print_info "Step 7: Testing analytics database access..."
if docker exec mysql mysql -u restaurant_user -prestaurant_pass -e "USE analytics; SHOW TABLES;" 2>/dev/null; then
    print_info "✓ Analytics database is accessible!"
else
    print_error "✗ Cannot access analytics database!"
    print_info "Attempting to fix permissions..."

    docker exec mysql mysql -u root -proot << 'EOF'
GRANT ALL PRIVILEGES ON analytics.* TO 'restaurant_user'@'%';
FLUSH PRIVILEGES;
EOF

    print_info "Permissions fixed. Testing again..."
    docker exec mysql mysql -u restaurant_user -prestaurant_pass -e "USE analytics; SHOW TABLES;"
fi

# Step 8: Start other services
print_info "Step 8: Starting all services..."
docker-compose up -d

# Step 9: Wait for services
print_info "Step 9: Waiting for services to start (60 seconds)..."
sleep 60

# Step 10: Check user-service logs
print_info "Step 10: Checking user-service logs..."
docker logs --tail=50 user-service

# Step 11: Final status check
print_info "=================================="
print_info "Final Status Check"
print_info "=================================="

# Check MySQL
if docker ps | grep -q mysql; then
    print_info "✓ MySQL: Running"
else
    print_error "✗ MySQL: Stopped"
fi

# Check MongoDB
if docker ps | grep -q mongodb; then
    print_info "✓ MongoDB: Running"
else
    print_error "✗ MongoDB: Stopped"
fi

# Check Redis
if docker ps | grep -q redis; then
    print_info "✓ Redis: Running"
else
    print_error "✗ Redis: Stopped"
fi

# Check RabbitMQ
if docker ps | grep -q rabbitmq; then
    print_info "✓ RabbitMQ: Running"
else
    print_error "✗ RabbitMQ: Stopped"
fi

# Check user-service
if docker ps | grep -q user-service; then
    print_info "✓ user-service: Running"

    # Check if service is healthy
    if docker logs user-service 2>&1 | grep -q "Started RestaurantApplication"; then
        print_info "✓ user-service: Started successfully!"
    else
        print_warning "⚠ user-service: Running but may have issues. Check logs:"
        print_info "  docker logs -f user-service"
    fi
else
    print_error "✗ user-service: Stopped"
fi

print_info "=================================="
print_info "Script completed!"
print_info "=================================="
print_info ""
print_info "Next steps:"
print_info "1. Check user-service logs: docker logs -f user-service"
print_info "2. Test health endpoint: curl http://localhost:8081/actuator/health"
print_info "3. View all running services: docker-compose ps"