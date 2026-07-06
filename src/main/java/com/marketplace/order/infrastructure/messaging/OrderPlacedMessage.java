package com.marketplace.order.infrastructure.messaging;

import com.marketplace.order.domain.event.OrderPlacedEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Message contract trên Kafka — PHẲNG và ổn định.
 * Tách khỏi domain event: domain đổi cấu trúc không làm vỡ consumer bên ngoài.
 */
public record OrderPlacedMessage(
        UUID orderId,
        UUID buyerId,
        BigDecimal totalAmount,
        String currency,
        Instant occurredAt
) {
    public static OrderPlacedMessage from(OrderPlacedEvent event) {
        return new OrderPlacedMessage(
                event.orderId().value(),
                event.buyerId().value(),
                event.totalAmount().amount(),
                event.totalAmount().currency(),
                event.occurredAt()
        );
    }
}
