package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/** docs/API_CONTRACT.md §3 — Security ({@code recentLogins}: en yeni 5). */
public record SecurityDto(
        Long userId,
        String email,
        Boolean isEmailVerified,
        LocalDateTime lastPasswordChange,
        Boolean twoFactorEnabled,
        LocalDateTime lastLoginAt,
        String lastLoginIp,
        List<LoginHistoryDto> recentLogins) {
}
