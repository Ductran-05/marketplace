package com.marketplace.product.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductCommand(
        UUID productId,
        UUID requesterId,
        String name,
        String description,
        BigDecimal price,
        String currency,
        int stockQuantity
) {}
