package com.marketplace.order.application.port;

import com.marketplace.shared.domain.Money;

import java.util.UUID;

/**
 * Cổng nhìn của order module sang product module.
 * Monolith: adapter gọi UseCase của product.
 * Microservices sau này: adapter gọi HTTP/gRPC — port này KHÔNG đổi.
 */
public interface ProductCatalog {

    /** Đọc snapshot thông tin sản phẩm. Việc GIỮ KHO đi qua saga (Kafka), không qua port này. */
    ProductSnapshot getProduct(UUID productId);

    record ProductSnapshot(UUID id, String name, Money price) {}
}
