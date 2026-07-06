package com.marketplace.order.application.command;

import java.util.List;
import java.util.UUID;

public record PlaceOrderCommand(UUID buyerId, List<Item> items) {
    public record Item(UUID productId, int quantity) {}
}
