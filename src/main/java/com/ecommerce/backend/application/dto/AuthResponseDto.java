package com.ecommerce.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isEmailVerified;

    public AuthResponseDto(String token, Long userId, String email, String firstName, String lastName,
            Boolean isEmailVerified) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isEmailVerified = isEmailVerified;
    }
}
