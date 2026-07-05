package com.marketplace.product.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductCommand(
        UUID sellerId,
        String name,
        String description,
        BigDecimal price,
        String currency,
        int stockQuantity
) {}
