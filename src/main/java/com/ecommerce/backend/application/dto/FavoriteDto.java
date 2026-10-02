package com.ecommerce.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Favori (docs/API_CONTRACT.md §3): {@code productPrice} liste fiyatı, {@code productDiscount}
 * yüzde indirim.
 */
public record FavoriteDto(
        Long id,
        Long userId,
        Long productId,
        String productName,
        String productImageUrl,
        BigDecimal productPrice,
        Integer productDiscount,
        String productCategory,
        Boolean productInStock,
        LocalDateTime createdAt) {
}
