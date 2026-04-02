# Phase 1 Overview – Event Design Goals
Muc tieu tuan 1 la tao nen nen tang event-driven vua du, tap trung vao domain events can thiet de:
- Dong bo du lieu tim kiem (search-service) va giam coupling HTTP.
- Ho tro cac luong quan trong (booking, user, dish, review, chat session).
- Tranh over-engineering: khong ep moi service phai dung event.

# Service-by-Service Event Suitability Analysis
| Service | Vai tro | Tan suat thay doi du lieu | Coupling | Nen dung event? | Ly do |
|---|---|---|---|---|---|
| `api-gateway` | Edge | Thap (stateless) | Thap | Khong | Gateway chi routing + security. Event domain khong thuoc ve gateway. |
| `discovery-service` | Infra | Rat thap | Thap | Khong | Eureka la registry, khong co domain state can phat event. |
| `backend-service` (user-service) | Core | Cao | Cao | Nen (publish) | Source of truth cho user/dish/booking/review. Event giam coupling voi search/chat va de mo rong analytics. |
| `search-service` | Supporting | Trung binh (chi doc/index) | Trung binh | Nen (consume) | Can cap nhat Elasticsearch theo event; khong nen lam source of truth. |
| `chat-service` | Supporting/edge-ish | Trung binh | Trung binh | Chi dung event noi bo | Dang dung RabbitMQ cho chat queue + session conversion. Khong can phat domain event ra ngoai o Phase 1. |
| Monitoring/ELK | Infra | Thap | Thap | Khong | Log/metrics thuoc ve observability, khong can domain events. |

Nhan xet nhanh tu code:
- `backend-service` da co Outbox cho booking va RabbitMQ publisher cho dish/user (chua co review).
- `search-service` da consume `dish.*`, `user.*`, `review.*`, `booking.*`, va co idempotency (`processed_events`).
- `chat-service` da consume event `chat.routing.convert` de migrate session -> user.

# Event Schema Definitions
## Nguyen tac chung
- Envelope tong quat: `eventName`, `eventVersion`, `eventId`, `timestamp`, `producer`, `payload`.
- Payload chi chua data can thiet cho consumer (search/indexing), khong le leak DB internals.
- Event name theo dang `domain.action` (vi du: `booking.created`).

## Event Envelope (v1)
```json
{
  "eventName": "booking.created",
  "eventVersion": "v1",
  "eventId": "evt_123e4567",
  "timestamp": "2026-02-06T10:33:00Z",
  "producer": "user-service",
  "payload": {}
}
```

## Booking Events (producer: user-service, consumer: search-service)
`booking.created` payload:
```json
{
  "bookingId": 1001,
  "userId": 10,
  "username": "alice",
  "tableId": 5,
  "tableName": "A5",
  "bookingTime": "2026-02-06T12:00:00",
  "numberOfGuests": 2,
  "status": "PENDING",
  "note": "Less spicy",
  "totalAmount": 350000,
  "version": 1,
  "triggeredBy": "alice"
}
```

`booking.status.changed` payload:
```json
{
  "bookingId": 1001,
  "oldStatus": "PENDING",
  "newStatus": "CONFIRMED",
  "version": 2,
  "triggeredBy": "admin"
}
```

`booking.cancelled` payload (neu can):
```json
{
  "bookingId": 1001,
  "reason": "Customer request",
  "triggeredBy": "alice"
}
```

## Dish Events (producer: user-service, consumer: search-service)
`dish.created` / `dish.updated` payload:
```json
{
  "dishId": 2001,
  "name": "Sushi",
  "description": "Fresh salmon",
  "price": 120000,
  "isAvailable": true,
  "category": "Japanese",
  "imageUrls": ["https://..."],
  "averageRating": 4.7,
  "totalReviews": 120,
  "orderCount": 540,
  "createdAt": "2026-02-01T08:00:00"
}
```

`dish.deleted` payload:
```json
{
  "dishId": 2001
}
```

`dish.availability.changed` payload:
```json
{
  "dishId": 2001,
  "isAvailable": false
}
```

`dish.rating.updated` payload:
```json
{
  "dishId": 2001,
  "averageRating": 4.8,
  "totalReviews": 125
}
```

