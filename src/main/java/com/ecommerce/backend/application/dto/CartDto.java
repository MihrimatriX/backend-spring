package com.ecommerce.backend.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sepet (docs/API_CONTRACT.md §3, §5.1). {@code totalAmount} ara toplamdır; {@code grandTotal}
 * kargo dahildir. {@code freeShippingRemainingTry} ücretsiz kargo eşiğine kalan tutar (eşik
 * aşıldıysa {@code null}).
 */
public record CartDto(
        Long userId,
        List<CartItemDto> items,
        Integer totalItems,
        BigDecimal totalAmount,
        BigDecimal shippingFee,
        BigDecimal grandTotal,
        BigDecimal freeShippingRemainingTry) {
}
