package com.ecommerce.backend.application.dto;

import java.math.BigDecimal;

/**
 * Sepet satırı: {@code unitPrice} indirimli satış fiyatı, {@code totalPrice} =
 * {@code unitPrice × min(quantity, stok)}, {@code isAvailable} = stok > 0 ve stok ≥ quantity.
 */
public record CartItemDto(
        Long productId,
        String productName,
        String productImageUrl,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal totalPrice,
        Boolean isAvailable) {
}
