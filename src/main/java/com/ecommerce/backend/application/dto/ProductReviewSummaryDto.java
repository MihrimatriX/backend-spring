package com.ecommerce.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewSummaryDto {

    private Long productId;
    private Double averageRating;
    private Long totalReviews;
    private Long rating1Count;
    private Long rating2Count;
    private Long rating3Count;
    private Long rating4Count;
    private Long rating5Count;
}
