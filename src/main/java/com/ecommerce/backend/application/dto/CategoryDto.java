package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;

/** Kategori (docs/API_CONTRACT.md §3). */
public record CategoryDto(
        Long id,
        String categoryName,
        String description,
        String imageUrl,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
