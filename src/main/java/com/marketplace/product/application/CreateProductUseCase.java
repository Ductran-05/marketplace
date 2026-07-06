package com.marketplace.product.application;

import com.marketplace.product.application.command.CreateProductCommand;
import com.marketplace.shared.domain.Money;
import com.marketplace.product.domain.model.Product;
import com.marketplace.product.domain.model.ProductId;
import com.marketplace.product.domain.model.SellerId;
import com.marketplace.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProductUseCase {

    private final ProductRepository productRepository;

    public CreateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductId execute(CreateProductCommand command) {
        Product product = Product.create(
                new SellerId(command.sellerId()),
                command.name(),
                command.description(),
                new Money(command.price(), command.currency()),
                command.stockQuantity()
        );
        productRepository.save(product);
        return product.getId();
    }
}
