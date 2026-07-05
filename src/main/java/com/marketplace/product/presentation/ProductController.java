package com.marketplace.product.presentation;

import com.marketplace.product.application.CreateProductUseCase;
import com.marketplace.product.application.DeleteProductUseCase;
import com.marketplace.product.application.GetProductsUseCase;
import com.marketplace.product.application.UpdateProductUseCase;
import com.marketplace.product.application.UploadProductImageUseCase;
import com.marketplace.product.application.command.CreateProductCommand;
import com.marketplace.product.application.command.UpdateProductCommand;
import com.marketplace.product.application.port.ImageStorage;
import com.marketplace.product.domain.model.Product;
import com.marketplace.product.presentation.request.ProductRequest;
import com.marketplace.product.presentation.response.PageResponse;
import com.marketplace.product.presentation.response.ProductResponse;
import com.marketplace.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final GetProductsUseCase getProductsUseCase;
    private final UploadProductImageUseCase uploadProductImageUseCase;
    private final ImageStorage imageStorage;

    public ProductController(CreateProductUseCase createProductUseCase,
                             UpdateProductUseCase updateProductUseCase,
                             DeleteProductUseCase deleteProductUseCase,
                             GetProductsUseCase getProductsUseCase,
                             UploadProductImageUseCase uploadProductImageUseCase,
                             ImageStorage imageStorage) {
        this.createProductUseCase = createProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.getProductsUseCase = getProductsUseCase;
        this.uploadProductImageUseCase = uploadProductImageUseCase;
        this.imageStorage = imageStorage;
    }

    private ProductResponse toResponse(Product product) {
        String imageUrl = product.getImageKey() == null
                ? null
                : imageStorage.presignedUrl(product.getImageKey());
        return ProductResponse.from(product, imageUrl);
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody ProductRequest request,
                                                      @AuthenticationPrincipal AuthenticatedUser user) {
        var productId = createProductUseCase.execute(new CreateProductCommand(
                user.userId(), request.name(), request.description(),
                request.price(), request.currency(), request.stockQuantity()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("productId", productId.toString()));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = getProductsUseCase.getPage(page, Math.min(size, 100));
        return ResponseEntity.ok(new PageResponse<>(
                result.items().stream().map(this::toResponse).toList(),
                result.page(), result.size(), result.totalItems(), result.totalPages()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(getProductsUseCase.getById(id)));
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Map<String, String>> uploadImage(@PathVariable UUID id,
                                                           @RequestParam("file") MultipartFile file,
                                                           @AuthenticationPrincipal AuthenticatedUser user) throws IOException {
        String key = uploadProductImageUseCase.execute(
                id, user.userId(), file.getContentType(), file.getInputStream(), file.getSize());
        return ResponseEntity.ok(Map.of(
                "imageKey", key,
                "imageUrl", imageStorage.presignedUrl(key)
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> update(@PathVariable UUID id,
                                       @Valid @RequestBody ProductRequest request,
                                       @AuthenticationPrincipal AuthenticatedUser user) {
        updateProductUseCase.execute(new UpdateProductCommand(
                id, user.userId(), request.name(), request.description(),
                request.price(), request.currency(), request.stockQuantity()
        ));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal AuthenticatedUser user) {
        deleteProductUseCase.execute(id, user.userId());
        return ResponseEntity.noContent().build();
    }
}
