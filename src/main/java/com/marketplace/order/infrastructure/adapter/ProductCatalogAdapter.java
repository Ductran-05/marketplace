package com.marketplace.order.infrastructure.adapter;

import com.marketplace.order.application.port.ProductCatalog;
import com.marketplace.product.application.GetProductsUseCase;
import com.marketplace.product.domain.model.Product;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Nối order → product QUA APPLICATION LAYER của product
 * (đúng quy tắc: không đụng repository/domain nội bộ của module khác).
 */
@Component
public class ProductCatalogAdapter implements ProductCatalog {

    private final GetProductsUseCase getProductsUseCase;

    public ProductCatalogAdapter(GetProductsUseCase getProductsUseCase) {
        this.getProductsUseCase = getProductsUseCase;
    }

    @Override
    public ProductSnapshot getProduct(UUID productId) {
        Product product = getProductsUseCase.getById(productId);
        return new ProductSnapshot(productId, product.getName(), product.getPrice());
    }
}
