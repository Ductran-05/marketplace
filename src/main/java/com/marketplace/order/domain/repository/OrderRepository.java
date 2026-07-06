package com.marketplace.order.domain.repository;

import com.marketplace.order.domain.model.Order;
import com.marketplace.order.domain.model.OrderId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    List<Order> findPageByBuyer(UUID buyerId, int page, int size);
    long countByBuyer(UUID buyerId);
}
