# Auth flow đầy đủ

## Sơ đồ

```
POST /register ──► user PENDING_VERIFICATION
                   └─ sau commit, thread riêng: OTP → Redis (15'), mail → Mailhog

  (OTP hết hạn?) POST /resend-otp ──► OTP mới (cooldown 60s/email)

POST /verify {email, otp} ──► so Redis, đúng → ACTIVE, xóa OTP

POST /login ──► check ACTIVE + BCrypt password
                └─► accessToken (15') + refreshToken (7 ngày, lưu Redis)

GET /me (Bearer) ──► JwtAuthenticationFilter validate → AuthenticatedUser principal

POST /refresh ──► so token với Redis → cặp MỚI, token cũ revoke (rotation)
                  dùng lại token cũ → INVALID_REFRESH_TOKEN → login lại

POST /become-seller (Bearer) ──► role BUYER → SELLER trong DB
                                 PHẢI refresh/login lại để token mang role mới
```

## Flow register chi tiết (đi qua 4 tầng)

1. **Filter chain**: JwtAuthenticationFilter — không token cũng cho qua (anonymous); SecurityConfig thấy /register là public.
2. **Presentation**: @Valid check format → 422 nếu sai, không chạm business.
3. **Application** (RegisterUseCase, @Transactional): `new Email()` (VO tự validate — domain tự bảo vệ, không tin tầng ngoài) → check trùng email → BCrypt hash.
4. **Domain**: `User.register()` — status PENDING, role BUYER, ghi `UserRegisteredEvent` vào bộ nhớ aggregate (CHƯA phát).
5. **Infrastructure**: save → map domain→JPA → INSERT.
6. **UseCase**: `pullEvents()` → publish — listener là @TransactionalEventListener nên Spring GIỮ LẠI CHỜ COMMIT.
7. Commit ✓ → trả 201. Sau đó listener chạy ở thread riêng (@Async): sinh OTP, gửi mail.

Điểm thiết kế: event phát SAU commit (save fail thì không gửi mail cho user không tồn tại); mail lỗi không làm hỏng đăng ký (đã có resend làm lưới đỡ).

## JWT

- Access (15') / Refresh (7 ngày), claims: sub (userId), email, role, type, **jti**.
- **jti (UUID)**: bug thật đã gặp — JWT có iat độ chính xác GIÂY, 2 token sinh cùng giây + cùng claims = giống hệt từng byte → rotation "thành công giả". jti làm mỗi token unique.
- **Stale role**: JWT là snapshot lúc phát hành. become-seller xong, token cũ VẪN ghi BUYER — phải refresh/login lại. Trade-off kinh điển của stateless JWT.
- Filter chỉ nhận type=access; refresh token không gọi API được.
- Refresh rotation: mỗi user 1 token hợp lệ trong Redis; refresh xong token cũ tự vô hiệu.

## Cơ chế Spring Event ("danh bạ")

- **Lúc khởi động**: Spring quét mọi bean tìm @EventListener/@TransactionalEventListener, nhìn KIỂU THAM SỐ để biết nghe event gì → xây registry (ApplicationEventMulticaster).
- **Runtime**: publishEvent → tra kiểu event trong danh bạ → gọi listener qua reflection. Không ai nghe → event rơi vào hư không, không lỗi.
- @TransactionalEventListener: ghi vào danh sách chờ của transaction, commit xong mới gọi; rollback → hủy.
- @Async: ném vào thread pool (@EnableAsync), thread request giải phóng ngay.
- Giới hạn: danh bạ CHỈ trong 1 JVM. Kafka = cùng ý tưởng nhưng danh bạ nằm ở server riêng → người nghe có thể là service khác.

## Vị trí các mảnh event

```
domain/event/UserRegisteredEvent   ← ĐỊNH NGHĨA (sự thật nghiệp vụ)
domain/model/User                  ← GHI NHẬN (registerEvent trong aggregate)
application/RegisterUseCase        ← PHÁT (pullEvents + publish, quyết định thời điểm)
infrastructure/listener/...        ← NGHE (hệ quả kỹ thuật: Redis, SMTP, thread)
```

Kiểm tra trực giác: "cái gì thay đổi thì file này đổi?" — nghiệp vụ đổi → domain; quy trình đổi → application; công nghệ đổi → infrastructure.
