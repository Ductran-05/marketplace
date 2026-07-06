package com.marketplace.shared.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Job nền: quét outbox chưa xử lý → đẩy Kafka → đánh dấu processed.
 *
 * Đảm bảo AT-LEAST-ONCE: nếu gửi Kafka xong mà chưa kịp đánh dấu processed
 * rồi crash, lần sau sẽ gửi LẶP LẠI — consumer phải idempotent.
 * Không bao giờ mất event: crash lúc nào thì event vẫn nằm trong DB.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxJpaRepository repository;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    public OutboxRelay(OutboxJpaRepository repository,
                       KafkaTemplate<String, String> stringKafkaTemplate) {
        this.repository = repository;
        this.stringKafkaTemplate = stringKafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    public void relay() {
        List<OutboxEventEntity> batch = repository.findTop50ByProcessedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEventEntity event : batch) {
            try {
                stringKafkaTemplate
                        .send(event.getTopic(), event.getMessageKey(), event.getPayload())
                        .get(10, TimeUnit.SECONDS);   // chờ broker ack thật sự

                event.markProcessed();
                repository.save(event);
            } catch (Exception e) {
                log.warn("Outbox relay failed for event {} (topic {}): {} — will retry next tick",
                        event.getId(), event.getTopic(), e.getMessage());
                break;   // dừng cả batch để giữ thứ tự; tick sau thử lại từ event này
            }
        }
    }
}
