package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;

/** docs/API_CONTRACT.md §3 — SupportMessage */
public record SupportMessageDto(
        Long id,
        Long ticketId,
        Long userId,
        String userName,
        String message,
        Boolean isFromSupport,
        LocalDateTime createdAt) {
}
