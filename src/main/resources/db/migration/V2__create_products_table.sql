CREATE TABLE products (
    id             UUID           NOT NULL PRIMARY KEY,
    seller_id      UUID           NOT NULL REFERENCES users (id),
    name           VARCHAR(255)   NOT NULL,
    description    TEXT,
    price_amount   NUMERIC(15, 2) NOT NULL,
    price_currency VARCHAR(3)     NOT NULL,
    stock_quantity INT            NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL,
    updated_at     TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_products_seller_id ON products (seller_id);
CREATE INDEX idx_products_created_at ON products (created_at DESC);
