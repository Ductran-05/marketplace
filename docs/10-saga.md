# Saga Pattern (choreography) — flow đặt hàng

## Vì sao chuyển từ transaction đồng bộ sang saga

Monolith cho phép trừ kho + tạo đơn trong 1 transaction (strong consistency). Nhưng khi tách microservices, order và product **không còn chung database** → không thể chung transaction → phải điều phối bằng event + bù trừ (compensation). Dự án chuyển sang saga NGAY TRONG monolith để: (1) học pattern, (2) khi tách service chỉ việc bê consumer đi, flow không đổi.

Nguyên tắc phân biệt (vẫn đúng): **event = chuyện ĐÃ xảy ra** (không phủ quyết được); việc "quyết định có được xảy ra không" giờ trở thành một BƯỚC riêng của saga với kết quả reserved/rejected.

## Flow

```
1. POST /orders → PlaceOrderUseCase: tạo đơn PENDING (KHÔNG đụng kho)
                  snapshot tên+giá (đọc đồng bộ qua port ProductCatalog — đọc vẫn OK)
                  outbox → Kafka "order.placed" (kèm items[productId, quantity])
                  → trả 201 ngay: "đã nhận đơn, đang xử lý"

2. product-service consumer nghe "order.placed" → ReserveStockUseCase:
   - IDEMPOTENT: check bảng stock_reservations(order_id PK) — message trùng thì bỏ qua
   - Validate TRƯỚC apply SAU: mọi item đủ hàng mới trừ, không trừ nửa vời
   - Kết quả ghi reservation + outbox (CÙNG transaction):
     đủ  → "inventory.reserved"
     thiếu → "inventory.rejected" (kèm lý do)

3. order-service consumer nghe kết quả:
   - reserved → ConfirmOrderUseCase: PENDING → CONFIRMED
                domain phát OrderConfirmedEvent → outbox → "order.confirmed"
   - rejected → CancelOrderUseCase: PENDING → CANCELLED  ← COMPENSATION

4. notification-service consumer nghe "order.confirmed"
   → tra email người mua (qua auth GetUserContactUseCase) → gửi mail xác nhận
```

## Topics

| Topic | Producer | Consumer (group) |
|---|---|---|
| `order.placed` | order (outbox) | product-service |
| `inventory.reserved` | product (outbox) | order-service |
| `inventory.rejected` | product (outbox) | order-service |
| `order.confirmed` | order (outbox) | notification-service |

Consumer nhận String, tự parse bằng ObjectMapper (payload outbox là JSON thô, không type header). Message contract đặt tại `shared/messaging/` — khi tách service, mỗi service copy contract riêng.

## Idempotency & khả năng chịu lỗi

- **at-least-once** từ outbox relay → mọi consumer phải idempotent:
  - product: bảng `stock_reservations` (order_id PK) — đã xử lý là bỏ qua
  - order: check status trước khi confirm/cancel; transition sai → log + skip (KHÔNG ném exception, tránh Kafka retry vô ích)
  - notification: có thể gửi mail trùng — chấp nhận được (có thể thêm dedupe Redis sau)
- Message không parse được (poison pill) → log + skip, không retry vô hạn
- Message format cũ (thiếu items) → skip có chủ đích

## Trade-off đã chấp nhận

- **Eventual consistency**: 201 trả về đơn PENDING; kết quả thật đến sau vài giây (client poll hoặc chờ thông báo)
- Cửa sổ "chưa biết": 2 người tranh món cuối → 1 người CANCELLED sau vài trăm ms thay vì bị chặn ngay
- Độ trễ saga ≈ 2 hop × (relay 2s + consumer) ≈ 4-8s trong dev

## Kịch bản đã test

1. Đủ hàng: PENDING → CONFIRMED, kho trừ đúng, mail "Order confirmed" tới Mailhog
2. Thiếu hàng: PENDING → CANCELLED, kho không đổi, reservation REJECTED kèm lý do
3. Replay message trùng qua console-producer: log "already processed — skipping", kho KHÔNG trừ lần 2

## Còn thiếu (bài tập tương lai)

- Release stock khi cancel đơn ĐÃ reserved (payment fail sau này) — cần event `order.cancelled` + product hoàn kho
- Lost update khi 2 reservation cạnh tranh cùng lúc (đọc-rồi-ghi không lock) → optimistic locking (@Version) hoặc SELECT FOR UPDATE
- Timeout saga: đơn kẹt PENDING nếu product-service chết lâu → job quét đơn PENDING quá hạn
