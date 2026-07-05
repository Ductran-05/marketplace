package com.marketplace.product.presentation.response;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        long totalPages
) {}
