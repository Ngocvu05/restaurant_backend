# Project Overview
## Muc tieu he thong
He thong quan ly nha hang theo kien truc microservices, tap trung vao:
- Quan ly nguoi dung, dat ban, mon an, thanh toan, danh gia.
- Tim kiem va dong bo du lieu tim kiem (Elasticsearch).
- Chat ho tro (websocket + AI) cho user/guest/admin.
- Giam sat va phan tich hoat dong (metrics, analytics, event store).

## Cong nghe su dung
- Java 21, Spring Boot (3.1.x -> 3.5.x), Spring Cloud (Eureka, Gateway).
- Spring Web / WebFlux, Spring Security, JWT (RS256).
- Spring Data JPA (MySQL), Spring Data MongoDB, Spring Data Redis.
- RabbitMQ (event bus + async messaging), WebSocket/STOMP.
- Elasticsearch 8.11, Caffeine cache, Resilience4j.
- MapStruct, Lombok, OpenAPI (springdoc).
- Docker Compose, Prometheus/Grafana, Logstash (monitoring/ELK).

# System Architecture
## So do kien truc (mo ta text)
Client
  -> API Gateway (JWT + rate limit + input validation + optional AES decrypt)
      -> User Service (backend-service)
         -> MySQL (restaurant, analytics)
         -> MongoDB (event store)
         -> Redis (cache, rate limit, locks)
         -> RabbitMQ (outbox -> events)
      -> Search Service
         -> Elasticsearch (indexes)
         -> MySQL (processed_events)
         -> Redis (cache)
         -> RabbitMQ (consume events)
      -> Chat Service
         -> MySQL (chat rooms/messages)
         -> Redis (cache)
         -> RabbitMQ (chat queues)
         -> Groq AI API
  -> Discovery Service (Eureka)
Monitoring: Prometheus/Grafana + Logstash (logs) + ELK

## Luong request tong quat
1) Client goi API qua API Gateway.
2) Gateway kiem tra rate limit, validate input, JWT (RS256) va forward den service.
3) User Service xu ly nghiep vu (dat ban, auth, thanh toan, review).
4) Event Outbox trong User Service publish len RabbitMQ.
5) Search Service consume event va cap nhat Elasticsearch, hoac sync tu user-service.
6) Chat Service nhan message (REST/WebSocket), luu DB, goi AI (Groq) neu can.

# Project Structure
- `api-gateway/`: Spring Cloud Gateway, JWT filter, rate limit, input validation, AES decryption.
- `discovery-service/`: Eureka Server.
- `backend-service/`: User service (core business). JPA + MongoDB + Redis + RabbitMQ + OAuth2.
- `search-service/`: Search/analytics service. Elasticsearch + RabbitMQ + Redis.
- `chat-service/`: Chat + WebSocket/STOMP + AI integration.
- `monitoring/`: Prometheus, Grafana dashboards, logstash config.
- `elk/`: Elasticsearch config skeleton.
- `docker-compose*.yml`, `init-*.sql`: Docker stack va khoi tao DB.
- `keys/`, `uploads/`, `logs/`: key JWT, file upload, log.

## Giai thich package/module (backend-service)
- `controller/`, `admin/controller/`, `analytics/controller/`: REST API (user, admin, analytics/transaction demo).
- `service/`, `service/implement/`: business logic (auth, booking, dish, review, payment, sync).
- `service/advance/`: event sourcing, distributed lock, transaction demo.
- `event/`: outbox + chat event producer.
- `model/`, `event/model/`: JPA entities + event store.
- `repository/`: Spring Data repositories.
- `security/`: JWT, user details.
- `config/`: security, redis, rabbit, websocket, distributed transaction.

## Giai thich package/module (search-service)
- `controller/`: REST API search va sync.
- `consumer/`: RabbitMQ consumers (dish/user/review/booking).
- `document/`: Elasticsearch document models.
- `service/`: search, analytics, sync, cache.
- `config/`: Elasticsearch index setup, RabbitMQ, Redis, JWT.
- `model/`: JPA models (processed_events).

## Giai thich package/module (chat-service)
- `controller/`: REST + WebSocket entrypoints.
- `service/`: room, message, AI, websocket send.
- `consumer/`: RabbitMQ consumers.
- `model/`: chat rooms/messages/participants.
- `config/`: WebSocket, Redis, RabbitMQ, AI config.

