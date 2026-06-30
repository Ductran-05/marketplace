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
| auth | ✅ | ✅ Register/Login | ✅ JWT, JPA | ✅ Controller |
| product | 🔲 | 🔲 | 🔲 | 🔲 |
| order | 🔲 | 🔲 | 🔲 | 🔲 |
| payment | 🔲 | 🔲 | 🔲 | 🔲 |
| notification | 🔲 | 🔲 | 🔲 | 🔲 |

## Lộ trình tiếp theo
1. **Auth**: JWT filter, refresh token, email verification
2. **Product**: CRUD, upload ảnh lên MinIO/S3
3. **Order**: tạo order, Kafka event `order.placed`
4. **Notification**: lắng nghe Kafka, gửi email xác nhận
5. **Payment**: tích hợp Stripe / PayOS
