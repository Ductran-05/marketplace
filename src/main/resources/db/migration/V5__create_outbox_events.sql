CREATE TABLE outbox_events (
    id           UUID         NOT NULL PRIMARY KEY,
    topic        VARCHAR(255) NOT NULL,
    message_key  VARCHAR(255) NOT NULL,
    payload      TEXT         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    processed_at TIMESTAMPTZ
);

-- Relay quét event chưa xử lý theo thứ tự tạo
CREATE INDEX idx_outbox_unprocessed ON outbox_events (created_at) WHERE processed_at IS NULL;
