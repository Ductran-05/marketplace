package com.marketplace.product.application;

import com.marketplace.product.application.port.ReservationStore;
import com.marketplace.product.domain.model.Product;
import com.marketplace.product.domain.model.ProductId;
import com.marketplace.product.domain.repository.ProductRepository;
import com.marketplace.shared.config.KafkaTopicsConfig;
import com.marketplace.shared.messaging.InventoryRejectedMessage;
import com.marketplace.shared.messaging.InventoryReservedMessage;
import com.marketplace.shared.messaging.OrderPlacedMessage;
import com.marketplace.shared.outbox.OutboxAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Bước 2 của saga đặt hàng: giữ kho cho order.
 *
 * - IDEMPOTENT: order đã xử lý rồi (message trùng do at-least-once) → bỏ qua.
 * - Validate TRƯỚC, apply SAU: chỉ trừ kho khi mọi item đều đủ —
 *   không bao giờ trừ nửa vời.
 * - Kết quả (reserved/rejected) phát qua outbox — cùng transaction với việc trừ kho.
 */
@Service
public class ReserveStockUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReserveStockUseCase.class);

    private final ProductRepository productRepository;
    private final ReservationStore reservationStore;
    private final OutboxAppender outboxAppender;

    public ReserveStockUseCase(ProductRepository productRepository,
                               ReservationStore reservationStore,
                               OutboxAppender outboxAppender) {
        this.productRepository = productRepository;
        this.reservationStore = reservationStore;
        this.outboxAppender = outboxAppender;
    }

    @Transactional
    public void execute(UUID orderId, List<OrderPlacedMessage.Item> items) {
        if (reservationStore.alreadyProcessed(orderId)) {
            log.info("Order {} already processed — skipping duplicate message", orderId);
            return;
        }

        // Pha 1: validate tất cả item, chưa đụng gì
        List<Product> toDecrease = new ArrayList<>();
        String failure = null;
        for (OrderPlacedMessage.Item item : items) {
            Optional<Product> found = productRepository.findById(new ProductId(item.productId()));
            if (found.isEmpty()) {
                failure = "Product not found: " + item.productId();
                break;
            }
            Product product = found.get();
            if (item.quantity() > product.getStockQuantity()) {
                failure = "Insufficient stock for '" + product.getName()
                        + "': requested " + item.quantity() + ", available " + product.getStockQuantity();
                break;
            }
            toDecrease.add(product);
        }

        if (failure == null) {
            // Pha 2: mọi item đều ổn → trừ kho thật
            for (int i = 0; i < items.size(); i++) {
                Product product = toDecrease.get(i);
                product.decreaseStock(items.get(i).quantity());
                productRepository.save(product);
            }
            reservationStore.saveReserved(orderId);
            outboxAppender.append(KafkaTopicsConfig.TOPIC_INVENTORY_RESERVED, orderId.toString(),
                    new InventoryReservedMessage(orderId, Instant.now()));
            log.info("Stock reserved for order {}", orderId);
        } else {
            reservationStore.saveRejected(orderId, failure);
            outboxAppender.append(KafkaTopicsConfig.TOPIC_INVENTORY_REJECTED, orderId.toString(),
                    new InventoryRejectedMessage(orderId, failure, Instant.now()));
            log.info("Stock rejected for order {}: {}", orderId, failure);
        }
    }
}
