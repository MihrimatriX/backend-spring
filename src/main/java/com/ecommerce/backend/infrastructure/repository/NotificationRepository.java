package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Sıralama {@code Pageable} ile verilir (en yeni önce). */
    List<Notification> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);

    List<Notification> findByUserIdAndIsActiveTrueAndIsReadFalse(Long userId);

    Optional<Notification> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);

    long countByUserIdAndIsActiveTrue(Long userId);

    long countByUserIdAndIsActiveTrueAndIsReadFalse(Long userId);
}
