package com.ecommerce.backend.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteDto {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal productPrice;
    private BigDecimal productDiscount;
    private String productCategory;
    private Boolean productInStock;
    private LocalDateTime createdAt;
}