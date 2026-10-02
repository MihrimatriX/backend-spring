package com.ecommerce.backend.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** {@code POST /api/order} gövdesi (docs/API_CONTRACT.md §5.2). */
@Data
public class CreateOrderDto {
    @NotNull(message = "Shipping address is required")
    private Long shippingAddressId;

    @NotNull(message = "Payment method is required")
    private Long paymentMethodId;

    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "At least one item is required")
    private List<@Valid @NotNull(message = "Order item is required") CreateOrderItemDto> items = new ArrayList<>();

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
