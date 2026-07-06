# ERD Database

Bản trực quan: https://claude.ai/code/artifact/cc5f0e82-10f2-4b74-bfe3-07f3bde37483

```mermaid
erDiagram
    users ||--o{ products : "seller_id (FK)"
    users ||--o{ orders : "buyer_id (FK)"
    orders ||--|{ order_items : "order_id (FK, ON DELETE CASCADE)"
    products ||..o{ order_items : "product_id (logic, KHONG co FK - snapshot)"

    users {
        uuid id PK
        varchar_255 email UK
        varchar_255 password_hash
        varchar_100 full_name
        varchar_20 role
        varchar_30 status
        timestamptz created_at
    }

    products {
        uuid id PK
        uuid seller_id FK
        varchar_255 name
        text description
        numeric_15_2 price_amount
        varchar_3 price_currency
        int stock_quantity
        varchar_500 image_key "nullable - key trong MinIO"
        timestamptz created_at
        timestamptz updated_at
    }

    orders {
        uuid id PK
        uuid buyer_id FK
        varchar_20 status "PENDING/CONFIRMED/PAID/SHIPPED/CANCELLED"
        numeric_15_2 total_amount
        varchar_3 total_currency
        timestamptz created_at
        timestamptz updated_at
    }

    order_items {
        uuid id PK
        uuid order_id FK
        uuid product_id "ref logic, khong FK"
        varchar_255 product_name "snapshot luc mua"
        numeric_15_2 unit_price_amount "snapshot luc mua"
        varchar_3 unit_price_currency
        int quantity
    }
```

## Quyết định thiết kế

1. **`order_items.product_id` cố ý KHÔNG có FK** — đơn hàng lưu snapshot (tên + giá lúc mua); seller đổi giá/xóa sản phẩm không được ảnh hưởng đơn đã đặt. FK cứng sẽ chặn xóa product.
2. **`order_items.order_id` có ON DELETE CASCADE** — item là thành phần trong Order aggregate, không có đời sống riêng.
3. **Tiền = cặp cột** `*_amount numeric(15,2)` + `*_currency varchar(3)` — ánh xạ VO `Money`. Không dùng float cho tiền.
4. **`image_key` nullable** — con trỏ tới object MinIO, file thật không nằm trong DB.
5. **Enum lưu dạng chữ** — đọc bằng mắt hiểu ngay, thêm giá trị không cần migrate.
6. **Index**: users(email), products(seller_id), products(created_at DESC), orders(buyer_id), orders(created_at DESC), order_items(order_id).
7. Bảng `flyway_schema_history` do Flyway tự quản — không đụng vào.
