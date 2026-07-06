package com.marketplace.product.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.product.application.ReserveStockUseCase;
import com.marketplace.shared.config.KafkaTopicsConfig;
import com.marketplace.shared.messaging.OrderPlacedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedConsumer.class);

    private final ReserveStockUseCase reserveStockUseCase;
    private final ObjectMapper objectMapper;

    public OrderPlacedConsumer(ReserveStockUseCase reserveStockUseCase, ObjectMapper objectMapper) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopicsConfig.TOPIC_ORDER_PLACED, groupId = "product-service")
    public void handle(String payload) {
        OrderPlacedMessage message;
        try {
            message = objectMapper.readValue(payload, OrderPlacedMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Unparseable message on order.placed, skipping: {}", payload, e);
            return;   // poison pill — bỏ qua thay vì retry vô hạn
        }

        if (message.items() == null || message.items().isEmpty()) {
            log.warn("order.placed without items (old format?) — skipping order {}", message.orderId());
            return;
        }

        reserveStockUseCase.execute(message.orderId(), message.items());
    }
}
