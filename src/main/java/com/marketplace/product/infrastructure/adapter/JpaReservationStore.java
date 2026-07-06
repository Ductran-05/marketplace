package com.marketplace.product.infrastructure.adapter;

import com.marketplace.product.application.port.ReservationStore;
import com.marketplace.product.infrastructure.persistence.entity.StockReservationJpaEntity;
import com.marketplace.product.infrastructure.persistence.repository.StockReservationJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JpaReservationStore implements ReservationStore {

    private final StockReservationJpaRepository repository;

    public JpaReservationStore(StockReservationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean alreadyProcessed(UUID orderId) {
        return repository.existsById(orderId);
    }

    @Override
    public void saveReserved(UUID orderId) {
        repository.save(new StockReservationJpaEntity(orderId, StockReservationJpaEntity.STATUS_RESERVED, null));
    }

    @Override
    public void saveRejected(UUID orderId, String reason) {
        repository.save(new StockReservationJpaEntity(orderId, StockReservationJpaEntity.STATUS_REJECTED, reason));
    }
}
