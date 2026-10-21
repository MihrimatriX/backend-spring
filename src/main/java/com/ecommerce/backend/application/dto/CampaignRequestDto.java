package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Yönetici kampanya oluşturma/güncelleme gövdesi (§4.5). Tarihler {@code Z}/ofsetli ISO-8601 gelir ve UTC'ye
 * çevrilir; {@code discount} gönderilmezse 0. {@code isActive} gönderilmezse oluşturmada {@code true},
 * güncellemede değişmez.
 */
public record CampaignRequestDto(
        @NotBlank(message = "Title is required")
        @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
        String title,

        @Size(max = 300, message = "Subtitle cannot exceed 300 characters")
        String subtitle,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @Min(value = 0, message = "Discount must be between 0 and 100")
        @Max(value = 100, message = "Discount must be between 0 and 100")
        Integer discount,

        @Size(max = 255, message = "Image URL cannot exceed 255 characters")
        String imageUrl,

        @Size(max = 50, message = "Background color cannot exceed 50 characters")
        String backgroundColor,

        @Size(max = 50, message = "Time left cannot exceed 50 characters")
        String timeLeft,

        @Size(max = 50, message = "Button text cannot exceed 50 characters")
        String buttonText,

        @Size(max = 255, message = "Button link cannot exceed 255 characters")
        String buttonHref,

        @NotNull(message = "Start date is required")
        LocalDateTime startDate,

        @NotNull(message = "End date is required")
        LocalDateTime endDate,

        Boolean isActive) {
}
