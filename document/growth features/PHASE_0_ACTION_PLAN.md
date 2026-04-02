# 🎯 PHASE 0 ACTION PLAN - RESTAURANT BACKEND

## 📊 CURRENT PROJECT ANALYSIS

Based on your structure:
```
restaurant_backend/
├─ api-gateway/          ✅ Entry point - HIGH priority
├─ backend-service/      ✅ Core business - HIGHEST priority  
├─ chat-service/         🟡 Feature service - MEDIUM priority
├─ discovery-service/    ✅ Infrastructure - HIGH priority
├─ search-service/       🟡 Feature service - MEDIUM priority
├─ elk/                  🟢 Already have! - Enhance
├─ monitoring/           🟢 Already have! - Enhance
├─ init/                 ✅ Database setup
├─ keys/                 ✅ Security certs
└─ docker-compose.yml    ✅ Orchestration
```

**Good news:** You already have ELK and monitoring! Ahead of the game! 🎉

---

## 🚀 PHASE 0 RECOMMENDED APPROACH

### Strategy: **Start with BACKEND-SERVICE (Core)**

**Why backend-service first?**
1. ✅ Contains core business logic (Users, Orders, Restaurants, Menu)
2. ✅ Other services depend on it
3. ✅ Largest impact if it fails
4. ✅ Most complex = best learning
5. ✅ Template for other services

**Analogy:** Fix the heart before the limbs!

---

## 📋 PHASE 0 WEEK-BY-WEEK BREAKDOWN

### **WEEK 1: DOCUMENT & UNDERSTAND BACKEND-SERVICE**

Focus: Map out what you have

---

#### **Day 1-2: Architecture Documentation**

**Create:** `docs/backend-service-architecture.md`

```markdown
# Backend Service Architecture

## Responsibilities
- [ ] User management (CRUD, auth)
- [ ] Restaurant management
- [ ] Menu management  
- [ ] Order management
- [ ] Review/Rating system
- [ ] What else?

## Current Database Schema
- [ ] Document all tables
- [ ] Document relationships
- [ ] Identify N+1 query risks

## External Dependencies
- [ ] MySQL connection
- [ ] Redis connection
- [ ] RabbitMQ/Kafka?
- [ ] Other services called?

## API Endpoints
- [ ] List all REST endpoints
- [ ] Document request/response
- [ ] Identify public vs internal

## Current Performance Baselines
- [ ] How many users?
- [ ] How many orders/day?
- [ ] Average response time?
- [ ] Database query time?
```

**Action Items:**
```bash
# 1. Generate API documentation
cd backend-service
mvn spring-boot:run
# Access Swagger: http://localhost:8080/swagger-ui.html

# 2. Export to docs/
cp target/swagger.json ../docs/backend-service-api.json

# 3. Database schema
mysqldump -u root -p restaurant --no-data > ../docs/schema.sql

# 4. Count tables and relationships
mysql -u root -p -e "
SELECT TABLE_NAME, TABLE_ROWS 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'restaurant'
ORDER BY TABLE_ROWS DESC;
" > ../docs/table-stats.txt
```

---

#### **Day 3-4: Data Flow Documentation**

**Create:** `docs/critical-flows.md`

```markdown
# Critical Business Flows

## 1. User Registration Flow
```
Frontend → API Gateway → Backend-Service
                           ↓
                        Validate
                           ↓
                        Save User (MySQL)
                           ↓
                        Create JWT
                           ↓
                        Return Token
```

## 2. Create Order Flow (MOST CRITICAL!)
```
User → API Gateway → Backend-Service
                        ↓
                     Validate User
                        ↓
                     Check Menu (cache?)
                        ↓
                     Check Inventory?
                        ↓
                     Calculate Total
                        ↓
                     Create Order (MySQL)
                        ↓
                     Publish Event? (Kafka/RabbitMQ?)
                        ↓
                     Return Order
```

## 3. Get Restaurant List Flow
```
User → API Gateway → Backend-Service
                        ↓
                     Check Cache? (Redis)
                        ↓ (miss)
                     Query DB (MySQL)
                        ↓
                     Cache Result
                        ↓
                     Return List
