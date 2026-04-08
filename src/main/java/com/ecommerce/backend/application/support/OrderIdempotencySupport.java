package com.ecommerce.backend.application.support;

import com.ecommerce.backend.infrastructure.repository.OrderIdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderIdempotencySupport {

    private final OrderIdempotencyRepository orderIdempotencyRepository;

    /**
     * Runs in a new transaction so we always see committed rows from other threads.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<Long> findExistingOrderId(Long userId, String idempotencyKey) {
        return orderIdempotencyRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                .map(r -> r.getOrderId());
    }
}
