package com.marketplace.shared.messaging;

import java.time.Instant;
import java.util.UUID;

/** Contract trên topic "inventory.rejected" — product từ chối giữ hàng (thiếu kho / không tồn tại). */
public record InventoryRejectedMessage(UUID orderId, String reason, Instant occurredAt) {}