```
```

**Action Items:**
```bash
# Trace a real request through logs
docker-compose logs -f backend-service | grep "Create Order"

# Check if caching exists
grep -r "@Cacheable" backend-service/src/

# Check if events are published
grep -r "RabbitTemplate\|KafkaTemplate" backend-service/src/
```

---

#### **Day 5-7: Capacity Planning Document**

**Create:** `docs/capacity-planning.md`

```markdown
# Capacity Planning - Backend Service

## Current State (Baseline)

### Traffic
- Total users: ___
- Daily active users (DAU): ___
- Orders per day: ___
- Peak orders per hour: ___
- Average order size: ___ items

### Database
- Total tables: ___
- Largest table: ___ (rows: ___)
- Total DB size: ___ MB
- Current connections: ___
- Max connections: ___

### Performance
- P50 latency: ___ ms
- P95 latency: ___ ms
- P99 latency: ___ ms
- Error rate: ___ %

### Resource Usage
- CPU: ___ %
- Memory: ___ MB / ___ MB
- Disk: ___ GB / ___ GB
- Network: ___ MB/s

## Target State (100K requests/day)

### Projections
- DAU: 10,000 (assuming 10 requests/user/day)
- Orders/day: 50,000
- Peak RPS: ~100 (assuming 6 hour peak window)
- Peak DB connections needed: ___

### Bottleneck Analysis
- [ ] Database will handle 100 RPS? (Test needed)
- [ ] Current connection pool size: ___
- [ ] Redis can handle load?
- [ ] Network bandwidth sufficient?

### Scaling Requirements
- [ ] Read replicas needed? (YES if >50 RPS)
- [ ] Horizontal scaling pods? (YES if CPU >70%)
- [ ] Redis cluster? (NO if <10GB data)
- [ ] Database sharding? (Evaluate at Phase 3)
```

**Action Items:**
```bash
# 1. Get current metrics from monitoring/
# If you have Prometheus/Grafana:
curl http://localhost:9090/api/v1/query?query=rate(http_requests_total[5m])

# 2. Database size
docker exec -it mysql mysql -u root -p -e "
SELECT 
  table_schema AS 'Database',
  ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'restaurant'
GROUP BY table_schema;
"

# 3. Connection pool config
grep -r "spring.datasource" backend-service/src/main/resources/

# 4. Current load test (if you have JMeter/k6)
# Install k6: https://k6.io/
k6 run -u 10 -d 30s load-test.js
```

**Load test script example:**
```javascript
// load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '1m', target: 10 },  // Ramp up to 10 users
    { duration: '3m', target: 10 },  // Stay at 10 users
    { duration: '1m', target: 0 },   // Ramp down
  ],
};

export default function () {
  let res = http.get('http://localhost:8080/api/v1/restaurants');
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
  sleep(1);
}
```

---

### **WEEK 2: SECURITY & DATA CONSISTENCY**

Focus: Foundation for all future phases

---

#### **Day 1-2: Security Baseline**

**Create:** `docs/security-baseline.md`

```markdown
# Security Baseline - Backend Service

## Current Authentication

### JWT Implementation
- [ ] Algorithm: HS256 or RS256?
- [ ] Secret management: Environment variable or Vault?
- [ ] Token expiry: ___ minutes
- [ ] Refresh token: YES / NO
- [ ] Token rotation: YES / NO

**Action:**
```bash
# Check JWT config
grep -r "jwt" backend-service/src/main/resources/application.yml

# Check secret in .env or docker-compose
grep JWT docker-compose.yml
```

**Issues to fix:**
- [ ] If HS256 + secret in .env → Migrate to RS256 (you already did this! ✅)
- [ ] If no refresh token → Add in Phase 4
- [ ] If no token rotation → Add in Phase 4

---

### API Security Headers
```bash
# Test your API Gateway
curl -I http://localhost:8080/api/v1/restaurants

