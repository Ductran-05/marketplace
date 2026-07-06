package com.marketplace.order.infrastructure.messaging;

import com.marketplace.order.domain.event.OrderConfirmedEvent;
import com.marketplace.order.domain.event.OrderPlacedEvent;
import com.marketplace.shared.config.KafkaTopicsConfig;
import com.marketplace.shared.messaging.OrderConfirmedMessage;
import com.marketplace.shared.messaging.OrderPlacedMessage;
import com.marketplace.shared.outbox.OutboxAppender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * BEFORE_COMMIT: dòng outbox ghi chung transaction với thay đổi của order —
 * event không bao giờ mất. OutboxRelay lo việc gửi Kafka (at-least-once).
 */
@Component
public class OrderOutboxWriter {

    private final OutboxAppender outboxAppender;

    public OrderOutboxWriter(OutboxAppender outboxAppender) {
        this.outboxAppender = outboxAppender;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(OrderPlacedEvent event) {
        var message = new OrderPlacedMessage(
                event.orderId().value(),
                event.buyerId().value(),
                event.totalAmount().amount(),
                event.totalAmount().currency(),
                event.items().stream()
                        .map(i -> new OrderPlacedMessage.Item(i.productId(), i.quantity()))
                        .toList(),
                event.occurredAt()
        );
        outboxAppender.append(KafkaTopicsConfig.TOPIC_ORDER_PLACED, event.orderId().toString(), message);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(OrderConfirmedEvent event) {
        var message = new OrderConfirmedMessage(
                event.orderId().value(),
                event.buyerId().value(),
                event.totalAmount().amount(),
                event.totalAmount().currency(),
                event.occurredAt()
        );
        outboxAppender.append(KafkaTopicsConfig.TOPIC_ORDER_CONFIRMED, event.orderId().toString(), message);
    }
}
