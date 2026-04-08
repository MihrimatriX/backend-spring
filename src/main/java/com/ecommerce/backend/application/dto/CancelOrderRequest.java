package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelOrderRequest {

    @Size(max = 500, message = "İptal nedeni en fazla 500 karakter olabilir")
    private String reason;
}
