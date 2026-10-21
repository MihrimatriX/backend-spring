package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.OrderIdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderIdempotencyRepository extends JpaRepository<OrderIdempotencyRecord, Long> {

    Optional<OrderIdempotencyRecord> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
}
