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
public class DeleteProductUseCase {

    private final ProductRepository productRepository;

    public DeleteProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(UUID productId, UUID requesterId) {
        Product product = productRepository.findById(new ProductId(productId))
                .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found"));

        if (!product.isOwnedBy(requesterId)) {
            throw new BusinessException("NOT_PRODUCT_OWNER", "You can only delete your own products");
        }

        productRepository.delete(product);
    }
}
