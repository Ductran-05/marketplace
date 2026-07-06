package com.marketplace.order.application;

import com.marketplace.order.application.command.PlaceOrderCommand;
import com.marketplace.order.application.port.ProductCatalog;
import com.marketplace.order.domain.model.BuyerId;
import com.marketplace.order.domain.model.Order;
import com.marketplace.order.domain.model.OrderId;
import com.marketplace.order.domain.model.OrderItem;
import com.marketplace.order.domain.repository.OrderRepository;
import com.marketplace.shared.domain.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductCatalog productCatalog;
    private final ApplicationEventPublisher eventPublisher;

    public PlaceOrderUseCase(OrderRepository orderRepository,
                             ProductCatalog productCatalog,
                             ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productCatalog = productCatalog;
        this.eventPublisher = eventPublisher;
    }

    /**
     * SAGA (choreography): use case này KHÔNG trừ kho.
     * Đơn sinh ra ở PENDING; product module nghe "order.placed" tự quyết định
     * reserve/reject; order nghe kết quả để confirm/cancel.
     */
    @Transactional
    public OrderId execute(PlaceOrderCommand command) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (PlaceOrderCommand.Item item : command.items()) {
            var product = productCatalog.getProduct(item.productId());   // NOT_FOUND nếu không có
            // Snapshot tên + giá tại thời điểm mua — seller đổi giá sau không ảnh hưởng đơn này
            orderItems.add(new OrderItem(product.id(), product.name(), product.price(), item.quantity()));
        }

        Order order = Order.place(new BuyerId(command.buyerId()), orderItems);
        orderRepository.save(order);

        List<DomainEvent> events = order.pullEvents();
        events.forEach(eventPublisher::publishEvent);   // → outbox (BEFORE_COMMIT) → Kafka

        return order.getId();
    }
}
