package com.marketplace.order.application.port;

import com.marketplace.shared.domain.Money;

import java.util.UUID;

/**
 * Cổng nhìn của order module sang product module.
 * Monolith: adapter gọi UseCase của product.
 * Microservices sau này: adapter gọi HTTP/gRPC — port này KHÔNG đổi.
 */
public interface ProductCatalog {

    ProductSnapshot getProduct(UUID productId);

    /** Trừ kho; ném BusinessException(INSUFFICIENT_STOCK) nếu không đủ. */
    void decreaseStock(UUID productId, int quantity);

    record ProductSnapshot(UUID id, String name, Money price) {}
}
