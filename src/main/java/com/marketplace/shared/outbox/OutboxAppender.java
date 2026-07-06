package com.marketplace.shared.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Ghi event vào bảng outbox. PHẢI được gọi bên trong transaction đang mở —
 * để dòng outbox và dữ liệu nghiệp vụ cùng sống chết trong 1 commit.
 */
@Component
public class OutboxAppender {

    private final OutboxJpaRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxAppender(OutboxJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void append(String topic, String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            repository.save(new OutboxEventEntity(UUID.randomUUID(), topic, key, json, Instant.now()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize outbox payload for topic " + topic, e);
        }
    }
}
