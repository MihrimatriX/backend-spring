package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Favorite> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);

    Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);

    Optional<Favorite> findByUserIdAndProductIdAndIsActiveTrue(Long userId, Long productId);

    void deleteByUserId(Long userId);

    @Query("SELECT f FROM Favorite f WHERE f.userId = :userId AND f.productId = :productId AND f.isActive = true")
    boolean existsByUserIdAndProductIdAndIsActiveTrue(@Param("userId") Long userId, @Param("productId") Long productId);
}
