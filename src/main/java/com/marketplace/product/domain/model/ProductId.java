package com.marketplace.product.domain.model;

import java.util.UUID;

public record ProductId(UUID value) {
    public ProductId {
        if (value == null) throw new IllegalArgumentException("ProductId cannot be null");
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
