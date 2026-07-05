package com.marketplace.product.application;

import com.marketplace.product.domain.model.Product;
import com.marketplace.product.domain.model.ProductId;
import com.marketplace.product.domain.repository.ProductRepository;
import com.marketplace.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetProductsUseCase {

    private final ProductRepository productRepository;

    public GetProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Product getById(UUID id) {
        return productRepository.findById(new ProductId(id))
                .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found"));
    }

    @Transactional(readOnly = true)
    public PageResult getPage(int page, int size) {
        List<Product> products = productRepository.findPage(page, size);
        long total = productRepository.countAll();
        return new PageResult(products, page, size, total);
    }

    public record PageResult(List<Product> items, int page, int size, long totalItems) {
        public long totalPages() {
            return size == 0 ? 0 : (totalItems + size - 1) / size;
        }
    }
}
