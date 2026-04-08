package com.ecommerce.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("items")
    private List<CartItemDto> items;

    @JsonProperty("totalItems")
    private Integer totalItems;

    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
}
