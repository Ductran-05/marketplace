package com.marketplace.shared.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Contract trên topic "order.placed". Nằm ở shared vì nhiều module cùng đọc/ghi —
 * khi tách microservices, mỗi service sẽ giữ một bản copy của contract này.
 */
public record OrderPlacedMessage(
        UUID orderId,
        UUID buyerId,
        BigDecimal totalAmount,
        String currency,
        List<Item> items,
        Instant occurredAt
) {
    public record Item(UUID productId, int quantity) {}
}
