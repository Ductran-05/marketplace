package com.marketplace.order.infrastructure.persistence;

import com.marketplace.order.domain.model.BuyerId;
import com.marketplace.order.domain.model.Order;
import com.marketplace.order.domain.model.OrderId;
import com.marketplace.order.domain.model.OrderItem;
import com.marketplace.order.domain.repository.OrderRepository;
import com.marketplace.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.marketplace.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.marketplace.order.infrastructure.persistence.repository.OrderJpaRepository;
import com.marketplace.shared.domain.Money;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryImpl(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        jpaRepository.save(toEntity(order));
        return order;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Order> findPageByBuyer(UUID buyerId, int page, int size) {
        return jpaRepository
                .findByBuyerId(buyerId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toDomain)
                .getContent();
    }

    @Override
    public long countByBuyer(UUID buyerId) {
        return jpaRepository.countByBuyerId(buyerId);
    }

    private OrderJpaEntity toEntity(Order order) {
        List<OrderItemJpaEntity> items = order.getItems().stream()
                .map(i -> new OrderItemJpaEntity(
                        UUID.randomUUID(),
                        i.productId(),
                        i.productName(),
                        i.unitPrice().amount(),
                        i.unitPrice().currency(),
                        i.quantity()))
                .toList();

        return new OrderJpaEntity(
                order.getId().value(),
                order.getBuyerId().value(),
                order.getStatus(),
                order.getTotalAmount().amount(),
                order.getTotalAmount().currency(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private Order toDomain(OrderJpaEntity e) {
        List<OrderItem> items = e.getItems().stream()
                .map(i -> new OrderItem(
                        i.getProductId(),
                        i.getProductName(),
                        new Money(i.getUnitPriceAmount(), i.getUnitPriceCurrency()),
                        i.getQuantity()))
                .toList();

        return Order.reconstitute(
                new OrderId(e.getId()),
                new BuyerId(e.getBuyerId()),
                items,
                e.getStatus(),
                new Money(e.getTotalAmount(), e.getTotalCurrency()),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
