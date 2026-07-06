package com.marketplace.product.application.port;

import java.util.UUID;

/** Sổ ghi nhận đã xử lý giữ kho cho order nào — chốt idempotency của saga. */
public interface ReservationStore {

    boolean alreadyProcessed(UUID orderId);

    void saveReserved(UUID orderId);

    void saveRejected(UUID orderId, String reason);
}
