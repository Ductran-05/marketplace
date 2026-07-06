package com.marketplace.product.application;

import com.marketplace.product.domain.model.Product;
import com.marketplace.product.domain.model.ProductId;
import com.marketplace.product.domain.repository.ProductRepository;
import com.marketplace.shared.exception.BusinessException;
import com.marketplace.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DecreaseStockUseCase {

    private final ProductRepository productRepository;

    public DecreaseStockUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(UUID productId, int quantity) {
        Product product = productRepository.findById(new ProductId(productId))
                .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + productId));

        try {
            product.decreaseStock(quantity);
        } catch (IllegalStateException e) {
            throw new BusinessException("INSUFFICIENT_STOCK", e.getMessage());
        }
        productRepository.save(product);
    }
}
