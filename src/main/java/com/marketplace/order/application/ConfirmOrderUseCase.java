package com.marketplace.order.application;

import com.marketplace.order.domain.model.Order;
import com.marketplace.order.domain.model.OrderId;
import com.marketplace.order.domain.model.OrderStatus;
import com.marketplace.order.domain.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Bước 3a của saga: inventory.reserved → PENDING → CONFIRMED.
 * Idempotent + chịu được message trùng/trễ: trạng thái không hợp lệ thì log và bỏ qua,
 * KHÔNG ném exception (ném sẽ khiến Kafka retry vô ích).
 */
@Service
public class ConfirmOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfirmOrderUseCase.class);

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ConfirmOrderUseCase(OrderRepository orderRepository, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void execute(UUID orderId) {
        Optional<Order> found = orderRepository.findById(new OrderId(orderId));
        if (found.isEmpty()) {
            log.warn("inventory.reserved for unknown order {} — skipping", orderId);
            return;
        }
        Order order = found.get();

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            return;   // message trùng — đã confirm rồi
        }

        try {
            order.confirm();   // domain phát OrderConfirmedEvent
        } catch (IllegalStateException e) {
            log.warn("Cannot confirm order {}: {} — skipping", orderId, e.getMessage());
            return;
        }
        orderRepository.save(order);
        order.pullEvents().forEach(eventPublisher::publishEvent);   // → outbox → order.confirmed
    }
}
