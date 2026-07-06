package com.marketplace.order.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItemJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPriceAmount;

    @Column(nullable = false, length = 3)
    private String unitPriceCurrency;

    @Column(nullable = false)
    private int quantity;

    protected OrderItemJpaEntity() {}

    public OrderItemJpaEntity(UUID id, UUID productId, String productName,
                              BigDecimal unitPriceAmount, String unitPriceCurrency, int quantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.unitPriceAmount = unitPriceAmount;
        this.unitPriceCurrency = unitPriceCurrency;
        this.quantity = quantity;
    }

    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPriceAmount() { return unitPriceAmount; }
    public String getUnitPriceCurrency() { return unitPriceCurrency; }
    public int getQuantity() { return quantity; }
}
