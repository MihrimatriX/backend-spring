package com.ecommerce.backend.application.dto;

import java.util.List;

/** docs/API_CONTRACT.md §3 — NotificationSummary ({@code recentNotifications}: en yeni 5). */
public record NotificationSummaryDto(
        long totalNotifications,
        long unreadNotifications,
        List<NotificationDto> recentNotifications) {
}
