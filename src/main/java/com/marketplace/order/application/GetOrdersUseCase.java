package com.marketplace.order.application;

import com.marketplace.order.domain.model.Order;
import com.marketplace.order.domain.model.OrderId;
import com.marketplace.order.domain.repository.OrderRepository;
import com.marketplace.shared.exception.BusinessException;
import com.marketplace.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetOrdersUseCase {

    private final OrderRepository orderRepository;

    public GetOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Order getById(UUID orderId, UUID requesterId) {
        Order order = orderRepository.findById(new OrderId(orderId))
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Order not found"));

        if (!order.isOwnedBy(requesterId)) {
            throw new BusinessException("NOT_ORDER_OWNER", "You can only view your own orders");
        }
        return order;
    }

    @Transactional(readOnly = true)
    public PageResult getMyOrders(UUID buyerId, int page, int size) {
        List<Order> orders = orderRepository.findPageByBuyer(buyerId, page, size);
        long total = orderRepository.countByBuyer(buyerId);
        return new PageResult(orders, page, size, total);
    }

    public record PageResult(List<Order> items, int page, int size, long totalItems) {
        public long totalPages() {
            return size == 0 ? 0 : (totalItems + size - 1) / size;
        }
    }
}
