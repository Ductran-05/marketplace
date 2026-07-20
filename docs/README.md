# Tài liệu dự án Marketplace

Tổng hợp kiến thức trong quá trình xây dựng — đọc lại khi quên.

| File | Nội dung |
|---|---|
| [01-architecture.md](01-architecture.md) | Clean Architecture, DDD concepts, port & adapter, dependency inversion |
| [02-infrastructure.md](02-infrastructure.md) | Các server hạ tầng: Postgres, Redis, Kafka, MinIO, Mailhog — vai trò và cách xem dữ liệu |
| [03-flyway-jpa.md](03-flyway-jpa.md) | Flyway vs JPA/Hibernate — ai tạo bảng, ai map dữ liệu, các kịch bản "quên" |
| [04-auth-flow.md](04-auth-flow.md) | Flow đăng ký/xác thực/JWT/refresh rotation đầy đủ |
| [05-domain-events.md](05-domain-events.md) | Domain event, cơ chế Spring event, vì sao không gọi mail thẳng trong UseCase |
| [06-minio-storage.md](06-minio-storage.md) | Object storage, presigned URL, flow upload ảnh |
| [07-api-testing.md](07-api-testing.md) | Swagger vs Bruno, cơ chế token, contract-first |
| [08-deployment-cicd.md](08-deployment-cicd.md) | Các con đường deploy, độ khó thay thế từng dịch vụ, kế hoạch CI/CD |
| [09-erd.md](09-erd.md) | ERD database (mermaid) + các quyết định thiết kế schema |
| [10-saga.md](10-saga.md) | Saga choreography cho flow đặt hàng: topics, idempotency, compensation, trade-off |
| [11-frontend.md](11-frontend.md) | React SPA: stack tối giản, Vite dev proxy thay CORS, quản lý token, phát hiện backend chỉ trả 403 |

## Sơ đồ kiến trúc trực quan

Artifact (cập nhật theo tiến độ): https://claude.ai/code/artifact/8eeea333-0ff9-4d1e-85c4-abd432715027
