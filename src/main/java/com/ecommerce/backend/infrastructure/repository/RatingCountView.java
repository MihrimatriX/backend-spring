package com.ecommerce.backend.infrastructure.repository;

/** Puan başına aktif yorum sayısı ({@link ReviewRepository#countByRating}). */
public interface RatingCountView {

    Integer getRating();

    Long getReviewCount();
}
