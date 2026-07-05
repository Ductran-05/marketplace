package com.marketplace.product.domain.model;

import com.marketplace.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class Product extends AggregateRoot {

    private final ProductId id;
    private final SellerId sellerId;
    private String name;
    private String description;
    private Money price;
    private int stockQuantity;
    private final Instant createdAt;
    private Instant updatedAt;

    private Product(ProductId id, SellerId sellerId, String name, String description,
                    Money price, int stockQuantity, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Product create(SellerId sellerId, String name, String description,
                                 Money price, int stockQuantity) {
        validateName(name);
        validateStock(stockQuantity);
        Instant now = Instant.now();
        return new Product(new ProductId(UUID.randomUUID()), sellerId,
                name, description, price, stockQuantity, now, now);
    }

    public static Product reconstitute(ProductId id, SellerId sellerId, String name, String description,
                                       Money price, int stockQuantity, Instant createdAt, Instant updatedAt) {
        return new Product(id, sellerId, name, description, price, stockQuantity, createdAt, updatedAt);
    }

    public void update(String name, String description, Money price, int stockQuantity) {
        validateName(name);
        validateStock(stockQuantity);
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.updatedAt = Instant.now();
    }

    public boolean isOwnedBy(UUID userId) {
        return sellerId.value().equals(userId);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
    }

    private static void validateStock(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
    }

    public ProductId getId() { return id; }
    public SellerId getSellerId() { return sellerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Money getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
