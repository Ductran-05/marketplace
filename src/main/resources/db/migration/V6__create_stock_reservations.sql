-- Idempotency cho saga: mỗi order chỉ được xử lý giữ kho 1 lần,
-- kể cả khi message order.placed đến trùng lặp (at-least-once)
CREATE TABLE stock_reservations (
    order_id   UUID        NOT NULL PRIMARY KEY,
    status     VARCHAR(20) NOT NULL,  -- RESERVED / REJECTED
    reason     TEXT,
    created_at TIMESTAMPTZ NOT NULL
);
