package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;

/** Alt kategori (docs/API_CONTRACT.md §3). */
public record SubCategoryDto(
        Long id,
        String subCategoryName,
        String description,
        String imageUrl,
        Long categoryId,
        String categoryName,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
