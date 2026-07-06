package com.marketplace.order.domain.model;

import java.util.UUID;

public record BuyerId(UUID value) {
    public BuyerId {
        if (value == null) throw new IllegalArgumentException("BuyerId cannot be null");
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
