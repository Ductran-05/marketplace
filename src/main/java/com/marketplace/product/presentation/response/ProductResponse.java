package com.marketplace.product.presentation.response;

import com.marketplace.product.domain.model.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID sellerId,
        String name,
        String description,
        BigDecimal price,
        String currency,
        int stockQuantity,
        String imageUrl,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(Product p, String imageUrl) {
        return new ProductResponse(
                p.getId().value(),
                p.getSellerId().value(),
                p.getName(),
                p.getDescription(),
                p.getPrice().amount(),
                p.getPrice().currency(),
                p.getStockQuantity(),
                imageUrl,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
