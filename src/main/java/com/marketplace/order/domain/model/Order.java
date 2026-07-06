package com.marketplace.order.domain.model;

import com.marketplace.order.domain.event.OrderConfirmedEvent;
import com.marketplace.order.domain.event.OrderPlacedEvent;
import com.marketplace.shared.domain.AggregateRoot;
import com.marketplace.shared.domain.Money;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot {

    private final OrderId id;
    private final BuyerId buyerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final Money totalAmount;
    private final Instant createdAt;
    private Instant updatedAt;

    private Order(OrderId id, BuyerId buyerId, List<OrderItem> items, OrderStatus status,
                  Money totalAmount, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.buyerId = buyerId;
        this.items = List.copyOf(items);
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Order place(BuyerId buyerId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        Money total = items.stream()
                .map(OrderItem::subtotal)
                .reduce(Money::add)
                .orElseThrow();

        Instant now = Instant.now();
        Order order = new Order(new OrderId(UUID.randomUUID()), buyerId, items,
                OrderStatus.PENDING, total, now, now);
        order.registerEvent(new OrderPlacedEvent(order.id, buyerId, total, order.items, now));
        return order;
    }

    public static Order reconstitute(OrderId id, BuyerId buyerId, List<OrderItem> items,
                                     OrderStatus status, Money totalAmount,
                                     Instant createdAt, Instant updatedAt) {
        return new Order(id, buyerId, items, status, totalAmount, createdAt, updatedAt);
    }

    public void confirm() {
        transition(OrderStatus.PENDING, OrderStatus.CONFIRMED);
        registerEvent(new OrderConfirmedEvent(id, buyerId, totalAmount, Instant.now()));
    }

    public void markPaid() {
        transition(OrderStatus.CONFIRMED, OrderStatus.PAID);
    }

    public void ship() {
        transition(OrderStatus.PAID, OrderStatus.SHIPPED);
    }

    public void cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel order in status " + status);
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    private void transition(OrderStatus expected, OrderStatus next) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Invalid transition: expected " + expected + " but order is " + status);
        }
        this.status = next;
        this.updatedAt = Instant.now();
    }

    public boolean isOwnedBy(UUID userId) {
        return buyerId.value().equals(userId);
    }

    public OrderId getId() { return id; }
    public BuyerId getBuyerId() { return buyerId; }
    public List<OrderItem> getItems() { return items; }
    public OrderStatus getStatus() { return status; }
    public Money getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
