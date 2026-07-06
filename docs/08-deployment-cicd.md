# Deployment & CI/CD

## Deploy = trả lời "mỗi mảnh hạ tầng sống ở đâu?"

### Con đường 1: VPS + Docker Compose (học nhiều nhất)
Thuê VPS (~$6-12/tháng) → bê docker-compose lên (bỏ Mailhog/DbGate, không expose port DB/Redis ra ngoài) → app chạy từ image trong registry → nginx/caddy làm reverse proxy + HTTPS.

### Con đường 2: PaaS + managed services (nhanh sống nhất — nên làm trước)

| Local | Production managed |
|---|---|
| App | Railway / Render / Fly.io |
| Postgres | Neon / Supabase / Railway |
| Redis | Upstash |
| MinIO | Cloudflare R2 (S3-compatible, free 10GB) / AWS S3 |
| Mailhog | Brevo / Resend / SendGrid |
| Kafka | Upstash Kafka (khó + đắt nhất, có thể bỏ ở bản đầu) |

## Độ khó thay thế từng mảnh

| Mảnh | Độ khó | Ghi chú |
|---|---|---|
| Postgres, Redis, SMTP | ⭐ | Đổi env; bẫy duy nhất: managed bắt TLS (`sslmode=require`, `rediss://`) |
| MinIO → R2/S3 | ⭐⭐ | Đổi env + có thể 1-3 dòng adapter (region, path-style); chỉ đụng 1 file nhờ port |
| Kafka → managed | ⭐⭐⭐ | Thêm cụm config SASL/TLS; free tier hạn chế |

**Code ĐÃ SẴN SÀNG**: mọi kết nối qua env var có default local (`${MINIO_ENDPOINT:http://localhost:9000}`) — deploy không sửa dòng Java nào. "Thay dịch vụ" ≠ "chuyển dữ liệu" (cái sau cần pg_dump, mc mirror riêng).

### Bẫy production
- `JWT_SECRET` phải thay bằng secret thật (≥256 bit, không commit)
- Presigned URL phải trỏ domain public (không phải localhost:9000)
- CORS khi có frontend khác domain

## CI/CD nên có gì

Nguyên tắc: **CI trả lời "code có tốt không?"** (mọi push), **CD trả lời "đưa lên server thế nào?"**.

### CI (GitHub Actions)
1. **Build + unit test**: `./mvnw verify` — hàng rào đầu tiên
2. **Integration test**: Testcontainers — bật Postgres/Redis thật trong Docker ngay trong CI, không mock
3. Code quality: Spotless/Checkstyle, JaCoCo coverage
4. Security: Trivy (CVE trong dependencies), Gitleaks (chặn lộ secret)
5. Merge main → build Docker image (multi-stage) → push ghcr.io, tag = git SHA

### CD
- Mức 1: merge main + CI xanh → Railway/Render tự pull & deploy
- Mức 2: SSH vào VPS → `docker compose pull && up -d`; rollback = deploy lại tag cũ
- Chưa cần: Kubernetes, ArgoCD, canary — để phase microservices

### Thứ tự triển khai
```
1. Viết unit test cho UseCase + domain   ← CI không có test = máy compile thuê
2. Dockerfile multi-stage
3. Push GitHub + workflow CI (build + test)
4. Testcontainers integration test
5. Deploy PaaS + đổi env sang managed services
```

Điểm nghẽn hiện tại: chưa có test nào, repo chưa lên GitHub.
