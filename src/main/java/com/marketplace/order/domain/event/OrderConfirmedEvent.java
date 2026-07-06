package com.marketplace.order.domain.event;

import com.marketplace.order.domain.model.BuyerId;
import com.marketplace.order.domain.model.OrderId;
import com.marketplace.shared.domain.DomainEvent;
import com.marketplace.shared.domain.Money;

import java.time.Instant;

public record OrderConfirmedEvent(
        OrderId orderId,
        BuyerId buyerId,
        Money totalAmount,
        Instant occurredAt
) implements DomainEvent {}
