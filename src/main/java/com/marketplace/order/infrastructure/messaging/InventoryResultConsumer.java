package com.marketplace.order.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.order.application.CancelOrderUseCase;
import com.marketplace.order.application.ConfirmOrderUseCase;
import com.marketplace.shared.config.KafkaTopicsConfig;
import com.marketplace.shared.messaging.InventoryRejectedMessage;
import com.marketplace.shared.messaging.InventoryReservedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryResultConsumer.class);

    private final ConfirmOrderUseCase confirmOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final ObjectMapper objectMapper;

    public InventoryResultConsumer(ConfirmOrderUseCase confirmOrderUseCase,
                                   CancelOrderUseCase cancelOrderUseCase,
                                   ObjectMapper objectMapper) {
        this.confirmOrderUseCase = confirmOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopicsConfig.TOPIC_INVENTORY_RESERVED, groupId = "order-service")
    public void onReserved(String payload) {
        try {
            InventoryReservedMessage message = objectMapper.readValue(payload, InventoryReservedMessage.class);
            confirmOrderUseCase.execute(message.orderId());
        } catch (JsonProcessingException e) {
            log.error("Unparseable message on inventory.reserved, skipping: {}", payload, e);
        }
    }

    @KafkaListener(topics = KafkaTopicsConfig.TOPIC_INVENTORY_REJECTED, groupId = "order-service")
    public void onRejected(String payload) {
        try {
            InventoryRejectedMessage message = objectMapper.readValue(payload, InventoryRejectedMessage.class);
            cancelOrderUseCase.execute(message.orderId(), message.reason());
        } catch (JsonProcessingException e) {
            log.error("Unparseable message on inventory.rejected, skipping: {}", payload, e);
        }
    }
}
