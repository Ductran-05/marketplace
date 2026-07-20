# Frontend — React SPA

## Mục tiêu

Backend đã đủ auth + product để test end-to-end nhưng chỉ qua Bruno/Swagger. Thêm 1 SPA React ở `/frontend` (cùng repo, monorepo) để vừa học React vừa có UI thật thao tác với API — v1 gồm auth flow (register/verify/login) và product listing/CRUD. v1.1 thêm flow đặt hàng (mua ngay + xem đơn) gọi vào order module (saga) đã có sẵn ở backend.

## Stack & lý do tối giản

Vite + React + TypeScript, chỉ thêm 2 dependency: `react-router-dom` (routing) và `axios` (cần interceptor thật để tự refresh token — viết tay trên `fetch` sẽ dư code hơn). KHÔNG thêm TanStack Query/Redux/UI kit/react-hook-form — app nhỏ (~7 endpoint đọc, 1 state dùng chung là auth), thêm vào là abstraction thừa.

Cấu trúc `frontend/src` **phẳng, KHÔNG mirror Clean Architecture của backend** (`api/`, `auth/`, `pages/`, `components/`, `types/`) — Clean Architecture giải quyết vấn đề của backend lớn nhiều người maintain lâu dài; SPA nhỏ 1 người học thì chỉ cần tách theo chức năng, tách layer thêm là overkill.

## Vì sao không cần sửa CORS backend

`SecurityConfig.java` không có `CorsConfigurationSource` — gọi thẳng từ `localhost:5173` sẽ bị trình duyệt chặn CORS. Thay vì sửa backend, dùng **Vite dev proxy** (`vite.config.ts` → `server.proxy['/api']` trỏ `http://localhost:8080`): browser thấy cùng origin (`:5173`), Vite tự forward request sang backend. Chỉ hoạt động với `npm run dev`; khi build production thật sẽ cần CORS thật hoặc reverse proxy (Nginx) — để sau.

## Quản lý token

- Lưu `accessToken`/`refreshToken` trong `localStorage` (`api/tokenStore.ts`). Đánh đổi: dễ bị đánh cắp qua XSS so với cookie httpOnly — chấp nhận được vì app không render HTML từ input người dùng (`dangerouslySetInnerHTML` không dùng ở đâu) và đổi sang cookie sẽ cần sửa backend (set-cookie, CSRF) — ngoài phạm vi v1.
- `AuthContext` lúc mount: nếu có token → gọi `GET /auth/me` để xác thực + lấy `{userId, email, role}` (login/refresh KHÔNG trả user info, chỉ trả token).
- Axios response interceptor (`api/client.ts`) bắt lỗi và tự refresh 1 lần rồi retry request gốc.

## Phát hiện quan trọng: backend chỉ trả 403, không có 401

`SecurityConfig` không đăng ký `AuthenticationEntryPoint` tùy chỉnh → Spring Security mặc định trả **403** cho cả 2 trường hợp: chưa đăng nhập/token hết hạn VÀ đúng token nhưng sai role (`@PreAuthorize` fail). Không có cách phân biệt qua status code.

Hệ quả: interceptor phải refresh khi gặp 403 (không phải 401 như convention thông thường). Khi 1 BUYER gọi API SELLER-only, sẽ tốn 1 lần refresh+retry lãng phí trước khi thất bại lại với 403 — chấp nhận được cho learning project, không sửa backend vì ngoài phạm vi v1 (fix thật sự cần backend phân biệt 401/403).

## Flow become-seller → refresh

`POST /auth/become-seller` không cập nhật role trong access token đang dùng. UI phải gọi tiếp `POST /auth/refresh` (dùng refresh token cũ) để lấy token mang role mới, rồi `GET /auth/me` để cập nhật context — `AuthContext.becomeSellerAndRefresh()` làm cả chuỗi này, UI tự re-render không cần logout/login lại.

## Giới hạn v1

- Không có trang "sản phẩm của tôi" riêng — backend không có endpoint đó. Seller quản lý sản phẩm qua trang public list/detail, nút Sửa/Xóa chỉ hiện khi `product.sellerId === user.userId`.
- Không có test tự động (Vitest/Testing Library) — phù hợp tinh thần tối giản của learning project; cân nhắc thêm sau nếu app lớn hơn.

## Flow đặt hàng (v1.1) — "Mua ngay" thay vì giỏ hàng

Order module backend hỗ trợ đơn nhiều sản phẩm (`items[]`) nhưng frontend chỉ làm nút **"Mua ngay"** trên trang chi tiết sản phẩm (1 sản phẩm/đơn, chọn số lượng) — vẫn gọi đúng contract `items[]` nên thêm giỏ hàng sau này không cần đổi API, chỉ đổi UI. Quyết định vì giỏ hàng đầy đủ (state riêng, trang checkout nhiều dòng) là khối lượng công việc khác hẳn, không cần thiết cho mục tiêu học tập hiện tại.

**Không có nút hủy đơn**: backend không có endpoint cho buyer tự hủy — `CANCELLED` chỉ do saga tự set khi thiếu kho (`inventory.rejected` → `CancelOrderUseCase`, xem `docs/10-saga.md`). Không tự thêm UI cho việc backend chưa hỗ trợ.

**Polling thay vì push**: `POST /orders` trả `201` ngay với đơn ở trạng thái `PENDING` — kết quả thật (CONFIRMED/CANCELLED) đến sau vài giây qua saga (Kafka), không có WebSocket/SSE nào expose ra frontend. `OrderDetailPage` tự poll `GET /orders/:id` mỗi 2s (khớp tick của outbox relay) khi status còn `PENDING`, tối đa 15 lần (~30s) rồi dừng và báo "xử lý lâu hơn dự kiến" thay vì poll vô hạn.

## Chạy

```bash
make frontend   # http://localhost:5173, tự npm install nếu chưa có node_modules
```

Cần backend chạy song song (`make run` hoặc `./mvnw spring-boot:run`, cổng 8080) và infra (`make up`).

## Lộ trình sau

- CORS thật/reverse proxy khi deploy production (Vite proxy chỉ chạy ở dev).
- TanStack Query nếu sau này cache/invalidate phức tạp hơn.
- Cookie httpOnly cho token nếu cần bảo mật production thật (cần sửa backend).
- Trang "sản phẩm của tôi" nếu backend thêm endpoint tương ứng.
- Giỏ hàng đầy đủ (nhiều sản phẩm/đơn) nếu cần — contract `items[]` đã sẵn sàng, chỉ cần đổi UI.
- Nút hủy đơn cho buyer nếu backend thêm endpoint tương ứng.
