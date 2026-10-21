package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;

/** Yorum (docs/API_CONTRACT.md §3). {@code userName} = "Ad Soyad". */
public record ReviewDto(
        Long id,
        Long userId,
        Long productId,
        Integer rating,
        String title,
        String comment,
        Boolean isVerified,
        Boolean isHelpful,
        String userName,
        String productName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
