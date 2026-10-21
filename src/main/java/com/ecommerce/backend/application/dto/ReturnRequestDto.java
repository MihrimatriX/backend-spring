package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReturnRequestDto {

    @NotBlank(message = "İade nedeni zorunludur")
    @Size(max = 500, message = "İade nedeni en fazla 500 karakter olabilir")
    private String reason;
}
