#!/bin/bash

echo "=========================================="
echo "Deploy User Service with Analytics DB"
echo "=========================================="

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Check prerequisites
print_step "Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    print_error "Docker not found. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose not found. Please install Docker Compose first."
    exit 1
fi

# Set base paths
BASE_DIR="backend-service/src/main/java/com/management/restaurant"
RESOURCES_DIR="backend-service/src/main/resources"

if [ ! -d "$BASE_DIR" ]; then
    print_error "Directory not found: $BASE_DIR"
    print_info "Please run this script from project root"
    exit 1
fi

# Step 1: Create directory structure
print_step "Step 1: Creating directory structure..."

mkdir -p "$BASE_DIR/analytics/model"
mkdir -p "$BASE_DIR/analytics/repository"
mkdir -p "$BASE_DIR/analytics/service"
mkdir -p "$BASE_DIR/helper"

print_info "✓ Directory structure created"

# Step 2: Backup existing files
print_step "Step 2: Backing up existing files..."

TIMESTAMP=$(date +%Y%m%d_%H%M%S)

if [ -f "$BASE_DIR/config/DistributedTransactionConfig.java" ]; then
    cp "$BASE_DIR/config/DistributedTransactionConfig.java" \
       "$BASE_DIR/config/DistributedTransactionConfig.java.backup.$TIMESTAMP"
    print_info "✓ Backed up DistributedTransactionConfig.java"
fi

if [ -f "$BASE_DIR/RestaurantApplication.java" ]; then
    cp "$BASE_DIR/RestaurantApplication.java" \
       "$BASE_DIR/RestaurantApplication.java.backup.$TIMESTAMP"
    print_info "✓ Backed up RestaurantApplication.java"
fi

if [ -f "$RESOURCES_DIR/application.properties" ]; then
    cp "$RESOURCES_DIR/application.properties" \
       "$RESOURCES_DIR/application.properties.backup.$TIMESTAMP"
    print_info "✓ Backed up application.properties"
fi

# Step 3: Check if analytics database exists
print_step "Step 3: Checking analytics database..."

if docker ps | grep -q mysql; then
    print_info "MySQL container is running. Checking databases..."

    DATABASES=$(docker exec mysql mysql -u restaurant_user -prestaurant_pass -e "SHOW DATABASES;" 2>&1)

    if echo "$DATABASES" | grep -q "analytics"; then
        print_info "✓ Analytics database exists"
    else
        print_warning "Analytics database not found!"
        print_info "Creating analytics database..."

        docker exec mysql mysql -u root -proot << 'EOF'
CREATE DATABASE IF NOT EXISTS analytics CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON analytics.* TO 'restaurant_user'@'%';
FLUSH PRIVILEGES;
EOF

        if [ $? -eq 0 ]; then
            print_info "✓ Analytics database created"
        else
            print_error "Failed to create analytics database"
            exit 1
        fi
    fi
else
    print_warning "MySQL container not running. Will create database on first start."
fi

# Step 4: Check required files
print_step "Step 4: Checking required files..."

MISSING_FILES=()

if [ ! -f "$BASE_DIR/analytics/model/SalesReport.java" ]; then
    MISSING_FILES+=("SalesReport.java")
fi

if [ ! -f "$BASE_DIR/analytics/repository/SalesReportRepository.java" ]; then
    MISSING_FILES+=("SalesReportRepository.java")
fi

if [ ! -f "$BASE_DIR/helper/SagaTransaction.java" ]; then
    MISSING_FILES+=("SagaTransaction.java")
fi

