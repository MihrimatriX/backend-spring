package com.ecommerce.backend.application.dto;

/** docs/API_CONTRACT.md §3 — AuthResponse */
public record AuthResponseDto(
        String token,
        String type,
        Long userId,
        String email,
        String firstName,
        String lastName,
        Boolean isEmailVerified,
        String role) {

    public static AuthResponseDto bearer(String token, Long userId, String email, String firstName, String lastName,
            Boolean isEmailVerified, String role) {
        return new AuthResponseDto(token, "Bearer", userId, email, firstName, lastName, isEmailVerified, role);
    }
}
