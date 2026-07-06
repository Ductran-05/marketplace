package com.marketplace.shared.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

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
}
