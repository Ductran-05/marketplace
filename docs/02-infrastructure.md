# Hạ tầng: các server độc lập xung quanh app

## Ý niệm quan trọng nhất

App Java và các hạ tầng là **những tiến trình hoàn toàn riêng biệt**, nói chuyện qua TCP. App không "nhúng" database — nó mở kết nối mạng đến `localhost:5432` như gọi một server ở xa. Đây là nền của mọi hệ thống phân tán: **một hệ thống = nhiều tiến trình chuyên biệt nói chuyện qua mạng.**

Bằng chứng: tắt app → Postgres vẫn sống, data còn nguyên. Tắt Docker → app lỗi Connection refused.

## Các service trong docker-compose

| Service | Port | Vai trò | Xem dữ liệu |
|---|---|---|---|
| **postgres** | 5432 | Dữ liệu có cấu trúc, bền vững (users, products) | `make db` (psql) hoặc DbGate |
| **redis** | 6379 | Dữ liệu tạm có TTL (OTP, refresh token, cooldown) | `make redis` hoặc DbGate |
| **kafka** + zookeeper | 9092 | Message broker — chờ Order module (Phase 3) | — |
| **minio** | 9000 (API), 9001 (console) | Object storage — file ảnh | `make minio` (minioadmin/minioadmin) |
| **mailhog** | 1025 (SMTP), 8025 (UI) | Bắt mail dev, KHÔNG gửi ra ngoài | `make mail` |
| **dbgate** | 3100 | UI xem Postgres + Redis | `make dbgate` |

## Redis đang dùng ở đâu

| Key | TTL | Vai trò |
|---|---|---|
| `otp:<email>` | 15 phút | Mã xác thực, dùng 1 lần, verify xong xóa |
| `resend-cooldown:<email>` | 60 giây | Chống spam resend (SETNX) |
| `refresh:<userId>` | 7 ngày | Refresh token hiện hành (rotation ghi đè) |

Vì sao Redis chứ không Postgres: TTL là tính năng native (Postgres phải tự viết job dọn), nhanh, mất cũng không sao.

Debug hay: `make redis` → `MONITOR` → thao tác API → thấy từng lệnh Redis realtime.

## Mailhog — SMTP giả cho dev

Mail đến Mailhog là DỪNG — không chuyển tiếp đến Gmail thật dù địa chỉ thật. Xem tại localhost:8025. Đây là chủ đích: test thoải mái không spam ai, không cần tài khoản SMTP.

Muốn gửi Gmail thật: set env `MAIL_HOST=smtp.gmail.com MAIL_PORT=587 MAIL_USERNAME=... MAIL_PASSWORD=<app-password> MAIL_SMTP_AUTH=true MAIL_STARTTLS=true` (cần App Password, bật 2FA). Code không đổi.

Pattern chung: **thay dịch vụ ngoài bằng bản giả local** — Mailhog thay SMTP, MinIO thay S3, Stripe test mode thay tiền thật.

## Health check `/actuator/health`

Trả lời "app còn sống và đủ điều kiện phục vụ không" — cho MÁY đọc: Docker healthcheck, load balancer (ngừng đẩy traffic vào instance DOWN), Kubernetes probes (giết pod/chặn traffic), CI/CD (deploy xong đợi UP mới coi là thành công), monitoring.

Tự check mọi dependency (db, redis, mail, disk) — 1 cái DOWN → tổng thể DOWN. Đã từng gặp: mail trỏ smtp.gmail.com không nối được → DOWN dù web vẫn chạy.

Mặc định giấu chi tiết components (tránh lộ hạ tầng); bật bằng `management.endpoint.health.show-details: always`.

## Lệnh Makefile

`make help` để xem tất cả. Nhớ chạy từ thư mục `marketplace/`. Port 8080 bị chiếm: `lsof -ti :8080 | xargs kill`.