if [ ${#MISSING_FILES[@]} -gt 0 ]; then
    print_warning "Missing files detected:"
    for file in "${MISSING_FILES[@]}"; do
        print_warning "  - $file"
    done
    print_info "Please copy the required files from artifacts before continuing."
    read -p "Continue anyway? (yes/no): " continue_deploy
    if [ "$continue_deploy" != "yes" ]; then
        exit 1
    fi
fi

# Step 5: Verify application.properties
print_step "Step 5: Verifying application.properties..."

if grep -q "spring.datasource.analytics.jdbc-url" "$RESOURCES_DIR/application.properties"; then
    print_info "✓ Analytics datasource configured"
else
    print_error "Analytics datasource NOT configured in application.properties"
    print_info "Please update application.properties with analytics datasource config"
    exit 1
fi

# Step 6: Stop user-service
print_step "Step 6: Stopping user-service..."

docker-compose stop user-service
print_info "✓ User-service stopped"

# Step 7: Remove old image
print_step "Step 7: Removing old image..."

docker rmi user-service:latest 2>/dev/null || true
print_info "✓ Old image removed"

# Step 8: Build new image
print_step "Step 8: Building new image..."

print_info "This may take a few minutes..."
docker-compose build --no-cache user-service

if [ $? -ne 0 ]; then
    print_error "Build failed!"
    print_info "Check the error messages above"
    exit 1
fi

print_info "✓ Build completed successfully"

# Step 9: Start user-service
print_step "Step 9: Starting user-service..."

docker-compose up -d user-service

print_info "✓ User-service started"

# Step 10: Wait and monitor
print_step "Step 10: Waiting for service to start..."

print_info "Monitoring logs for 90 seconds..."

for i in {1..90}; do
    if ! docker ps | grep -q user-service; then
        print_error "Container stopped unexpectedly!"

        EXIT_CODE=$(docker inspect user-service --format='{{.State.ExitCode}}' 2>/dev/null)
        print_error "Exit code: $EXIT_CODE"

        if [ "$EXIT_CODE" = "137" ]; then
            print_error "OOM (Out of Memory) Kill detected!"
            print_info "Solutions:"
            print_info "  1. Increase Docker memory to 8GB+"
            print_info "  2. Reduce JAVA_OPTS: -Xmx768m -Xms384m"
        fi

        print_info "Last 50 lines of logs:"
        docker logs --tail=50 user-service
        exit 1
    fi

    # Check if started successfully
    if docker logs user-service 2>&1 | grep -q "Started RestaurantApplication"; then
        print_info "✓ Service started successfully!"
        break
    fi

    echo -n "."
    sleep 1
done

echo ""

# Step 11: Verify EntityManagerFactories
print_step "Step 11: Verifying EntityManagerFactories..."

sleep 5

LOGS=$(docker logs user-service 2>&1)

if echo "$LOGS" | grep -q "restaurantEntityManagerFactory"; then
    print_info "✓ Restaurant EntityManagerFactory initialized"
else
    print_warning "⚠ Restaurant EntityManagerFactory not found in logs"
fi

if echo "$LOGS" | grep -q "analyticsEntityManagerFactory"; then
    print_info "✓ Analytics EntityManagerFactory initialized"
else
    print_warning "⚠ Analytics EntityManagerFactory not found in logs"
fi

# Step 12: Check for errors
print_step "Step 12: Checking for errors..."

if echo "$LOGS" | grep -qi "error\|exception\|failed"; then
    print_warning "Errors detected in logs. Showing last 100 lines:"
    docker logs --tail=100 user-service
else
    print_info "✓ No obvious errors found"
fi

# Step 13: Test database connections
print_step "Step 13: Testing database connections..."

print_info "Testing restaurant DB..."
docker exec mysql mysql -u restaurant_user -prestaurant_pass -e "USE restaurant; SELECT 'OK' as Status;" 2>/dev/null
if [ $? -eq 0 ]; then
    print_info "✓ Restaurant DB accessible"
else
    print_error "✗ Cannot access restaurant DB"
fi

print_info "Testing analytics DB..."
docker exec mysql mysql -u restaurant_user -prestaurant_pass -e "USE analytics; SELECT 'OK' as Status;" 2>/dev/null
if [ $? -eq 0 ]; then
    print_info "✓ Analytics DB accessible"
else
    print_error "✗ Cannot access analytics DB"
fi

# Step 14: Show status
print_step "Step 14: Final status check..."

echo ""
echo "=========================================="
echo "Deployment Status"
echo "=========================================="

# Service status
if docker ps | grep -q user-service; then
    print_info "✓ user-service: RUNNING"

    # Check health endpoint
    sleep 5
    HEALTH=$(curl -s http://localhost:8081/actuator/health 2>/dev/null)
    if echo "$HEALTH" | grep -q '"status":"UP"'; then
        print_info "✓ Health check: PASSED"
    else
        print_warning "⚠ Health check: Not ready yet"
    fi
else
    print_error "✗ user-service: STOPPED"
fi

# Database status
if docker ps | grep -q mysql; then
    print_info "✓ MySQL: RUNNING"
else
    print_error "✗ MySQL: STOPPED"
fi

if docker ps | grep -q mongodb; then
    print_info "✓ MongoDB: RUNNING"
else
    print_warning "⚠ MongoDB: STOPPED"
fi

# Memory usage
echo ""
print_info "Memory usage:"
docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}" | grep -E "NAME|user-service|mysql"

echo ""
echo "=========================================="
echo "Deployment Complete!"
echo "=========================================="
echo ""
print_info "Useful commands:"
print_info "  - View logs: docker logs -f user-service"
print_info "  - Check health: curl http://localhost:8081/actuator/health"
print_info "  - Check memory: docker stats user-service"
print_info "  - Restart: docker-compose restart user-service"
print_info "  - Shell into MySQL: docker exec -it mysql mysql -u restaurant_user -p"
echo ""