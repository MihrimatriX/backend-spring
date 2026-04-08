package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateEmailDto {
    @NotBlank(message = "New email is required")
    @Email(message = "Invalid email format")
    private String newEmail;

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    // Constructors
    public UpdateEmailDto() {}

    public UpdateEmailDto(String newEmail, String currentPassword) {
        this.newEmail = newEmail;
        this.currentPassword = currentPassword;
    }

    // Getters and Setters
    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
}
