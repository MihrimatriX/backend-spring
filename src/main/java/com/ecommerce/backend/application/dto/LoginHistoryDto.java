package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;

/** docs/API_CONTRACT.md §3 — LoginHistory */
public record LoginHistoryDto(
        Long id,
        LocalDateTime loginAt,
        String ipAddress,
        String userAgent,
        String location,
        Boolean isSuccessful) {
}
