package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusDto {
    @NotBlank(message = "Status is required")
    private String status;

    private String notes;
}