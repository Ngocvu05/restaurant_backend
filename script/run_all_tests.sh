#!/bin/bash

# ============================================
# Transaction Demo API - Automated Test Runner
# ============================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
BOOKING_ID=1
DISH_ID=1
TABLE_ID=1
USER_ID=2

# Counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Functions
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_test() {
    echo -e "${YELLOW}Testing:${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓ PASSED${NC} - $1\n"
    ((PASSED_TESTS++))
    ((TOTAL_TESTS++))
}

print_failure() {
    echo -e "${RED}✗ FAILED${NC} - $1"
    echo -e "${RED}Response:${NC} $2\n"
    ((FAILED_TESTS++))
    ((TOTAL_TESTS++))
}

test_endpoint() {
    local method=$1
    local endpoint=$2
    local description=$3
    local data=$4

    print_test "$description"

    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ]; then
        print_success "$description (HTTP $http_code)"
    else
        print_failure "$description (HTTP $http_code)" "$body"
    fi
}

# ============================================
# Start Testing
# ============================================

echo -e "${GREEN}"
echo "╔═══════════════════════════════════════════════╗"
echo "║   Transaction Demo API - Test Runner v1.0    ║"
echo "╔═══════════════════════════════════════════════╗"
echo -e "${NC}\n"

# Check if server is running
echo -e "${YELLOW}Checking if server is running...${NC}"
if ! curl -s "$BASE_URL" > /dev/null; then
    echo -e "${RED}Error: Server is not running at $BASE_URL${NC}"
    echo -e "${YELLOW}Please start the application first: mvn spring-boot:run${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Server is running${NC}\n"

# ============================================
# CATEGORY 1: ISOLATION LEVELS
# ============================================
print_header "CATEGORY 1: ISOLATION LEVELS (5 tests)"

test_endpoint "GET" \
    "/api/transactions/isolation/read-committed/$BOOKING_ID" \
    "1.1 READ_COMMITTED Isolation"

test_endpoint "GET" \
    "/api/transactions/isolation/repeatable-read?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59" \
    "1.2 REPEATABLE_READ Isolation"

test_endpoint "POST" \
    "/api/transactions/isolation/serializable" \
    "1.3 SERIALIZABLE Isolation" \
    '{"tableId":1,"userId":2,"bookingTime":"2025-10-30T19:00:00","numberOfGuests":4}'

test_endpoint "PUT" \
    "/api/transactions/isolation/default/$BOOKING_ID?status=CONFIRMED" \
    "1.4 DEFAULT Isolation"

test_endpoint "POST" \
    "/api/transactions/isolation/demo/$BOOKING_ID" \
    "1.5 Demonstrate All Isolation Levels"

# ============================================
# CATEGORY 2: PROPAGATION
# ============================================
print_header "CATEGORY 2: TRANSACTION PROPAGATION (4 tests)"

test_endpoint "PUT" \
    "/api/transactions/propagation/required/$BOOKING_ID?status=CONFIRMED" \
    "2.1 REQUIRED Propagation"

test_endpoint "POST" \
    "/api/transactions/propagation/requires-new/$BOOKING_ID?action=TEST_ACTION" \
    "2.2 REQUIRES_NEW Propagation"

test_endpoint "POST" \
    "/api/transactions/propagation/complex/$BOOKING_ID" \
    "2.3 Complex Operation (Multiple Propagations)"

test_endpoint "POST" \
    "/api/transactions/propagation/rollback-test/$BOOKING_ID" \
    "2.4 Rollback Scenario"

# ============================================
# CATEGORY 3: LOCKING MECHANISMS
# ============================================
print_header "CATEGORY 3: LOCKING MECHANISMS (6 tests)"

test_endpoint "PUT" \
    "/api/transactions/locking/optimistic/dish/$DISH_ID?newPrice=55000" \
    "3.1 Optimistic Locking"

test_endpoint "POST" \
    "/api/transactions/locking/optimistic/conflict-test/$DISH_ID" \
    "3.2 Optimistic Locking Conflict"

test_endpoint "POST" \
    "/api/transactions/locking/pessimistic/book-table/$TABLE_ID" \
    "3.3 Pessimistic Locking"

test_endpoint "POST" \
    "/api/transactions/locking/pessimistic/deadlock-test" \
    "3.4 Deadlock Simulation"

test_endpoint "PUT" \
    "/api/transactions/locking/hybrid/dish/$DISH_ID" \
    "3.5 Hybrid Locking"

test_endpoint "POST" \
    "/api/transactions/locking/timeout/$TABLE_ID" \
    "3.6 Lock Timeout"

# ============================================
# CATEGORY 4: DEADLOCK PREVENTION
# ============================================
print_header "CATEGORY 4: DEADLOCK PREVENTION (5 tests)"

test_endpoint "POST" \
    "/api/transactions/deadlock/lock-ordering" \
    "4.1 Lock Ordering Strategy" \
    '[1,2,3]'

test_endpoint "POST" \
    "/api/transactions/deadlock/retry" \
    "4.2 Retry Strategy" \
    '{"tableId":1,"userId":2,"bookingTime":"2025-10-31T20:00:00","numberOfGuests":6}'

test_endpoint "POST" \
    "/api/transactions/deadlock/short-transaction/$TABLE_ID" \
    "4.3 Short Transaction Strategy"

test_endpoint "POST" \
    "/api/transactions/deadlock/process-order?bookingId=$BOOKING_ID" \
    "4.4 Process Order with Deadlock Handling" \
    '[1,2,3,4,5]'

test_endpoint "POST" \
    "/api/transactions/deadlock/simulate" \
    "4.5 Simulate Deadlock"

# ============================================
# CATEGORY 5: DISTRIBUTED TRANSACTIONS
# ============================================
print_header "CATEGORY 5: DISTRIBUTED TRANSACTIONS (6 tests)"

test_endpoint "POST" \
    "/api/transactions/distributed/sequential/$BOOKING_ID" \
    "5.1 Sequential Transactions"

test_endpoint "POST" \
    "/api/transactions/distributed/saga/$BOOKING_ID" \
    "5.2 Saga Pattern (Recommended)"

test_endpoint "POST" \
    "/api/transactions/distributed/2pc/$BOOKING_ID" \
    "5.3 Two-Phase Commit"

test_endpoint "POST" \
    "/api/transactions/distributed/eventual/$BOOKING_ID" \
    "5.4 Eventual Consistency"

test_endpoint "POST" \
    "/api/transactions/distributed/manual/$BOOKING_ID" \
    "5.5 Manual Transaction Management"

test_endpoint "POST" \
    "/api/transactions/distributed/demo/$BOOKING_ID" \
    "5.6 Demonstrate All Approaches"

# ============================================
# SUMMARY
# ============================================
print_header "TEST SUMMARY"

echo -e "${BLUE}Total Tests:${NC} $TOTAL_TESTS"
echo -e "${GREEN}Passed:${NC} $PASSED_TESTS"
echo -e "${RED}Failed:${NC} $FAILED_TESTS"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}╔═══════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  ✓ ALL TESTS PASSED SUCCESSFULLY!   ║${NC}"
    echo -e "${GREEN}╚═══════════════════════════════════════╝${NC}\n"
    exit 0
else
    success_rate=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo -e "\n${YELLOW}╔═══════════════════════════════════════╗${NC}"
    echo -e "${YELLOW}║  Success Rate: ${success_rate}%                  ║${NC}"
    echo -e "${YELLOW}║  Review failed tests above           ║${NC}"
    echo -e "${YELLOW}╚═══════════════════════════════════════╝${NC}\n"
    exit 1
fi