package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;

/** docs/API_CONTRACT.md §3 — Faq */
public record FaqDto(
        Long id,
        String question,
        String answer,
        String category,
        Integer viewCount,
        Boolean isPublished,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
