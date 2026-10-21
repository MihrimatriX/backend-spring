package com.ecommerce.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code POST /api/cart/add}. Geçersiz değerler iş kuralı kodlarıyla döner
 * ({@code INVALID_QUANTITY}, {@code PRODUCT_NOT_FOUND}) — Bean Validation kullanılmaz.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartDto {
    private Long productId;
    private Integer quantity;
}