# Core Components
## Service
- `AuthServiceImpl` (`backend-service`): dang ky, login, refresh token, lock account, email verification, token rotation.
- `BookingServiceImpl`: dat ban + pre-order, gui email/notification, log mongo, outbox event.
- `ReviewServiceImpl`: review CRUD + spam prevention + cap nhat rating dish.
- `UnifiedPaymentServiceImpl`, `MoMoPaymentServiceImpl`, `VNPayPaymentServiceImpl`: thanh toan.
- `BookingEventSourcingService` + `EventStoreService`: event sourcing (MongoDB).
- `OutboxEventServiceImpl`: Outbox pattern + publish RabbitMQ (scheduled).
- `DataSyncService` (`search-service`): sync dishes/users/reviews tu user-service.
- `DishSearchService`/`AdvancedDishSearchService`: truy van Elasticsearch.
- `ChatRoomServiceImpl`, `ChatServiceImpl`: tao/lay room, xu ly message, cache.
- `ChatAIServiceImpl`: goi Groq API.

## Controller
- User Service:
  - `/api/v1/auth/**`: register/login/refresh/logout/verify-email.
  - `/api/v1/bookings`, `/api/v1/dishes`, `/api/v1/reviews`, `/api/v1/payments/**`.
  - `/api/users`, `/api/v1/home`, `/api/v1/upload`.
  - `/api/v1/admin/**`: dashboard, user/dish/booking/payment/review admin.
  - `/api/v1/sync/**`: dong bo du lieu cho search-service.
  - `/api/event-sourcing/**`: demo event sourcing.
  - `/api/transactions/**`: demo transaction (locking/isolation/propagation).
- Search Service:
  - `/api/v1/dishes/**`, `/api/v1/search/advanced/**`, `/api/search/reviews/**`, `/api/v1/users/**`.
- Chat Service:
  - `/api/v1/chat/**`, `/api/v1/rooms/**`, `/api/v1/admin/chat/**`, `/api/v1/guest/**`.
  - WebSocket: `/ws` (STOMP), app dest `/app/chat.send`.

## Repository
- User Service: `UserRepository`, `BookingRepository`, `DishRepository`, `ReviewRepository`,
  `PaymentRepository`, `RefreshTokenRepository`, `OutboxEventRepository`, `EventStoreRepository`.
- Search Service: `DishDocumentRepository`, `UserDocumentRepository`, `ReviewDocumentRepository`,
  `ProcessedEventRepository`.
- Chat Service: `ChatRoomRepository`, `ChatMessageRepository`.

## Domain / Entity
- User Service (MySQL): `User`, `UserRole`, `Booking`, `TableEntity`, `Dish`, `Review`,
  `Payment`, `PreOrder`, `RefreshToken`, `Image`, `Notification`, `OrderHistory`, `OAuth2Link`,
  `OutboxEvent`.
- User Service (MongoDB): `DomainEvent`, `ActivityLog`, `BookingHistory`, `UserSession`...
- Search Service (MySQL): `ProcessedEvent`.
- Search Service (Elasticsearch): `DishDocument`, `UserDocument`, `ReviewDocument`, `BookingDocument`.
- Chat Service (MySQL): `ChatRoom`, `ChatMessage`, `ChatParticipant`.

# API Documentation
## Danh sach endpoint chinh (rut gon)
### User Service
- Auth
  - `POST /api/v1/auth/register`: dang ky (multipart/JSON, co the co avatar).
  - `POST /api/v1/auth/login`: login.
  - `POST /api/v1/auth/refresh-token`: refresh token.
  - `POST /api/users/verify-email`: verify email token.
- Booking/Dish/Review
  - `POST /api/v1/bookings`, `GET /api/v1/bookings/{id}`, `GET /api/v1/bookings/history`
  - `GET /api/v1/dishes`, `GET /api/v1/dishes/{id}`, `POST /api/v1/dishes`
  - `POST /api/v1/dishes/{dishId}/reviews`, `GET /api/v1/dishes/{dishId}/reviews`
- Payment
  - `POST /api/v1/payments/create`
  - `POST /api/v1/payments/{paymentMethod}/callback`
  - `GET /api/v1/payments/status/{paymentId}`
- Sync cho search-service
  - `GET /api/v1/sync/dishes/all`, `GET /api/v1/sync/users/all`, `GET /api/v1/sync/reviews/all`

### Search Service
- Dish search
  - `GET /api/v1/dishes/search`
  - `GET /api/v1/dishes/search/advanced`
  - `GET /api/v1/dishes/search/filters`
  - `GET /api/v1/dishes/top-rated`
- Advanced search & analytics
  - `GET /api/v1/search/advanced/fuzzy`
  - `GET /api/v1/search/advanced/aggregations`
  - `GET /api/v1/search/advanced/analytics/*`

