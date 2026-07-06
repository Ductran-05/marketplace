package com.marketplace.shared.messaging;

import java.time.Instant;
import java.util.UUID;

/** Contract trên topic "inventory.reserved" — product đã giữ hàng thành công cho order. */
public record InventoryReservedMessage(UUID orderId, Instant occurredAt) {}
