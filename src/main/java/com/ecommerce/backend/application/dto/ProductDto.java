package com.ecommerce.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ürün (docs/API_CONTRACT.md §3). {@code unitPrice} indirimsiz liste fiyatıdır; {@code averageRating}
 * (1 ondalık, yorum yoksa 0) ve {@code totalReviews} aktif yorumlardan hesaplanır.
 */
public record ProductDto(
        Long id,
        String productName,
        BigDecimal unitPrice,
        Integer unitInStock,
        String quantityPerUnit,
        Long categoryId,
        String categoryName,
        Long subCategoryId,
        String subCategoryName,
        String description,
        String imageUrl,
        Integer discount,
        Boolean isActive,
        double averageRating,
        long totalReviews,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
