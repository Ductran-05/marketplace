package com.marketplace.order.domain.model;

import java.util.UUID;

public record OrderId(UUID value) {
    public OrderId {
        if (value == null) throw new IllegalArgumentException("OrderId cannot be null");
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
