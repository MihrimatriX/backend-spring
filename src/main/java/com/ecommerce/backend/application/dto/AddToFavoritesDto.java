package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToFavoritesDto {
    @NotNull(message = "Product ID is required")
    private Long productId;
}
