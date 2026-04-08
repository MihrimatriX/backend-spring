package com.ecommerce.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    @JsonProperty("productId")
    private Long productId;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("productImageUrl")
    private String productImageUrl;

    @JsonProperty("unitPrice")
    private BigDecimal unitPrice;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("totalPrice")
    private BigDecimal totalPrice;

    /** Product discount % (for UI line totals). */
    @JsonProperty("discount")
    private Integer discount;

    @JsonProperty("unitInStock")
    private Integer unitInStock;

    @JsonProperty("quantityPerUnit")
    private String quantityPerUnit;
}
