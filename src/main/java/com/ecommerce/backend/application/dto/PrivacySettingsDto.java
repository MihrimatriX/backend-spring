package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;

/** docs/API_CONTRACT.md §3 — PrivacySettings */
public record PrivacySettingsDto(
        Long userId,
        Boolean profileVisibility,
        Boolean showEmail,
        Boolean showPhone,
        Boolean allowDataCollection,
        Boolean allowAnalytics,
        Boolean allowCookies,
        Boolean allowMarketing,
        Boolean dataSharing,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