## User Events (producer: user-service, consumer: search-service)
`user.created` / `user.updated` payload:
```json
{
  "userId": 10,
  "username": "alice",
  "fullName": "Alice Nguyen",
  "email": "alice@example.com",
  "phoneNumber": "0909xxx",
  "address": "HCM",
  "roleName": "CUSTOMER",
  "status": "ACTIVE",
  "avatarUrl": "https://..."
}
```

`user.deleted` payload:
```json
{
  "userId": 10
}
```

`user.status.changed` payload:
```json
{
  "userId": 10,
  "status": "LOCKED"
}
```

## Review Events (producer: user-service, consumer: search-service)
`review.created` / `review.updated` payload:
```json
{
  "reviewId": 9001,
  "dishId": 2001,
  "customerName": "Alice",
  "customerEmail": "alice@example.com",
  "customerAvatar": "https://...",
  "rating": 5,
  "comment": "Great",
  "isActive": true,
  "isVerified": false,
  "createdAt": "2026-02-06T10:00:00"
}
```

`review.deleted` payload:
```json
{
  "reviewId": 9001,
  "dishId": 2001
}
```

`review.status.changed` payload:
```json
{
  "reviewId": 9001,
  "dishId": 2001,
  "isActive": false,
  "isVerified": true
}
```

## Chat Session Event (producer: user-service, consumer: chat-service)
`chat.session.converted` payload:
```json
{
  "sessionId": "sess_abc",
  "userId": 10
}
```

Luu y tu code hien tai:
- `DishEvent`/`UserEvent` dang set `eventType = DISH_CREATED/USER_CREATED` trong payload,
  nhung `search-service` dang expect `eventType = "dish.created"/"user.created"`.
  Can chuan hoa `eventName` de tranh mismatch.
- `Review` events chua duoc publish, trong khi `search-service` co consumer.

# Versioning Strategy
Muc tieu: de maintain cho project ca nhan, nhung co the scale len production-grade.
- `eventVersion` dung `v1`, `v2` (string).
- Chi bump version khi:
  - Thay doi nghia field (semantic change).
  - Xoa/rename field da duoc consumer su dung.
  - Thay doi type (string -> number).
- Khong can bump version khi:
  - Them field moi (optional).
  - Them event moi (eventName moi).
- Backward compatibility:
  - Consumer phai ignore field khong biet.
  - Producer giu field cu toi thieu 1 version khi co thay doi.
- Xu ly consumer cu:
  - Producer co the publish song song `v1` + `v2` trong thoi gian chuyen doi.
  - Consumer uu tien `v2`, fallback `v1`.

# Contract Testing Approach
Muc tieu: nhe, de duy tri, khong over-engineering.

## Producer vs Consumer responsibility
- Producer: dam bao event dung schema, co day du field bat buoc.
- Consumer: dam bao handle duoc event theo schema, bo qua field la.

## Cong cu de xuat (Java)
Option nhe (Phase 1):
- JSON Schema + JUnit (networknt json-schema-validator).
- Luu schema trong repo (vi du: `document/event-schemas/`).

Option mo rong (Phase 2+):
- Spring Cloud Contract (Message-based).
- Pact (Message Pact) neu can consumer-driven contracts.

## Flow test mau (booking.created)
1) Producer test (user-service):
   - Build payload `booking.created`.
   - Validate JSON against schema `booking.created.v1.json`.
2) Consumer test (search-service):
   - Load sample JSON (contract fixture).
   - Deserialize -> `BookingEvent`.
   - Assert khong throw va update document duoc.

# Week 1 Action Checklist
Day 1:
- Review tat ca event hien co (booking/dish/user/chat session).
- Chuan hoa naming `eventName` (lowercase + dot).

Day 2:
- Tao JSON schema cho `booking.created`, `booking.status.changed`, `dish.*`, `user.*`.
- Tao sample payload JSON de testing.

Day 3:
- Them publish review events (neu can dong bo search).
- Fix mismatch `eventType` vs `search-service` expectation.

Day 4:
- Them contract tests (producer + consumer) cho 1 event mau.
- Bo sung idempotency assertion (processed_events).

Day 5:
- Chay full flow local (user-service -> rabbit -> search-service).
- Update doc + checklist cho Phase 2.

