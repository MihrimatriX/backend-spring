package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Anonim iletişim formu — {@code POST /api/helpsupport/contact}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactFormDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject cannot exceed 200 characters")
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category = "General";

    /** Doğrulamadan önce kırpılır. */
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }
}
