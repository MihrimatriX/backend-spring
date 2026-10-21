package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    /** Varsayılan önce, sonra en yeni. */
    List<PaymentMethod> findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDescIdDesc(Long userId);

    Optional<PaymentMethod> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);

    List<PaymentMethod> findByUserIdAndIsDefaultTrueAndIsActiveTrue(Long userId);
}