# Should have:
# X-Content-Type-Options: nosniff
# X-Frame-Options: DENY
# Strict-Transport-Security: max-age=31536000
```

**Action:**
```java
// Add to API Gateway SecurityConfig
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http.headers()
        .contentTypeOptions()
        .xssProtection()
        .frameOptions().deny()
        .httpStrictTransportSecurity();
    return http.build();
}
```

---

### Service-to-Service Authentication

**Current state:**
- [ ] Backend calls Search service: How authenticated?
- [ ] Backend calls Chat service: How authenticated?
- [ ] Gateway calls Backend: JWT forwarding?

**Document:**
```
Service Communication Matrix:

API Gateway → Backend:    JWT (from user)
API Gateway → Search:     JWT (from user)
API Gateway → Chat:       JWT (from user)

Backend → Search:         ??? (System token needed!)
Backend → Chat:           ??? (System token needed!)
Chat → Backend:           ??? (System token needed!)
```

**Action required:**
- [ ] Implement system user + service tokens
- [ ] Or use mTLS for service-to-service

---

### Secrets Management

**Current state:**
```bash
# Check how secrets are stored
cat docker-compose.yml | grep -i password
cat backend-service/src/main/resources/application.yml | grep -i password
```

**Issues:**
- [ ] Passwords in git? → Move to .env
- [ ] .env in git? → Add to .gitignore
- [ ] Production secrets? → Use Vault (Phase 4)

**Quick fix NOW:**
```bash
# 1. Create .env file
cat > .env << EOF
MYSQL_ROOT_PASSWORD=strong_password_here
REDIS_PASSWORD=another_strong_password
JWT_SECRET=your_jwt_secret_base64
EOF

# 2. Add to .gitignore
echo ".env" >> .gitignore

# 3. Update docker-compose.yml
# Change:
# MYSQL_ROOT_PASSWORD: admin
# To:
# MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
```

---

### Input Validation

**Check:**
```bash
# Do you validate input?
grep -r "@Valid\|@Validated" backend-service/src/

# Do you have DTO validation?
grep -r "@NotNull\|@NotBlank\|@Size" backend-service/src/
```

**Example:**
```java
// Should have:
@PostMapping("/orders")
public Order createOrder(@Valid @RequestBody CreateOrderDTO dto) {
    // ...
}

// DTO should have:
public class CreateOrderDTO {
    @NotNull(message = "User ID required")
    private Long userId;
    
    @NotEmpty(message = "Items required")
    @Size(min = 1, max = 50, message = "1-50 items allowed")
    private List<OrderItemDTO> items;
    
    @Min(value = 0, message = "Total must be positive")
    private BigDecimal total;
}
```

**If missing → Add validation annotations**

---

#### **Day 3-4: Data Consistency Map**

**Create:** `docs/data-consistency.md`

```markdown
# Data Consistency Requirements

## Strong Consistency (ACID required)

### Orders Table
- **Why:** Money involved, audit required
- **Consistency:** MUST be immediately consistent
- **Transactions:** Required (atomicity)
- **Example:** Order total = sum(item prices)

### Payments Table  
- **Why:** Financial data, regulatory compliance
- **Consistency:** MUST be immediately consistent
- **Transactions:** Two-phase commit with payment gateway

### Inventory (if exists)
- **Why:** Prevent overselling
- **Consistency:** Strong (optimistic locking)
- **Race condition:** Multiple users buying last item

**Action:**
```sql
-- Add version column for optimistic locking
ALTER TABLE inventory 
ADD COLUMN version INT DEFAULT 0;

-- Update query becomes:
UPDATE inventory 
SET quantity = quantity - 1, version = version + 1
WHERE id = ? AND version = ?;

-- If 0 rows affected → Conflict, retry
```

---

## Eventual Consistency (Can tolerate delay)

### Restaurant Ratings
- **Why:** Aggregate data, not critical
- **Consistency:** Can be 5-10 minutes stale
- **Update frequency:** After each review
- **How:** Background job or event-driven

