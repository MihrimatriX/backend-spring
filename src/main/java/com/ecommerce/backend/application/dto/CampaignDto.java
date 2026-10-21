package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;

/** Kampanya (docs/API_CONTRACT.md §3). */
public record CampaignDto(
        Long id,
        String title,
        String subtitle,
        String description,
        Integer discount,
        String imageUrl,
        String backgroundColor,
        String timeLeft,
        String buttonText,
        String buttonHref,
        Boolean isActive,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
