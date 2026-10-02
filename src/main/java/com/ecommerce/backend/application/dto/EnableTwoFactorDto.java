package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** {@code POST /api/security/enable-2fa} (taslak uç). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnableTwoFactorDto {

    @NotBlank(message = "Password is required")
    private String password;
}
