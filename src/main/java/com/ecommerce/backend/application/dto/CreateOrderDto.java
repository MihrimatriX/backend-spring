package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CreateOrderDto {
    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;

    @NotNull(message = "Payment method ID is required")
    private Long paymentMethodId;

    @NotEmpty(message = "Order items are required")
    private List<CreateOrderItemDto> items = new ArrayList<>();

    private String notes;
}