package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/** {@code PUT /api/review/{id}} — kısmi güncelleme; gönderilmeyen (null) alanlar değişmez. */
public record UpdateReviewDto(
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5")
        Integer rating,

        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title,

        @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        String comment) {
}
