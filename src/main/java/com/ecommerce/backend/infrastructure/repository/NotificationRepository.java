package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isActive = true")
    Long countByUserIdAndIsActiveTrue(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isActive = true AND n.isRead = false")
    Long countByUserIdAndIsActiveTrueAndIsReadFalse(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isActive = true ORDER BY n.createdAt DESC")
    List<Notification> findTop5ByUserIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("userId") Long userId);
}
