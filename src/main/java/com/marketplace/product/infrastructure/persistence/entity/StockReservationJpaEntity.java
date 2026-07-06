package com.marketplace.product.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_reservations")
public class StockReservationJpaEntity {

    public static final String STATUS_RESERVED = "RESERVED";
    public static final String STATUS_REJECTED = "REJECTED";

    @Id
    @Column(columnDefinition = "uuid")
    private UUID orderId;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "text")
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected StockReservationJpaEntity() {}

    public StockReservationJpaEntity(UUID orderId, String status, String reason) {
        this.orderId = orderId;
        this.status = status;
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public UUID getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
