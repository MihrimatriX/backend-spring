package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;

/** docs/API_CONTRACT.md §3 — UserSettings */
public record UserSettingsDto(
        Long userId,
        String language,
        String timezone,
        String currency,
        Boolean emailNotifications,
        Boolean smsNotifications,
        Boolean pushNotifications,
        Boolean marketingEmails,
        Boolean orderUpdates,
        Boolean priceAlerts,
        Boolean stockNotifications,
        String theme,
        Integer itemsPerPage,
        Boolean autoSaveCart,
        Boolean showProductRecommendations,
        Boolean enableLocationServices,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
