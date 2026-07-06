CREATE TABLE orders (
    id             UUID           NOT NULL PRIMARY KEY,
    buyer_id       UUID           NOT NULL REFERENCES users (id),
    status         VARCHAR(20)    NOT NULL,
    total_amount   NUMERIC(15, 2) NOT NULL,
    total_currency VARCHAR(3)     NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL,
    updated_at     TIMESTAMPTZ    NOT NULL
);

CREATE TABLE order_items (
    id                  UUID           NOT NULL PRIMARY KEY,
    order_id            UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id          UUID           NOT NULL,
    product_name        VARCHAR(255)   NOT NULL,
    unit_price_amount   NUMERIC(15, 2) NOT NULL,
    unit_price_currency VARCHAR(3)     NOT NULL,
    quantity            INT            NOT NULL
);

CREATE INDEX idx_orders_buyer_id ON orders (buyer_id);
CREATE INDEX idx_orders_created_at ON orders (created_at DESC);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
