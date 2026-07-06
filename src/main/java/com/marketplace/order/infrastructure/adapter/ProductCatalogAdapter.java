package com.marketplace.order.infrastructure.adapter;

import com.marketplace.order.application.port.ProductCatalog;
import com.marketplace.product.application.DecreaseStockUseCase;
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
    private final DecreaseStockUseCase decreaseStockUseCase;

    public ProductCatalogAdapter(GetProductsUseCase getProductsUseCase,
                                 DecreaseStockUseCase decreaseStockUseCase) {
        this.getProductsUseCase = getProductsUseCase;
        this.decreaseStockUseCase = decreaseStockUseCase;
    }

    @Override
    public ProductSnapshot getProduct(UUID productId) {
        Product product = getProductsUseCase.getById(productId);
        return new ProductSnapshot(productId, product.getName(), product.getPrice());
    }

    @Override
    public void decreaseStock(UUID productId, int quantity) {
        decreaseStockUseCase.execute(productId, quantity);
    }
}
