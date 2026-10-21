package com.ecommerce.backend.application.dto;

/** Ürün yorum özeti (docs/API_CONTRACT.md §3); yorum yoksa tüm değerler 0. */
public record ProductReviewSummaryDto(
        Long productId,
        double averageRating,
        long totalReviews,
        long rating1Count,
        long rating2Count,
        long rating3Count,
        long rating4Count,
        long rating5Count) {
}
