package com.marketplace.order.application;

import com.marketplace.order.domain.model.Order;
import com.marketplace.order.domain.model.OrderId;
import com.marketplace.order.domain.model.OrderStatus;
import com.marketplace.order.domain.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Bước 3b của saga (compensation): inventory.rejected → PENDING → CANCELLED.
 */
@Service
public class CancelOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelOrderUseCase.class);

    private final OrderRepository orderRepository;

    public CancelOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void execute(UUID orderId, String reason) {
        Optional<Order> found = orderRepository.findById(new OrderId(orderId));
        if (found.isEmpty()) {
            log.warn("inventory.rejected for unknown order {} — skipping", orderId);
            return;
        }
        Order order = found.get();

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;   // message trùng — đã cancel rồi
        }

        try {
            order.cancel();
        } catch (IllegalStateException e) {
            log.warn("Cannot cancel order {}: {} — skipping", orderId, e.getMessage());
            return;
        }
        orderRepository.save(order);
        log.info("Order {} cancelled: {}", orderId, reason);
    }
}