### Menu Cache (Redis)
- **Why:** Read-heavy, changes infrequent
- **Consistency:** Can be 5 minutes stale
- **TTL:** 5 minutes
- **Invalidation:** On menu update event

### Search Index (Elasticsearch)
- **Why:** Full-text search, approximate results OK
- **Consistency:** Can be 1-5 minutes stale
- **Sync mechanism:** CDC (Change Data Capture) or events

### Analytics / Dashboards
- **Why:** Business intelligence, historical data
- **Consistency:** Can be 1 hour - 1 day stale
- **Update:** Batch job overnight

---

## Consistency Levels Matrix

| Data Type | Consistency | Max Delay | Method |
|-----------|-------------|-----------|--------|
| Orders | Strong | 0s | ACID transaction |
| Payments | Strong | 0s | 2PC or Saga |
| Inventory | Strong | 0s | Optimistic lock |
| User Profile | Strong | 0s | Single DB write |
| Ratings | Eventual | 5 min | Event → Update |
| Menu Cache | Eventual | 5 min | TTL + Event invalidate |
| Search Index | Eventual | 5 min | CDC or Event |
| Analytics | Eventual | 1 day | Batch ETL |

---

## Conflict Resolution Strategy

### Scenario: User updates profile, multiple devices

**Problem:**
```
Device A: Update name to "John" (t=1)
Device B: Update name to "Johnny" (t=2)

Which wins?
```

**Strategy:**
- [ ] Last-write-wins (timestamp)
- [ ] Version-based (optimistic locking) ✅ Recommended
- [ ] Application merge (complex)

**Implementation:**
```java
@Entity
public class User {
    @Id
    private Long id;
    
    @Version  // ✅ Hibernate optimistic locking
    private Integer version;
    
    private String name;
}

// Update will fail if version mismatch → Retry with latest
```

---

### Scenario: Distributed cache inconsistency

**Problem:**
```
User updates restaurant info
→ DB updated
→ Cache NOT invalidated
→ Stale data served
```

**Solutions:**

**Option A: Write-through**
```java
@Transactional
public void updateRestaurant(Restaurant r) {
    repository.save(r);  // Write to DB
    cache.put(r.getId(), r);  // Write to cache
}
```

**Option B: Event-driven** ✅ Better for microservices
```java
@Transactional
public void updateRestaurant(Restaurant r) {
    repository.save(r);
    eventPublisher.publish(new RestaurantUpdatedEvent(r.getId()));
}

// Listener invalidates cache
@EventListener
public void onRestaurantUpdated(RestaurantUpdatedEvent e) {
    cache.evict(e.getRestaurantId());
}
```
```

---

#### **Day 5-7: Database Optimization Audit**

**Create:** `docs/database-optimization.md`

**Step 1: Identify Slow Queries**

```sql
-- Enable slow query log in MySQL
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1; -- Queries >1s

-- After running your app for 1 hour:
-- Check slow query log
SELECT * FROM mysql.slow_log 
ORDER BY query_time DESC 
LIMIT 20;
```

**Step 2: Missing Indexes Audit**

```sql
-- Find tables without indexes
SELECT 
    t.TABLE_NAME,
    t.TABLE_ROWS,
    COUNT(s.INDEX_NAME) as index_count
FROM information_schema.TABLES t
LEFT JOIN information_schema.STATISTICS s 
    ON t.TABLE_NAME = s.TABLE_NAME 
    AND t.TABLE_SCHEMA = s.TABLE_SCHEMA
WHERE t.TABLE_SCHEMA = 'restaurant'
GROUP BY t.TABLE_NAME, t.TABLE_ROWS
HAVING index_count < 2  -- Only primary key
ORDER BY t.TABLE_ROWS DESC;
```

**Step 3: Analyze Common Queries**

```java
// Find all repository methods
grep -r "findBy" backend-service/src/main/java/**/repository/

// Example findings:
// ❌ findByUsernameAndStatus() - No index on (username, status)
// ❌ findByRestaurantIdAndCreatedAtBetween() - No index on (restaurant_id, created_at)
// ✅ findById() - Has primary key index
```

**Step 4: Add Missing Indexes**

```sql
-- Based on your queries, add indexes:

