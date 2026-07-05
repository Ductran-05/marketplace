package com.marketplace.product.domain.model;

import java.util.UUID;

public record SellerId(UUID value) {
    public SellerId {
        if (value == null) throw new IllegalArgumentException("SellerId cannot be null");
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
