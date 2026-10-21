package com.ecommerce.backend.application.dto;

import java.math.BigDecimal;

/** Sipariş satırı; {@code unitPrice} sipariş anındaki indirimli fiyattır. */
public record OrderItemDto(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice) {
}
