package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("SELECT f FROM Favorite f JOIN FETCH f.product p LEFT JOIN FETCH p.category "
            + "WHERE f.userId = :userId AND f.isActive = true ORDER BY f.createdAt DESC, f.id DESC")
    List<Favorite> findActiveByUserIdWithProduct(@Param("userId") Long userId);

    List<Favorite> findByUserIdAndProductIdAndIsActiveTrue(Long userId, Long productId);

    boolean existsByUserIdAndProductIdAndIsActiveTrue(Long userId, Long productId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Favorite f WHERE f.userId = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
