package com.ecommerce.backend.application.dto;

import java.math.BigDecimal;

/**
 * {@code GET /api/product} sorgusu (§4.2). {@code pageNumber}/{@code pageSize} controller'da kırpılmış gelir.
 */
public record ProductFilterDto(
        Long categoryId,
        Long subCategoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String searchTerm,
        String sortBy,
        String sortOrder,
        int pageNumber,
        int pageSize) {
}