-- For user login
CREATE INDEX idx_users_username ON users(username);

-- For order queries
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_status ON orders(status);

-- Composite index for common query
CREATE INDEX idx_orders_user_status 
ON orders(user_id, status, created_at);

-- For restaurant search
CREATE INDEX idx_restaurants_name ON restaurants(name);
CREATE INDEX idx_restaurants_location ON restaurants(city, area);
```

**Step 5: Connection Pool Tuning**

```yaml
# backend-service/src/main/resources/application.yml

spring:
  datasource:
    hikari:
      # Current config?
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      
      # ✅ Recommended for 100 RPS:
      maximum-pool-size: 20  # Rule: (core_count * 2) + effective_spindle_count
      minimum-idle: 10
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
      
      # Monitoring
      leak-detection-threshold: 60000
```

**Calculation:**
```
Formula: connections = ((core_count * 2) + effective_spindle_count)

Your server: 4 CPU cores, SSD (assume 1 spindle)
connections = (4 * 2) + 1 = 9

Add buffer: 9 * 1.5 = ~15
Recommended: 20 (allows some headroom)
```

---

### **DELIVERABLES - END OF WEEK 2**

You should have:

✅ **Documentation:**
```
docs/
├── backend-service-architecture.md
├── critical-flows.md
├── capacity-planning.md
├── security-baseline.md
├── data-consistency.md
└── database-optimization.md
```

✅ **Artifacts:**
```
docs/
├── schema.sql (current DB schema)
├── table-stats.txt (table sizes)
├── backend-service-api.json (Swagger export)
└── slow-queries.log (performance issues)
```

✅ **Completed Audits:**
- [ ] Security vulnerabilities identified
- [ ] Missing indexes documented
- [ ] Slow queries identified
- [ ] Data consistency requirements mapped
- [ ] Capacity constraints known

---

## 🎯 AFTER PHASE 0 - WHAT NEXT?

### **You'll have answers to:**

1. ✅ Which tables need read replicas? (Large, read-heavy tables)
2. ✅ Where to add caching? (Slow queries identified)
3. ✅ What data can be eventual? (Consistency map done)
4. ✅ Security gaps to fix? (Baseline audit done)
5. ✅ Can DB handle 100K requests? (Load test done)

### **Phase 0.5: Quick Wins (Week 3)**

Before moving to Phase 1, implement quick improvements:

**Database Quick Wins:**
```sql
-- Add missing indexes (from audit)
-- Estimated impact: 50% faster queries

-- Tune connection pool
-- Estimated impact: 20% better concurrency

-- Add read replica
-- Estimated impact: 2x read capacity
```

**Security Quick Wins:**
```java
// Add input validation
// Add security headers
// Move secrets to .env
// Estimated impact: Close 80% of common vulnerabilities
```

**Observability Quick Wins:**
```yaml
# Enhance existing ELK
# Add structured logging
# Setup basic dashboards
# Estimated impact: 10x faster debugging
```

---

## 📊 APPLY TO OTHER SERVICES (WEEK 4+)

### **Service Priority Order:**

**1. Backend-Service** ✅ (Week 1-3)
- Core business logic
- Highest complexity
- Template for others

**2. API Gateway** (Week 4)
- Entry point for all requests
- Critical for security
- Use: Same security audit template

**3. Search-Service** (Week 5)  
- Feature service
- Elasticsearch-specific considerations
- Use: Same caching strategy

**4. Chat-Service** (Week 6)
- Feature service  
- Real-time requirements
- Use: Same event-driven patterns

**5. Discovery-Service** (Week 7)
- Infrastructure
- Already working, minimal changes
- Use: Same monitoring setup

---

### **Template Approach:**

For each service, repeat:

```
Day 1-2: Architecture doc (copy template from backend-service)
Day 3-4: Flow diagrams (customize for service)
Day 5-7: Security + Data audit (use same checklist)
```

**Time per service: ~1 week**

**Total for 5 services: ~7 weeks** (overlapping with Phase 1)

---

## 🚀 FINAL PHASE 0 CHECKLIST

Before moving to Phase 1, verify:

### **Backend-Service:**
- [ ] Architecture documented
- [ ] Critical flows mapped
- [ ] Capacity plan created
- [ ] Security baseline audited
- [ ] Data consistency mapped
- [ ] Database optimized
- [ ] Indexes added
- [ ] Slow queries identified
- [ ] Load test run

### **Other Services:**
- [ ] API Gateway architecture documented
- [ ] Discovery-Service verified healthy
- [ ] Search-Service dependencies mapped
- [ ] Chat-Service real-time requirements understood

### **Infrastructure:**
- [ ] ELK logging verified working
- [ ] Monitoring dashboards exist
- [ ] Prometheus metrics collecting
- [ ] Secrets in .env (not git)
- [ ] Docker-compose resource limits set

---

## 💡 PRO TIPS

**1. Don't skip documentation!**
```
Temptation: "I know my code, skip docs"
Reality: In 2 months, you'll forget why you did X
Result: Waste days re-learning

