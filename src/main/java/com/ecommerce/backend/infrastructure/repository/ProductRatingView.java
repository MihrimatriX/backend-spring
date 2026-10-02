package com.ecommerce.backend.infrastructure.repository;

/** Bir ürünün aktif yorum toplamları ({@link ReviewRepository#findRatingTotals}). */
public interface ProductRatingView {

    Long getProductId();

    Long getRatingSum();

    Long getReviewCount();
}
