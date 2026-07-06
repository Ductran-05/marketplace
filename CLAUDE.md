# Marketplace Backend — CLAUDE.md

## Tổng quan dự án
Ứng dụng backend marketplace viết bằng Java 21 + Spring Boot 3.5 để học các kỹ năng: authentication, events, mail, payment, microservices, cloud storage, CI/CD.

## Tech Stack
- **Java**: 21 (LTS)
- **Framework**: Spring Boot 3.5
- **Build**: Maven
- **Database**: PostgreSQL + Flyway migration
- **Cache**: Redis
- **Message Broker**: Kafka
- **Auth**: JWT (jjwt 0.12.x) + Spring Security
- **Mail**: Spring Mail + Mailhog (dev)
- **Docs**: SpringDoc OpenAPI (Swagger UI tại `/swagger-ui.html`)

## Kiến trúc
**Clean Architecture** — dependency chỉ trỏ vào trong:
```
Presentation → Application → Domain ← Infrastructure
```

**Áp dụng một số DDD concepts** (không full DDD):
- Entity, Value Object, Aggregate Root
- Domain Event
- Repository Interface (domain) / Repository Impl (infrastructure)
- UseCase pattern thay vì Service thuần

## Cấu trúc package
Mỗi feature là 1 package độc lập:
```
com.marketplace/
├── auth/
│   ├── presentation/      ← Controller, Request/Response DTO
│   ├── application/       ← UseCase, Command/Query
│   ├── domain/            ← Entity, Value Object, Repository Interface, Domain Event
│   └── infrastructure/    ← JPA Entity, Repository Impl, JWT, Adapter
├── product/               ← (tương tự)
├── order/
├── payment/
├── notification/
└── shared/
    ├── domain/            ← AggregateRoot, DomainEvent interface
    ├── exception/         ← BusinessException, NotFoundException, GlobalExceptionHandler
    └── config/            ← SecurityConfig và các config chung
```

## Quy tắc bắt buộc
1. **Domain layer KHÔNG được import** package `infrastructure`, `jakarta.persistence`, Spring annotations
2. **Giao tiếp giữa modules** qua Domain Event (Kafka) hoặc UseCase — KHÔNG gọi trực tiếp repository của module khác
3. **JPA Entity** tách biệt hoàn toàn với **Domain Entity** — có mapper/converter ở infrastructure
4. **Controller** chỉ nhận Request DTO và trả Response DTO — không expose Domain Entity
5. **UseCase** = 1 class, 1 method `execute()`, 1 trách nhiệm duy nhất
6. **Flyway** quản lý toàn bộ schema — KHÔNG dùng `ddl-auto: create` hay `update`

## Coding conventions
- **Không Lombok** ở domain layer — dùng record hoặc constructor thủ công
- **Lombok** được phép ở infrastructure/presentation nếu cần
- Value Object dùng Java `record`
- Command/Query dùng Java `record`
- Response DTO dùng Java `record`
- Không viết comment giải thích WHAT — chỉ comment khi WHY không rõ ràng

## Tài liệu
Thư mục `docs/` chứa tài liệu kiến thức theo chủ đề (kiến trúc, hạ tầng, Flyway, auth flow, events, MinIO, testing, deployment) — cập nhật khi có khái niệm/quyết định quan trọng mới.

## Quy ước bổ sung
- Thư mục `bruno/` là collection test API (app Bruno) — **khi thêm/sửa endpoint phải thêm/sửa file `.bru` tương ứng**. Request cần auth dùng `auth: bearer` với `token: {{accessToken}}`; token được tự lưu vào environment bởi script post-response của `04-login.bru`.

## Lệnh hay dùng
```bash
# Chạy infrastructure (DB, Redis, Kafka, Mail)
docker compose up -d

# Build project
./mvnw clean package -DskipTests

# Chạy app
./mvnw spring-boot:run

# Chạy tests
./mvnw test

# Tạo Flyway migration mới
# Đặt file tại: src/main/resources/db/migration/V{n}__{description}.sql

# Xem mail (Mailhog UI)
open http://localhost:8025

# Xem API docs
open http://localhost:8080/swagger-ui.html
```

## Environment variables (local dev)
Tất cả có default trong `application.yml`, không cần tạo `.env` khi dev local:
- `DB_USERNAME`, `DB_PASSWORD` — default: `marketplace`
- `JWT_SECRET` — thay bằng secret thật khi production (min 256 bits)
- `MAIL_HOST`, `MAIL_PORT` — default: Mailhog localhost:1025
- `REDIS_HOST`, `REDIS_PORT`
- `KAFKA_SERVERS`

## Modules và trạng thái

