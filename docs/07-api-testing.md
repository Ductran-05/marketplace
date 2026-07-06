# Test API: Swagger + Bruno

## Phân vai (2 công cụ độc lập, không nói chuyện với nhau)

| | Swagger | Bruno |
|---|---|---|
| Bản chất | **Tài liệu tự sinh từ code** (springdoc scan annotation) | **Kịch bản test viết tay**, file `.bru` trong git |
| Thêm endpoint | Tự xuất hiện | Phải thêm file (quy ước: Claude tự cập nhật) |
| Token | Nút Authorize — dán tay, persist qua refresh | Script post-response TỰ LƯU vào environment |
| Hợp với | "API này có gì? Schema thế nào?" | Test flow hằng ngày |

Swagger là bản đồ, Bruno là con đường mòn đi hằng ngày. `api.http` (REST Client) đã bỏ vì trùng vai với Bruno.

## Bruno

- Collection: thư mục `bruno/` — mỗi request 1 file `.bru`, UI và file là một (sửa UI = sửa file)
- **Environment** (dropdown góc phải trên, chọn `local`): chứa host, email, password, accessToken, refreshToken
- `{{host}}` trong URL = biến, thay giá trị lúc Send — đổi environment là cả collection trỏ server khác
- Login có script: `bru.setEnvVar("accessToken", res.body.accessToken)` — token tự chảy sang request sau qua `{{accessToken}}`
- `vars:pre-request` trong 1 request sẽ GHI ĐÈ biến environment — cẩn thận khi hardcode productId
- Thứ tự chạy: 04 Login → (08 Become Seller → 06 Refresh nếu cần SELLER) → products/*

## Cơ chế token trong REST Client (nếu quay lại dùng)

Response của request có `# @name login` được lưu vào bộ nhớ phiên VSCode; request khác tham chiếu tường minh `{{login.response.body.accessToken}}`. KHÔNG tự gắn toàn cục — request mới phải tự viết dòng Authorization. Mất khi đóng VSCode.

## OpenAPI spec & contract-first

- App xuất spec máy-đọc-được tại `/v3/api-docs` — Postman/Bruno/Insomnia import được để tự sinh collection (nhưng chỉ là danh sách endpoint rời, không có kịch bản chuyền token)
- **Code-first** (dự án này): code → spec. **Contract-first**: viết `openapi.yaml` trước → generator sinh code/SDK — hợp khi nhiều team làm song song
- Token tự động hay không KHÔNG phụ thuộc contract-first, mà phụ thuộc **securityScheme**: `http bearer` (JWT tự chế) = dán tay; `oauth2 flows` (Keycloak/Auth0) = Swagger UI tự popup login lấy token — sẽ gặp khi tích hợp OAuth2/OIDC
- UI render spec viết tay: Swagger Editor, **Scalar** (đẹp, có client test), Redoc (chỉ đọc)
