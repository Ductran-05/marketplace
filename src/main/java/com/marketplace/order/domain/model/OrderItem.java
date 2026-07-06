package com.marketplace.order.domain.model;

import com.marketplace.shared.domain.Money;

import java.util.UUID;

/**
 * Value Object bên trong Order aggregate.
 * productName và unitPrice là SNAPSHOT tại thời điểm đặt hàng —
 * seller đổi giá/tên sau đó không ảnh hưởng đơn đã đặt.
 */
public record OrderItem(UUID productId, String productName, Money unitPrice, int quantity) {

    public OrderItem {
        if (productId == null) throw new IllegalArgumentException("ProductId is required");
        if (productName == null || productName.isBlank()) throw new IllegalArgumentException("Product name is required");
        if (unitPrice == null) throw new IllegalArgumentException("Unit price is required");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }
}
