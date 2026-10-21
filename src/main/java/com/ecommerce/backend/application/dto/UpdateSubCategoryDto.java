package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * {@code PUT /api/subcategory/{id}}. {@code id} gönderilmişse rotadaki id ile aynı olmalıdır
 * (400 {@code ID_MISMATCH}); {@code isActive} gönderilmezse (null) değişmez.
 */
public record UpdateSubCategoryDto(
        Long id,

        @NotBlank(message = "SubCategory name is required")
        @Size(max = 100, message = "SubCategory name cannot exceed 100 characters")
        String subCategoryName,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @Size(max = 200, message = "Image URL cannot exceed 200 characters")
        String imageUrl,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        Boolean isActive) {
}
