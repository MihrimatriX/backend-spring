package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** {@code PUT /api/category/{id}}; {@code isActive} gönderilmezse (null) değişmez. */
public record UpdateCategoryDto(
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name cannot exceed 100 characters")
        String categoryName,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @Size(max = 255, message = "Image URL cannot exceed 255 characters")
        String imageUrl,

        Boolean isActive) {
}
