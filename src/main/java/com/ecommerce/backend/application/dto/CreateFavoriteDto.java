package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.NotNull;

public class CreateFavoriteDto {
    @NotNull(message = "Product ID is required")
    private Long productId;

    // Constructors
    public CreateFavoriteDto() {
    }

    public CreateFavoriteDto(Long productId) {
        this.productId = productId;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
