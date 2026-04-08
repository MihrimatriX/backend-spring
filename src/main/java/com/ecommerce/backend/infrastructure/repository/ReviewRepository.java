package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdAndIsActiveTrueOrderByCreatedAtDesc(Long productId);

    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.isActive = true ORDER BY r.createdAt DESC")
    List<Review> findActiveReviewsByProductId(@Param("productId") Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.isActive = true")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.isActive = true")
    Long countActiveReviewsByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.rating = :rating AND r.isActive = true")
    Long countReviewsByProductIdAndRating(@Param("productId") Long productId, @Param("rating") Integer rating);

    List<Review> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndProductIdAndIsActiveTrue(Long userId, Long productId);
}
