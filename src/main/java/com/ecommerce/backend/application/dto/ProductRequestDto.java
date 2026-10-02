package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Yönetici ürün oluşturma/güncelleme gövdesi (§4.2). {@code Product} şeklindeki okuma alanları
 * ({@code id}, {@code categoryName}, {@code averageRating}, tarihler …) gönderilirse yok sayılır.
 * {@code isActive} gönderilmezse oluşturmada {@code true}, güncellemede değişmez.
 */
public record ProductRequestDto(
        @NotBlank(message = "Product name is required")
        @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
        String productName,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Unit price must have at most 10 integer digits and 2 decimal places")
        BigDecimal unitPrice,

        @NotNull(message = "Unit in stock is required")
        @Min(value = 0, message = "Unit in stock cannot be negative")
        Integer unitInStock,

        @NotBlank(message = "Quantity per unit is required")
        @Size(max = 50, message = "Quantity per unit cannot exceed 50 characters")
        String quantityPerUnit,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        Long subCategoryId,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @Size(max = 255, message = "Image URL cannot exceed 255 characters")
        String imageUrl,

        @Min(value = 0, message = "Discount must be between 0 and 100")
        @Max(value = 100, message = "Discount must be between 0 and 100")
        Integer discount,

        Boolean isActive) {
}
