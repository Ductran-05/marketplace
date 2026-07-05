package com.marketplace.product.infrastructure.persistence;

import com.marketplace.product.domain.model.Money;
import com.marketplace.product.domain.model.Product;
import com.marketplace.product.domain.model.ProductId;
import com.marketplace.product.domain.model.SellerId;
import com.marketplace.product.domain.repository.ProductRepository;
import com.marketplace.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.marketplace.product.infrastructure.persistence.repository.ProductJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryImpl(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        jpaRepository.save(toEntity(product));
        return product;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public void delete(Product product) {
        jpaRepository.deleteById(product.getId().value());
    }

    @Override
    public List<Product> findPage(int page, int size) {
        return jpaRepository
                .findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toDomain)
                .getContent();
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    private ProductJpaEntity toEntity(Product p) {
        return new ProductJpaEntity(
                p.getId().value(),
                p.getSellerId().value(),
                p.getName(),
                p.getDescription(),
                p.getPrice().amount(),
                p.getPrice().currency(),
                p.getStockQuantity(),
                p.getImageKey(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private Product toDomain(ProductJpaEntity e) {
        return Product.reconstitute(
                new ProductId(e.getId()),
                new SellerId(e.getSellerId()),
                e.getName(),
                e.getDescription(),
                new Money(e.getPriceAmount(), e.getPriceCurrency()),
                e.getStockQuantity(),
                e.getImageKey(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
