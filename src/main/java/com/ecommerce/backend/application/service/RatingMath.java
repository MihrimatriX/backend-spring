package com.ecommerce.backend.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Ortalama puan: 1 ondalık, yarım değerler yukarı; yorum yoksa 0 (docs/API_CONTRACT.md §3). */
final class RatingMath {

    private RatingMath() {
    }

    static double average(long ratingSum, long reviewCount) {
        if (reviewCount <= 0) {
            return 0;
        }
        return BigDecimal.valueOf(ratingSum)
                .divide(BigDecimal.valueOf(reviewCount), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