### Chat Service
- `POST /api/v1/chat/send`: gui tin nhan (user/admin/AI).
- `GET /api/v1/chat/history`
- `GET /api/v1/rooms/{userId}`, `POST /api/v1/rooms/private`
- `POST /api/v1/guest/send`, `POST /api/v1/guest/migrate`
- WebSocket STOMP: `/ws` + `/app/chat.send`

## Mo ta request / response (rut gon)
Auth login
```json
POST /api/v1/auth/login
{
  "username": "string",
  "password": "string",
  "sessionId": "optional"
}
```
Response:
```json
{
  "userId": 1,
  "username": "string",
  "role": "ADMIN|CUSTOMER",
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "emailVerified": true
}
```

Booking create
```json
POST /api/v1/bookings
{
  "username": "string",
  "tableId": 1,
  "bookingTime": "2026-02-01T12:00:00",
  "numberOfGuests": 2,
  "note": "string",
  "preOrderDishes": [
    { "dishId": 10, "quantity": 2, "note": "less spicy" }
  ]
}
```
Response: `BookingDTO` (id, status, totalAmount, preOrders...)

Search dish
```http
GET /api/v1/dishes/search?keyword=sushi&page=0&size=10
```
Response: danh sach `DishDocument` (id, name, price, rating, category).

# Database Design
## Bang chinh (MySQL - user-service)
- `users` (User) -> `user_roles` (UserRole) many-to-one.
- `bookings` -> `users`, `tables` many-to-one.
- `preorders` -> `bookings`, `dishes` many-to-one.
- `payments` -> `bookings` many-to-one.
- `reviews` (soft delete, verify/active flags) -> `dishes`.
- `refresh_token` -> `users` many-to-one.
- `outbox_events` (Outbox pattern).

## MongoDB (user-service)
- `domain_events` (event store) cho event sourcing.
- `activity_logs`, `booking_history`, `user_session`, `review_analytics` (analytics).

## Search Service
- MySQL: `processed_events` (idempotency for RabbitMQ events).
- Elasticsearch: `dishes`, `users`, `reviews`, `bookings` indexes.

## Chat Service
- `chat_rooms`, `chat_messages`, `chat_participants` (room + message + participant).

## Quan he chinh
- User 1-N Booking
- Booking 1-N PreOrder
- Booking 1-N Payment
- Dish 1-N Review
- User 1-N RefreshToken
- ChatRoom 1-N ChatMessage
- ChatRoom 1-N ChatParticipant

# Security & Configuration
- JWT RS256: private key trong user-service, public key cho api-gateway/search-service.
- Gateway filter: validate JWT, rate limit (Redis), input validation, optional AES decrypt.
- Spring Security (user-service): role-based access, stateless JWT.
- Refresh token: rotation + revoke + tracking (device/ip/user-agent).
- CORS config, WebSocket handshake headers.
- RabbitMQ exchanges: `dish.exchange`, `user.exchange`, `review.exchange`, `booking.exchange`.
- Cache: Redis + Caffeine (L1/L2).
- Monitoring: Actuator + Prometheus + Grafana dashboards.

# How to Run
## Local
1) Tao file `.env` (tham khao `/.env`), bo sung bien moi truong.
2) Chay dependency: MySQL, MongoDB, Redis, RabbitMQ, Elasticsearch.
3) Chay tung service:
   - `./gradlew bootRun` trong `discovery-service/`
   - `./gradlew bootRun` trong `backend-service/`
   - `./gradlew bootRun` trong `search-service/`
   - `./gradlew bootRun` trong `chat-service/`
   - `./gradlew bootRun` trong `api-gateway/`

## Docker
- Su dung `docker-compose.yml` (core stack) hoac `docker-compose.full.yml` (full stack).
```bash
docker-compose up -d
```

# Improvement Suggestions
## Kien truc
- Dong bo version Spring Boot/Cloud giua cac service (hien tai 3.1.x -> 3.5.x).
- Tieu chuan hoa base path API (`/api/v1/...`) de giam nham lan giua `/api/users` va `/api/v1/users`.
- Danh gia lai luong event: outbox -> RabbitMQ -> search-service (gap retry/monitoring rõ rang).

## Code quality
- Bo sung migration (Flyway/Liquibase) thay cho `ddl-auto=update` o production.
- Chuan hoa validation/request DTO (Bean Validation) va error response format.
- Giam log nhay cam (token, header) trong production.
- Xem lai mot so path mapping co the sai: `"/update{id}"`, `"/delete{id}"` (thieu `/`).

## Scalability
- Tai gateway: rate limit theo user + IP (Redis) va circuit breaker.
- Search service: bulk indexing + backpressure khi event burst.
- Chat service: scale websocket + sticky session + message broker relay tu RabbitMQ.
