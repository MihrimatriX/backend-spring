package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(Long userId);

    Optional<PaymentMethod> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);

    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.userId = :userId AND pm.isDefault = true AND pm.isActive = true")
    List<PaymentMethod> findByUserIdAndIsDefaultTrueAndIsActiveTrue(@Param("userId") Long userId);
}
