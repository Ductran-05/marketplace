# MinIO — Object Storage

## MinIO là gì

Object storage server mã nguồn mở, **nói cùng API với Amazon S3** — "S3 mini tự host". Giống DB ở vai trò (server độc lập trong Docker, dữ liệu bền qua volume), khác ở bản chất:

| | PostgreSQL | MinIO |
|---|---|---|
| Lưu gì | Hàng/cột có cấu trúc | **Blob** — file nguyên khối (ảnh, video, pdf) |
| Truy vấn | WHERE/JOIN/GROUP BY | Chỉ get/put/delete **theo key** |
| Giao thức | SQL | HTTP (chuẩn S3) |

Vì thế kiến trúc chia đôi: **file nằm MinIO, con trỏ (image_key) nằm Postgres**. Nhét ảnh vào DB (bytea) = antipattern: DB phình, backup chậm.

Học MinIO = học S3 luôn: đổi env endpoint + credentials là code chạy với AWS S3/Cloudflare R2 thật.

## Cấu trúc trong dự án

- Bucket `product-images`, tự tạo lúc khởi động (`MinioImageStorage.ensureBucketExists`)
- Key: `products/{productId}/{uuid}.{ext}` — key trông như path nhưng chỉ là chuỗi
- Chỉ nhận JPEG/PNG/WebP, max 5MB (multipart config)
- Console: `make minio` → localhost:9001 (minioadmin/minioadmin) — duyệt file như file manager
- Dữ liệu vật lý: Docker volume `minio_data`

## Flow upload (2 bước: tạo trước, gắn ảnh sau)

```
POST /products            → tạo product, imageUrl=null, script Bruno lưu productId
POST /products/{id}/image → multipart "file"
    1. check chủ sản phẩm + loại ảnh
    2. sinh key mới → SDK đẩy file vào MinIO
    3. DB ghi image_key; ảnh cũ xóa SAU khi DB trỏ ảnh mới
       (xóa fail chỉ để lại rác, không bao giờ mất ảnh đang dùng)
```

## Flow đọc — Presigned URL

```
GET /products/{id} → app lấy image_key từ DB → KÝ một URL mới (HMAC, hạn 15')
Client ──GET thẳng MinIO bằng URL đó──► ảnh (app không gánh băng thông đọc)
```

- Presigned URL không lưu ở đâu — sinh mới mỗi lần GET (tính toán local, không gọi mạng)
- Bucket private; URL hết hạn sau 15 phút
- Nâng cao (chưa làm): **presigned PUT** — client upload thẳng lên storage, app chỉ cấp URL, không gánh cả băng thông ghi

## Test upload

- **Bruno**: request `products/06 Upload` — tab Body có UI chọn file (ảnh mẫu `bruno/assets/sample.png`)
- **Swagger**: endpoint MultipartFile tự hiện nút Choose File
- **curl**: `curl -F "file=@anh.jpg" -H "Authorization: Bearer $TOKEN" .../products/{id}/image`
- REST Client (.http): cú pháp `< ./path/file.jpg` trong body multipart (thô hơn)
