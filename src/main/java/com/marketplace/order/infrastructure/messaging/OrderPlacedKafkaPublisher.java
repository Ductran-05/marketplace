package com.marketplace.order.infrastructure.messaging;

import com.marketplace.order.domain.event.OrderPlacedEvent;
import com.marketplace.shared.config.KafkaTopicsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridge: Spring domain event → Kafka topic.
 *
 * LƯU Ý (chủ đích của Đợt 2): đây là producer "ngây thơ" — nếu app crash
 * giữa lúc commit DB và lúc gửi Kafka, event MẤT vĩnh viễn.
 * Đợt 3 sẽ thay bằng Outbox pattern để đóng khoảng hở này.
 */
@Component
public class OrderPlacedKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedKafkaPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderPlacedKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    @TransactionalEventListener
    public void handle(OrderPlacedEvent event) {
        OrderPlacedMessage message = OrderPlacedMessage.from(event);
        String key = message.orderId().toString();   // cùng key → cùng partition → giữ thứ tự theo order

        kafkaTemplate.send(KafkaTopicsConfig.TOPIC_ORDER_PLACED, key, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order.placed for order {}", key, ex);
                    } else {
                        log.info("Published order.placed for order {} to partition {} offset {}",
                                key,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
