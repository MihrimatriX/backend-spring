package com.ecommerce.backend.application.dto;

import lombok.Data;

/** {@code POST /api/favorite/add}; ürün yoksa 400 {@code PRODUCT_NOT_FOUND}. */
@Data
public class AddToFavoritesDto {
    private Long productId;
}
