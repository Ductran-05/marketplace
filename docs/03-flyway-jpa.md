# Flyway vs JPA/Hibernate — ai làm gì với database

## Một câu tóm gọn

> Code Java version bằng Git; cấu trúc DB cũng cần version — Flyway làm việc đó bằng file SQL đánh số trong repo, tự đồng bộ mọi DB (máy mình, máy khác, production) lên đúng phiên bản.

Flyway = "Git cho schema database".

## Phân vai rõ ràng

| | Flyway | Hibernate/JPA (`@Entity`) |
|---|---|---|
| Nhiệm vụ | **TẠO/SỬA bảng** | **ĐỌC/GHI dữ liệu** vào bảng đã có |
| Nguồn | Chỉ file `.sql` trong `db/migration/` | Class có `@Entity` (bản đồ ORM) |
| Chạy khi | 1 lần lúc khởi động | Mỗi query, suốt vòng đời app |

**Flyway KHÔNG biết `@Entity` tồn tại. Hibernate KHÔNG đọc file .sql.** Hai công cụ độc lập, chỉ "gặp nhau" ở schema thật trong Postgres. `ddl-auto: validate` là chốt kiểm: Hibernate đối chiếu bản đồ @Entity với schema thật lúc khởi động — lệch là app từ chối chạy.

## Cách Flyway hoạt động

```
db/migration/V1__create_users_table.sql
             V2__create_products_table.sql
             V3__add_image_key_to_products.sql
```

Mỗi lần khởi động: so bảng `flyway_schema_history` (sổ ghi đã chạy gì) với file trong repo → chạy phần còn thiếu theo thứ tự → ghi sổ. `make reset-db` xóa sạch → khởi động lại → schema tự mọc lại từ V1.

Quy tắc tên: `V{số}__{mô_tả}.sql`. **Không bao giờ sửa file đã chạy** — muốn đổi thì tạo file V mới.

## Ba kịch bản "quên" và hậu quả

1. **Thêm field vào Entity, quên viết migration** → app từ chối khởi động: `Schema-validation: missing column [x]`. An toàn — lỗi to, rõ, ngay lập tức.
2. **Sửa DB bằng tay trong DbGate** → máy mình chạy ngon, KHÔNG lỗi gì — nhưng Git không có migration nào ghi lại → máy khác/production thiếu cột → "works on my machine". Nguy hiểm nhất vì âm thầm. Kỷ luật: mọi thay đổi schema đi qua file migration.
3. **Sửa file migration đã chạy** → `FlywayValidateException: checksum mismatch` → app từ chối khởi động. Fix: trả file về nguyên trạng, viết V mới.

Không dùng Flyway thì thay bằng gì? `ddl-auto: update` (Hibernate tự sửa bảng) — cấm kỵ production: không xóa/đổi tên cột được, không kiểm soát, không lịch sử.

## Flow thêm 1 entity mới (trong ra ngoài)

```
1. Domain Entity + Value Objects      (Java thuần)
2. Domain Repository Interface
3. Flyway migration V{n}__....sql
4. JPA Entity (@Entity, @Table)
5. Spring Data Repository (extends JpaRepository)
6. Repository Impl (mapper toDomain/toEntity)
7. UseCase
8. Controller + Request/Response DTO
```

Thêm field mới (vd `phone`): sửa **3 nơi** — migration mới + JPA entity + domain/mapper. Thiếu nơi nào `validate` bắt lúc khởi động.
