package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    Optional<Order> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findAllOrderByCreatedAtDesc();

    Page<Order> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.orderNumber = :orderNumber AND o.isActive = true")
    Optional<Order> findByOrderNumberAndIsActiveTrue(@Param("orderNumber") String orderNumber);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.isActive = true")
    Long countByUserIdAndIsActiveTrue(@Param("userId") Long userId);
}
