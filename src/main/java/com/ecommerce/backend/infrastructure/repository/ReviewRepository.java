package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = { "user", "product" })
    List<Review> findByProductIdAndIsActiveTrueOrderByCreatedAtDescIdDesc(Long productId);

    @EntityGraph(attributePaths = { "user", "product" })
    List<Review> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = { "user", "product" })
    Optional<Review> findWithDetailsByIdAndIsActiveTrue(Long id);

    boolean existsByUserIdAndProductIdAndIsActiveTrue(Long userId, Long productId);

    /** Ürün başına aktif yorum toplamları — sayfa başına tek gruplu sorgu (N+1 yok). */
    @Query("SELECT r.productId AS productId, SUM(r.rating) AS ratingSum, COUNT(r) AS reviewCount FROM Review r "
            + "WHERE r.isActive = true AND r.productId IN :productIds GROUP BY r.productId")
    List<ProductRatingView> findRatingTotals(@Param("productIds") Collection<Long> productIds);

    @Query("SELECT r.rating AS rating, COUNT(r) AS reviewCount FROM Review r "
            + "WHERE r.isActive = true AND r.productId = :productId GROUP BY r.rating")
    List<RatingCountView> countByRating(@Param("productId") Long productId);

    /**
     * Kullanıcının bu ürünü içeren iptal edilmemiş sipariş sayısı ("doğrulanmış alışveriş", §4.11).
     * Sipariş tablolarına salt okunur erişir.
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi JOIN oi.order o WHERE o.userId = :userId "
            + "AND oi.productId = :productId AND LOWER(o.status) <> 'cancelled'")
    long countPurchases(@Param("userId") Long userId, @Param("productId") Long productId);
}
