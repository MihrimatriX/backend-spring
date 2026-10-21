package com.ecommerce.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** {@code PUT /api/cart/update}: miktarı ayarlar; {@code quantity ≤ 0} satırı siler. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemDto {
    private Long productId;
    private Integer quantity;
}