| Module | Domain | Application | Infrastructure | Presentation |
|--------|--------|-------------|----------------|--------------|
| auth | ✅ | ✅ Register/Login/Verify/Refresh/BecomeSeller | ✅ JWT filter, OTP Redis, mail listener | ✅ Controller đầy đủ |
| product | ✅ Product, Money | ✅ CRUD + phân trang, ownership, upload ảnh | ✅ JPA, MinIO adapter | ✅ @PreAuthorize SELLER |
| order | ✅ Order+OrderItem, state machine, 2 events | ✅ PlaceOrder (saga), Confirm/Cancel, GetOrders | ✅ JPA 1-n, outbox writer, inventory consumer | ✅ |
| payment | 🔲 | 🔲 | 🔲 | 🔲 |
| notification | — | — | ✅ OrderConfirmedConsumer → mail | — |

## Auth flow (đã hoàn chỉnh, đã test end-to-end)
```
register → mail OTP (Mailhog :8025) → verify → login → access+refresh token
→ gọi API với Bearer token → refresh (rotation: token cũ bị revoke)
```
- OTP: Redis key `otp:<email>`, TTL 15 phút, dùng 1 lần
- Refresh token: Redis key `refresh:<userId>`, rotation mỗi lần refresh
- JWT có `jti` để mỗi token unique (tránh trùng khi sinh cùng 1 giây)
- Listener gửi mail: `@Async` + `@TransactionalEventListener` (chỉ chạy sau commit)
- Lấy user hiện tại trong controller: `@AuthenticationPrincipal AuthenticatedUser user`

## Cloud storage (MinIO)
- Bucket `product-images`, tự tạo lúc app khởi động (`MinioImageStorage.ensureBucketExists`)
- Key format: `products/{productId}/{uuid}.{ext}` — chỉ nhận JPEG/PNG/WebP, max 5MB
- Client đọc ảnh qua **presigned URL** (hết hạn 15 phút) — tải thẳng từ MinIO, không qua app
- MinIO Console: `make minio` (http://localhost:9001, minioadmin/minioadmin)
- Đổi sang AWS S3 thật: chỉ cần đổi env `MINIO_*`, code giữ nguyên

## Cross-module (modular monolith)
- Module KHÔNG gọi repository/domain của module khác. Order → Product qua port `ProductCatalog` (order/application/port), adapter gọi UseCase của product (`GetProductsUseCase`, `DecreaseStockUseCase`). Khi tách microservices: thay adapter bằng HTTP client, port giữ nguyên.
- `Money` và `PageResponse` là shared kernel: `shared/domain/Money`, `shared/presentation/PageResponse`.
- Order snapshot tên+giá sản phẩm tại thời điểm đặt (không tham chiếu giá hiện tại).
- **SAGA (choreography)** cho đặt hàng — xem `docs/10-saga.md`: PlaceOrder KHÔNG trừ kho, tạo đơn PENDING → Kafka `order.placed` → product reserve/reject (idempotent qua bảng `stock_reservations`) → order confirm/cancel → notification gửi mail khi `order.confirmed`. Consumer nhận String tự parse; message contract ở `shared/messaging/`.

## Kafka
- KRaft mode (không Zookeeper), 2 listener: `localhost:9092` (app trên host), `kafka:29092` (container khác — Kafka UI dùng)
- Kafka UI: `make kafka-ui` (http://localhost:8090)
- Topic `order.placed` (3 partitions) khai báo ở `KafkaTopicsConfig`
- `OrderPlacedKafkaPublisher`: bridge Spring event → Kafka, key = orderId (giữ thứ tự theo order)
- Message contract (`OrderPlacedMessage`) tách khỏi domain event — phẳng, ổn định cho consumer ngoài

## Outbox pattern (event không thể mất)
- `OrderPlacedOutboxWriter`: `@TransactionalEventListener(BEFORE_COMMIT)` — dòng outbox ghi CÙNG transaction với đơn hàng
- `shared/outbox/`: `OutboxAppender` (serialize + ghi bảng), `OutboxRelay` (@Scheduled 2s: quét chưa processed → gửi Kafka có ack → đánh dấu; fail thì break giữ thứ tự, tick sau retry)
- Bảng `outbox_events` (V5), partial index cho dòng chưa processed
- Semantics: **at-least-once** — có thể gửi TRÙNG (crash giữa send và markProcessed) → consumer phải idempotent
- Relay dùng `KafkaTemplate<String,String>` riêng (payload đã là JSON string, tránh serialize 2 lần)
- Đã test: đặt hàng khi Kafka chết → 201 OK, event chờ trong outbox → Kafka sống lại → tự đến nơi

## Lộ trình tiếp theo
1. **Order đợt 4**: Notification module — Kafka consumer nghe `order.placed`, gửi mail xác nhận đơn (idempotent)
2. **Payment**: tích hợp Stripe / PayOS
3. **Test + CI/CD**: unit/integration test (Testcontainers), GitHub Actions
