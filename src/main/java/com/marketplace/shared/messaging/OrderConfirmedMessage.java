package com.marketplace.shared.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Contract trên topic "order.confirmed" — saga kết thúc thành công. */
public record OrderConfirmedMessage(
        UUID orderId,
        UUID buyerId,
        BigDecimal totalAmount,
        String currency,
        Instant occurredAt
) {}
