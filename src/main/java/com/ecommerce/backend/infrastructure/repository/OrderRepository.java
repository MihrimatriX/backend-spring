package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /** DTO için gereken ilişkiler tek sorguda yüklenir. */
    String DETAILS = "SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product "
            + "LEFT JOIN FETCH o.user LEFT JOIN FETCH o.shippingAddress LEFT JOIN FETCH o.billingAddress "
            + "LEFT JOIN FETCH o.paymentMethod ";

    @Query(DETAILS + "WHERE o.id = :id")
    Optional<Order> findDetailedById(@Param("id") Long id);

    @Query(DETAILS + "WHERE o.userId = :userId ORDER BY o.createdAt DESC, o.id DESC")
    List<Order> findDetailedByUserId(@Param("userId") Long userId);

    @Query(DETAILS + "WHERE o.id IN :ids ORDER BY o.createdAt DESC, o.id DESC")
    List<Order> findDetailedByIdIn(@Param("ids") Collection<Long> ids);

    /** Yönetici listesi: önce sayfadaki id'ler (koleksiyon fetch'i ile sayfalama yapılmaz). */
    @Query("SELECT o.id FROM Order o ORDER BY o.createdAt DESC, o.id DESC")
    List<Long> findIdsNewestFirst(Pageable pageable);
}
