# Kiến trúc: Clean Architecture + DDD concepts

## Lộ trình tổng thể

**Modular Monolith trước → Microservices sau.** Microservices giải quyết vấn đề của team lớn/hệ thống phức tạp — bắt đầu bằng nó khi học sẽ mất thời gian vào infrastructure thay vì business logic. Xây monolith có kỷ luật module, khi tách service sẽ dễ.

## Bốn tầng và quy tắc duy nhất

```
Presentation → Application → Domain ← Infrastructure
```

**Dependency chỉ trỏ VÀO TRONG.** Domain không biết tầng nào khác tồn tại.

| Tầng | Chứa gì | Được import gì |
|---|---|---|
| `presentation/` | Controller, Request/Response DTO | Application |
| `application/` | UseCase, Command/Query, **Port (interface)** | Domain |
| `domain/` | Entity, Value Object, Domain Event, Repository interface | KHÔNG GÌ CẢ (Java thuần) |
| `infrastructure/` | JPA entity, Repository impl, Adapter, Listener | Application + Domain (để implement) |

**Cách kiểm tra nhanh một file có "sạch" không: nhìn import.** File domain mà có `import jakarta.persistence` hay `org.springframework` → vi phạm.

## DDD concepts đang dùng

- **Entity / Aggregate Root**: `User`, `Product` — extends `AggregateRoot` (shared). Aggregate = cụm object thay đổi cùng nhau như một khối; Root là cổng vào duy nhất, chỉ Root có repository và phát event.
- **Value Object**: `Email`, `UserId`, `Money`, `ProductId` — Java record, tự validate trong constructor. Sau khi tạo là chắc chắn hợp lệ.
- **Domain Event**: `UserRegisteredEvent` — sự thật nghiệp vụ đã xảy ra, tên thì quá khứ.
- **Repository interface ở domain, impl ở infrastructure**.
- **UseCase pattern**: 1 class, 1 method `execute()`, 1 trách nhiệm. UseCase điều phối, domain quyết định.
- KHÔNG dùng: Event Sourcing, full CQRS (overkill).

## Port & Adapter (Hexagonal)

Ẩn dụ ổ cắm điện: **Port** = chuẩn ổ cắm (do thiết bị/lõi nghiệp vụ định nghĩa — "tôi cần gì"), **Adapter** = cục sạc (chuyển công nghệ cụ thể khớp với chuẩn — "làm bằng gì").

| Port (application/domain định nghĩa) | Adapter (infrastructure) | Công nghệ giấu đi |
|---|---|---|
| `OtpStore` | `RedisOtpStore` | Redis |
| `RefreshTokenStore` | `RedisRefreshTokenStore` | Redis |
| `VerificationMailer` | `SmtpVerificationMailer` | SMTP |
| `ImageStorage` | `MinioImageStorage` | MinIO/S3 |
| `UserRepository`, `ProductRepository` | `*RepositoryImpl` | JPA/Postgres |

Đổi công nghệ = thay adapter, lõi bất động.

## Dependency Inversion — tại sao "vòng vo"

Application cần gửi mail nhưng CẤM biết SMTP. Giải pháp: application tự định nghĩa interface (`VerificationMailer`), infrastructure implement nó. Cả 2 mũi tên source-code đều trỏ VỀ application — đảo ngược so với gọi thẳng xuống.

Trả giá: thêm 1 file interface. Được lại:
- Test UseCase bằng mock, không cần hạ tầng thật
- Đổi công nghệ không sửa nghiệp vụ
- Compiler cưỡng chế kiến trúc (import sai là thấy ngay)

## Hai repository dễ nhầm

```
UseCase
  └─► UserRepository (domain interface — hợp đồng nghiệp vụ, nói bằng Email/User)
        └─► UserRepositoryImpl (mapper domain ↔ JPA, 2 hàm toEntity/toDomain)
              └─► UserJpaRepository (Spring Data — TỰ SINH implementation lúc runtime)
                    └─► Hibernate → Postgres
```

`UserJpaRepository` không ai viết impl — Spring Data tạo proxy lúc khởi động, sinh SQL từ **tên method** (`findByEmail` → `WHERE email = ?`). Tên sai field → fail ngay lúc khởi động.

## Query để đâu (CQRS nhẹ)

- **Command** (write): luôn có object riêng (6-7 field), đi qua domain đầy đủ.
- **Query** (read): 1-2 tham số thì truyền thẳng, KHÔNG tạo object nghi lễ. Package `query/` sẽ đầy khi có search phức tạp (SearchProductsQuery với keyword/minPrice/maxPrice...).
- Nguyên tắc: pattern phục vụ mình, không phải mình phục vụ pattern.

## Annotation của ai?

- `jakarta.persistence.*` (@Entity, @Id, @Column) — chuẩn JPA của **Jakarta EE**, KHÔNG phải Spring. Hibernate là bên thực thi.
- `jakarta.validation.*` (@NotBlank, @Email) — chuẩn Bean Validation.
- `org.springframework.*` (@Service, @RestController, @Transactional) — Spring.
- Spring Boot là "người lắp ráp": starter kéo Hibernate/Tomcat về + auto-configure.
- Nhớ theo tầng: presentation dùng Spring Web + Validation, application dùng @Service/@Transactional, domain KHÔNG CÓ GÌ, infrastructure dùng JPA + Spring.
