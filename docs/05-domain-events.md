# Domain Event — lưu ở đâu, vì sao dùng

## Event KHÔNG được lưu — chỉ tồn tại trong RAM

```
1. User.register()      → event vào List<DomainEvent> của aggregate (RAM)
2. pullEvents()         → rút ra, list clear (tránh phát 2 lần)
3. publishEvent()       → vào Spring context (vẫn RAM)
4. Listener xử lý xong  → garbage collector dọn, biến mất vĩnh viễn
```

DB chỉ thấy **kết quả** của event (user được tạo, mail được gửi), không thấy event.

## AggregateRoot làm gì

Base class với đúng 1 việc: **thu thập domain event**. `registerEvent()` (entity con gọi khi có chuyện xảy ra) + `pullEvents()` (UseCase rút ra để publish, rút xong xóa).

Vì sao vòng vèo, sao không publish thẳng trong `User.register()`?
1. Domain không được phụ thuộc Spring — không inject ApplicationEventPublisher vào entity được.
2. Event chỉ nên phát SAU khi save thành công — tách 2 bước cho UseCase kiểm soát thứ tự.
3. Test domain chỉ cần assert `pullEvents()` chứa event — không mock messaging.

## "Không dùng event thì gửi mail trong UseCase — có vi phạm DDD không?"

**Không hẳn** — phân biệt cho chính xác:
- Gọi mailer qua port trong UseCase: KHÔNG phạm luật tầng nào. Application được phép điều phối side effect.
- Vi phạm THẬT là nhét logic mail vào domain entity (domain biết SMTP).
- Gọi thẳng trong UseCase chỉ là **thiết kế kém hơn**: (1) coupling tăng dần — thêm voucher/analytics là sửa code đăng ký (vi phạm Open/Closed); (2) mail chạy TRONG transaction — gửi rồi mà commit fail, hoặc SMTP treo giữ connection DB; (3) mất ngữ nghĩa — "user đã đăng ký" không còn là khái niệm trong code.

Domain event = công cụ DDD **khuyên dùng** khi 1 hành động kéo nhiều hệ quả, không phải luật bắt buộc.

## Điểm yếu hiện tại và Outbox pattern (Phase 3)

Khoảng hở: `commit ✓ → [app crash] → listener chưa chạy` → user có trong DB nhưng mail không bao giờ gửi, event mất không dấu vết. Với mail OTP chấp nhận được (có resend); với `OrderPaidEvent` là mất tiền.

**Outbox pattern**: bảng `outbox_events` ghi **cùng transaction** với dữ liệu chính → background job đọc outbox chưa xử lý → đẩy Kafka → đánh dấu processed. App crash? Event vẫn trong DB, khởi động lại xử lý tiếp. Giải quyết dual-write problem.

| Cách | Event ở đâu | Mất được? | Dùng khi |
|---|---|---|---|
| Spring Event (hiện tại) | RAM | Có | Việc phụ (mail chào mừng) |
| Outbox (Phase 3) | Bảng trong DB | Không | Việc quan trọng (đơn hàng, tiền) |
| Event Sourcing | Event LÀ source of truth | Không | Overkill cho dự án này |

## Chiến lược nâng cấp dần

Domain code KHÔNG ĐỔI qua các phase — `User.register()` vẫn chỉ `registerEvent()`. Chỉ "người nghe" thay đổi: listener nội bộ → bridge đẩy Kafka → service khác consume. Đó là lợi ích của tách domain event khỏi cơ chế vận chuyển.
