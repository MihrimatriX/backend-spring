package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;

/** docs/API_CONTRACT.md §3 — Notification */
public record NotificationDto(
        Long id,
        Long userId,
        String title,
        String message,
        String type,
        String actionUrl,
        Boolean isRead,
        LocalDateTime readAt,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
