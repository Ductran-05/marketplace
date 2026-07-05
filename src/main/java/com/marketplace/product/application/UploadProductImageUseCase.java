package com.marketplace.product.application;

import com.marketplace.product.application.port.ImageStorage;
import com.marketplace.product.domain.model.Product;
import com.marketplace.product.domain.model.ProductId;
import com.marketplace.product.domain.repository.ProductRepository;
import com.marketplace.shared.exception.BusinessException;
import com.marketplace.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Service
public class UploadProductImageUseCase {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final ProductRepository productRepository;
    private final ImageStorage imageStorage;

    public UploadProductImageUseCase(ProductRepository productRepository, ImageStorage imageStorage) {
        this.productRepository = productRepository;
        this.imageStorage = imageStorage;
    }

    @Transactional
    public String execute(UUID productId, UUID requesterId,
                          String contentType, InputStream content, long size) {
        Product product = productRepository.findById(new ProductId(productId))
                .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found"));

        if (!product.isOwnedBy(requesterId)) {
            throw new BusinessException("NOT_PRODUCT_OWNER", "You can only upload images for your own products");
        }

        String extension = ALLOWED_TYPES.get(contentType);
        if (extension == null) {
            throw new BusinessException("UNSUPPORTED_IMAGE_TYPE",
                    "Only JPEG, PNG, WebP are supported, got: " + contentType);
        }

        String key = "products/" + productId + "/" + UUID.randomUUID() + extension;
        imageStorage.store(key, content, size, contentType);

        String oldKey = product.getImageKey();
        product.attachImage(key);
        productRepository.save(product);

        // Xóa ảnh cũ sau khi DB đã trỏ sang ảnh mới — thất bại cũng chỉ để lại rác, không mất dữ liệu
        if (oldKey != null) {
            imageStorage.delete(oldKey);
        }
        return key;
    }
}
