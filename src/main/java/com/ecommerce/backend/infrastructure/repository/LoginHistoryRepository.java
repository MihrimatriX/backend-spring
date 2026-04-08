package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    Page<LoginHistory> findByUserIdAndIsActiveTrueOrderByLoginAtDesc(Long userId, Pageable pageable);

    @Query("SELECT lh FROM LoginHistory lh WHERE lh.userId = :userId AND lh.isActive = true ORDER BY lh.loginAt DESC")
    List<LoginHistory> findTop5ByUserIdAndIsActiveTrueOrderByLoginAtDesc(@Param("userId") Long userId);
}
