package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** {@code POST /api/security/disable-2fa} (taslak uç). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisableTwoFactorDto {

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Verification code is required")
    private String verificationCode;
}
