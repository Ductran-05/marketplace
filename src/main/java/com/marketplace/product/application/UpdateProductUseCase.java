package com.marketplace.product.application;

import com.marketplace.product.application.command.UpdateProductCommand;
import com.marketplace.product.domain.model.Money;
import com.marketplace.product.domain.model.Product;
import com.marketplace.product.domain.model.ProductId;
import com.marketplace.product.domain.repository.ProductRepository;
import com.marketplace.shared.exception.BusinessException;
import com.marketplace.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProductUseCase {

    private final ProductRepository productRepository;

    public UpdateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(UpdateProductCommand command) {
        Product product = productRepository.findById(new ProductId(command.productId()))
                .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found"));

        if (!product.isOwnedBy(command.requesterId())) {
            throw new BusinessException("NOT_PRODUCT_OWNER", "You can only update your own products");
        }

        product.update(
                command.name(),
                command.description(),
                new Money(command.price(), command.currency()),
                command.stockQuantity()
        );
        productRepository.save(product);
    }
}
