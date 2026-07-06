package com.marketplace.shared.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

@Configuration
public class KafkaTopicsConfig {

    public static final String TOPIC_ORDER_PLACED = "order.placed";

    @Bean
    public NewTopic orderPlacedTopic() {
        return TopicBuilder.name(TOPIC_ORDER_PLACED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Template cho Outbox relay: payload trong outbox ĐÃ là JSON string,
     * dùng StringSerializer để gửi nguyên văn (JsonSerializer mặc định sẽ bọc thêm 1 lớp quote).
     */
    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate(KafkaProperties properties) {
        Map<String, Object> config = properties.buildProducerProperties(null);
        var factory = new DefaultKafkaProducerFactory<String, String>(
                config, new StringSerializer(), new StringSerializer());
        return new KafkaTemplate<>(factory);
    }
}
