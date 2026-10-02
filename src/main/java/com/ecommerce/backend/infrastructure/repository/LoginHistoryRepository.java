package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.LoginHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    /** Sıralama {@code Pageable} ile verilir (en yeni önce). */
    List<LoginHistory> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);

    Optional<LoginHistory> findFirstByUserIdAndIsActiveTrueAndIsSuccessfulTrueOrderByLoginAtDescIdDesc(Long userId);
}
