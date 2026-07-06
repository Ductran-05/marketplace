package com.marketplace.order.presentation.response;

import com.marketplace.order.domain.model.Order;
import com.marketplace.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID buyerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        List<Item> items,
        Instant createdAt
) {
    public record Item(UUID productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {}

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId().value(),
                order.getBuyerId().value(),
                order.getStatus(),
                order.getTotalAmount().amount(),
                order.getTotalAmount().currency(),
                order.getItems().stream()
                        .map(i -> new Item(i.productId(), i.productName(),
                                i.unitPrice().amount(), i.quantity(), i.subtotal().amount()))
                        .toList(),
                order.getCreatedAt()
        );
    }
}