Investment: 2 days documenting
Savings: Weeks of confusion later
```

**2. Measure BEFORE optimizing**
```
Wrong: "Let's add caching everywhere!"
Right: "Slow query log shows orders query is 2s → cache that"

Without measurement = random guessing
With measurement = targeted improvement
```

**3. Security CANNOT be added later**
```
Adding security to existing system = Rewriting everything
Building with security from start = Just following checklist

Phase 0 security audit = 2 days
Fixing security breach later = Weeks + reputation damage
```

**4. Small wins build momentum**
```
Week 1: "Ugh, so much documentation"
Week 2: "Oh, found 5 slow queries to fix!"
Week 3: "Added indexes, 2x faster! 🎉"

Documentation → Discovery → Quick wins → Motivation
```

---

## 🎯 EXPECTED OUTCOMES

### **After Phase 0 (3-4 weeks):**

**Knowledge:**
- ✅ Deep understanding of your system
- ✅ Know exactly where bottlenecks are
- ✅ Clear roadmap for improvements
- ✅ Confidence to scale to 100K requests

**Artifacts:**
- ✅ 6+ architecture documents
- ✅ Database schema exported
- ✅ API documentation generated
- ✅ Load test baselines
- ✅ Security audit report

**Improvements:**
- ✅ 50% faster queries (from indexes)
- ✅ 80% security vulnerabilities closed
- ✅ 2x database capacity (read replicas)
- ✅ Monitoring dashboards functional

**Mental State:**
- ✅ From "I think my system works" 
- ✅ To "I KNOW my system, measured and documented"

---

## ❓ QUESTIONS?

**Q: Can I skip Phase 0 and jump to coding Phase 1?**
A: You CAN, but you'll waste weeks debugging issues that Phase 0 would have revealed. Trust me, I've seen this 100 times. 😅

**Q: 3-4 weeks feels long for "just documentation"**
A: Phase 0 is NOT just docs. You're:
- Running load tests
- Optimizing database  
- Fixing security
- Adding indexes
- Setting up monitoring

These are REAL improvements!

**Q: Do I need to document ALL services in Phase 0?**
A: No! Start with backend-service (Week 1-3).
Others can be done in parallel with Phase 1-2 (Week 4-7).

**Q: What if I find major issues in Phase 0?**
A: GOOD! Better to find now than in Phase 4 when system is complex!
Fix critical issues before moving forward.

---

## 🎉 YOU'RE READY!

**Start tomorrow:**
1. Create `docs/` folder
2. Copy `backend-service-architecture.md` template
3. Spend 2 hours filling it out
4. You'll be surprised what you discover!

**Remember:**
> "Give me six hours to chop down a tree and I will spend the first four sharpening the axe." 
> - Abraham Lincoln

Phase 0 is sharpening your axe! 🪓

Good luck! Let me know what you discover! 🚀
