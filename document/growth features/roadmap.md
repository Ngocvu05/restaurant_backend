# 🗺️ Roadmap Phát Triển Project `restaurant_backend`

> Mục tiêu: học **backend nâng cao + distributed system**, hướng tới **Senior Backend / Tech Lead**
>
> Quy mô target: **~100.000 request** | Stack chính: **Spring Boot, MySQL, MongoDB, JWT/OAuth2**

---

## 🎯 Target State (Kết quả cuối)

Sau roadmap này, project sẽ:

* Là hệ thống **microservices hoàn chỉnh**
* Áp dụng **event-driven architecture**
* Có khả năng scale & chịu lỗi
* Có monitoring, security, CI/CD như production

---

# 🧱 PHASE 0 – FOUNDATION & SYSTEM THINKING

**Thời gian:** ~1 tuần
**Mục tiêu:** Hiểu rõ toàn bộ hệ thống trước khi mở rộng

### Việc cần làm

* Hoàn thiện documentation:

    * architecture.md
    * auth-flow.md
    * data-design.md
    * deployment.md
* Viết thêm:

    * failure-scenarios.md
* Vẽ flow:

    * Login flow
    * Create Order flow

### Kiến thức đạt được

* System thinking
* High-level architecture
* Trade-off khi thiết kế hệ thống

---

# 🚦 PHASE 1 – ORDER LIFECYCLE & EVENT-DRIVEN

**Thời gian:** 2–3 tuần
**Mục tiêu:** Bước vào distributed system thực sự

### Tính năng chính

* Order lifecycle:

  ```
  CREATED → PAID → PREPARING → READY → COMPLETED → CANCELLED
  ```

### Việc cần làm

* Thiết kế domain event:

    * OrderCreatedEvent
    * PaymentSucceededEvent
    * PaymentFailedEvent
    * OrderCompletedEvent
* Tích hợp message broker (Kafka/RabbitMQ)
* Publish / Consume event giữa các service
* Đảm bảo idempotency & retry

### Kiến thức đạt được

* Event-driven architecture
* Message broker
* Eventually consistency

---

# 🔄 PHASE 2 – DISTRIBUTED TRANSACTION (SAGA)

**Thời gian:** ~2 tuần
**Mục tiêu:** Xử lý business transaction xuyên service

### Flow

```
Create Order
 → Reserve Inventory
 → Process Payment
 → Confirm Order
```

### Failure Handling

```
Payment FAIL
 → Release Inventory
 → Cancel Order
```

### Việc cần làm

* Áp dụng Saga (Choreography)
* Thiết kế compensating action
* State machine cho order

### Kiến thức đạt được

* Saga Pattern
* Distributed consistency
* Business rollback

---

# ⚡ PHASE 3 – CACHE & PERFORMANCE

**Thời gian:** 1–2 tuần
**Mục tiêu:** Tối ưu hiệu năng & giảm tải hệ thống

### Việc cần làm

* Redis cache:

    * Restaurant
    * Menu
    * User profile
* Cache strategy:

    * Cache-aside
    * TTL & eviction
* Rate limiting tại Gateway

### Kiến thức đạt được

* Redis
* Cache stampede
* Distributed lock
* Performance tuning

---

# 🔐 PHASE 4 – AUTH & SECURITY NÂNG CAO

**Thời gian:** ~1 tuần
**Mục tiêu:** Auth đúng chuẩn production

### Việc cần làm

* Refresh token rotation
* JWT RS256
* Internal service authentication
* Token revoke & blacklist

### Kiến thức đạt được

* OAuth2 nâng cao
* Zero-trust architecture
* Security best practices

---

# 🔍 PHASE 5 – OBSERVABILITY & RESILIENCE

**Thời gian:** 1–2 tuần
**Mục tiêu:** Vận hành & debug hệ thống production

### Việc cần làm

* Monitoring:

    * Prometheus
    * Grafana
* Distributed tracing
* Circuit breaker (Resilience4j)
* Retry / fallback
* Simulate service failure

### Kiến thức đạt được

* Observability
* SRE mindset
* Failure handling

---

# 🚀 PHASE 6 – CI/CD & DEPLOYMENT

**Thời gian:** ~1 tuần
**Mục tiêu:** Tự động hóa build & deploy

### Việc cần làm

* CI/CD với GitHub Actions
* Build + test + docker
* Rolling update
* Health check & readiness probe

### Kiến thức đạt được

* CI/CD pipeline
* Container deployment
* Production readiness

---

## 🧠 Tổng kết

| Phase     | Level đạt được          |
| --------- | ----------------------- |
| Phase 0   | Mid backend             |
| Phase 1   | Strong mid              |
| Phase 2   | Senior backend          |
| Phase 3   | Senior production-ready |
| Phase 4–5 | Senior+ / Tech Lead     |
| Phase 6   | Production Engineer     |

---

📌 Ghi chú: Roadmap có thể điều chỉnh theo thời gian & mục tiêu học tập
