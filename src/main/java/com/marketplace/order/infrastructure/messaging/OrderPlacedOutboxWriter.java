package com.marketplace.order.infrastructure.messaging;

import com.marketplace.order.domain.event.OrderPlacedEvent;
import com.marketplace.shared.config.KafkaTopicsConfig;
import com.marketplace.shared.outbox.OutboxAppender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Outbox pattern (thay cho naive Kafka publisher của Đợt 2):
 *
 * BEFORE_COMMIT — chạy ĐỒNG BỘ bên trong transaction của PlaceOrderUseCase,
 * nên dòng outbox và đơn hàng nằm chung 1 commit: cùng tồn tại hoặc cùng biến mất.
 * Không còn khoảng hở "commit xong crash là mất event".
 *
 * Việc gửi Kafka thật do OutboxRelay (job nền) đảm nhiệm — at-least-once.
 */
@Component
public class OrderPlacedOutboxWriter {

    private final OutboxAppender outboxAppender;

    public OrderPlacedOutboxWriter(OutboxAppender outboxAppender) {
        this.outboxAppender = outboxAppender;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(OrderPlacedEvent event) {
        outboxAppender.append(
                KafkaTopicsConfig.TOPIC_ORDER_PLACED,
                event.orderId().toString(),
                OrderPlacedMessage.from(event)
        );
    }
}
