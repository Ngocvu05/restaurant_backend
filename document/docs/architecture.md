# 🏗️ System Architecture Overview

## 1. Mục tiêu tài liệu
Tài liệu này mô tả **high-level architecture** và **luồng xử lý chính** của hệ thống `restaurant_backend`.

Mục tiêu:
- Giúp dev mới onboard nhanh
- Hiểu rõ vai trò từng service
- Hiểu flow request / auth / data
- Là nền tảng để scale hệ thống lên **hàng trăm ngàn request**

---

## 2. Tổng quan kiến trúc

Hệ thống được xây dựng theo mô hình **Microservices Architecture**, sử dụng **Spring Boot + Spring Cloud**.

### Thành phần chính

```
Client (Web / Mobile)
        |
        v
API Gateway  ───────▶ Auth / JWT Validation
        |
        v
Service Discovery (Eureka)
        |
        v
┌────────────────────────────────────┐
│        Internal Microservices       │
│                                    │
│  - Backend Service (Core Domain)    │
│  - Chat Service                    │
│  - Search Service                  │
│                                    │
└────────────────────────────────────┘n        |
        v
Databases (MySQL / MongoDB)
```

---

## 3. Các service & trách nhiệm

### 3.1 API Gateway
**Vai trò:**
- Entry point duy nhất cho client
- Xác thực JWT / OAuth2
- Route request tới service tương ứng
- Rate limiting, logging

**Công nghệ:**
- Spring Cloud Gateway
- Spring Security

---

### 3.2 Discovery Service
**Vai trò:**
- Đăng ký & discover các service
- Giúp service giao tiếp không hard-code IP

**Công nghệ:**
- Netflix Eureka

---

### 3.3 Backend Service (Core)
**Vai trò:**
- Xử lý nghiệp vụ chính: user, restaurant, order, payment
- Quản lý transaction

**Database:**
- MySQL (transactional data)

---

### 3.4 Chat Service
**Vai trò:**
- Quản lý chat, message
- Lưu lịch sử hội thoại

**Database:**
- MongoDB (document-based)

---

### 3.5 Search Service
**Vai trò:**
- Full-text search restaurant / menu
- Index dữ liệu từ backend-service

**Công nghệ:**
- Elasticsearch

---

## 4. Authentication & Authorization Flow

### 4.1 JWT Authentication

```
Client
  |
  | 1. Login (username/password or OAuth2)
  v
Auth Logic (Backend / Auth Service)
  |
  | 2. Issue JWT
  v
Client stores JWT
  |
  | 3. Request with Authorization: Bearer <JWT>
  v
API Gateway
  |
  | 4. Validate JWT
  v
Target Service
```

### 4.2 OAuth2 Login (High-level)
- Client login qua Google / Facebook
- Backend verify token với provider
- Tạo user + OAuth2 link
- Issue JWT

---

## 5. Request Flow (Example: Create Order)

```
Client
  |
  | POST /orders
  v
API Gateway
  |  - Validate JWT
  |  - Route request
  v
Backend Service
  |  - Validate business rules
  |  - Start transaction
  |  - Persist order
  v
MySQL
```

---

## 6. Data Storage Strategy

### 6.1 MySQL
- User
- Order
- Payment
- OAuth2 links

**Lý do:**
- Strong consistency
- Transaction support

### 6.2 MongoDB
- Chat messages
- Logs (optional)

**Lý do:**
- Schema linh hoạt
- High write throughput

---

## 7. Observability & Monitoring

### Logging
- Centralized logging với ELK Stack
- Mỗi request có trace-id

### Monitoring
- Prometheus + Grafana
- Metrics: CPU, memory, request latency

---

## 8. Scalability & Reliability

### Scalability
- Stateless services
- Horizontal scaling bằng Docker / Kubernetes
- Read replica cho MySQL

### Reliability
- Circuit Breaker (Resilience4j)
- Retry + Timeout

---

## 9. Non-goals (Hiện tại)
- Event-driven architecture
- Saga pattern
- CQRS

(Có thể mở rộng trong tương lai)

---

## 10. Tương lai & cải tiến
- Tách Auth Service riêng
- Áp dụng Event-driven (Kafka)
- Distributed tracing (Jaeger)
- Zero-trust security

---

📌 **Tài liệu này được cập nhật khi kiến trúc thay đổi**

