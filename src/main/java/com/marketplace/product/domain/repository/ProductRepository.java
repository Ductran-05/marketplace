package com.marketplace.product.domain.repository;

import com.marketplace.product.domain.model.Product;
import com.marketplace.product.domain.model.ProductId;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
    void delete(Product product);
    List<Product> findPage(int page, int size);
    long countAll();
}
