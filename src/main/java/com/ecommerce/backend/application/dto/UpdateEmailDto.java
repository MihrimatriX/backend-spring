package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** {@code POST /api/security/update-email}; e-posta serviste küçük harfe çevrilir. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmailDto {

    @NotBlank(message = "New email is required")
    @Email(message = "Invalid email format")
    private String newEmail;

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    /** Doğrulamadan önce kırpılır. */
    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail == null ? null : newEmail.trim();
    }
}
