package com.marketplace.notification.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.application.GetUserContactUseCase;
import com.marketplace.shared.config.KafkaTopicsConfig;
import com.marketplace.shared.messaging.OrderConfirmedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Bước cuối của saga: đơn đã CONFIRMED → gửi mail xác nhận cho người mua.
 * Consumer này ở notification module — sau này tách service chỉ việc bê đi,
 * vì nó chỉ phụ thuộc Kafka message + use case công khai của auth.
 */
@Component
public class OrderConfirmedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmedConsumer.class);

    private final GetUserContactUseCase getUserContactUseCase;
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    public OrderConfirmedConsumer(GetUserContactUseCase getUserContactUseCase,
                                  JavaMailSender mailSender,
                                  ObjectMapper objectMapper) {
        this.getUserContactUseCase = getUserContactUseCase;
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopicsConfig.TOPIC_ORDER_CONFIRMED, groupId = "notification-service")
    public void handle(String payload) {
        OrderConfirmedMessage message;
        try {
            message = objectMapper.readValue(payload, OrderConfirmedMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Unparseable message on order.confirmed, skipping: {}", payload, e);
            return;
        }

        var contact = getUserContactUseCase.execute(message.buyerId());

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("no-reply@marketplace.local");
        mail.setTo(contact.email());
        mail.setSubject("Marketplace - Order confirmed");
        mail.setText("""
                Hi %s,

                Your order has been confirmed!

                Order ID: %s
                Total:    %s %s

                Thank you for shopping with us.
                """.formatted(contact.fullName(), message.orderId(),
                message.totalAmount().toPlainString(), message.currency()));

        mailSender.send(mail);
        log.info("Order confirmation email sent to {} for order {}", contact.email(), message.orderId());
    }
}
