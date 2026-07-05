package com.marketplace.product.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 5000) String description,
        @NotNull @DecimalMin(value = "0.0") BigDecimal price,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}", message = "Currency must be a 3-letter code") String currency,
        @Min(0) int stockQuantity
) {}
