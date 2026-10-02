package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByIsActiveTrueOrderByCreatedAtDescIdDesc();

    /** Aktif ve {@code startDate ≤ now ≤ endDate}; en yeni önce. */
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.startDate <= :now AND c.endDate >= :now "
            + "ORDER BY c.createdAt DESC, c.id DESC")
    List<Campaign> findRunning(@Param("now") LocalDateTime now);

    Optional<Campaign> findByIdAndIsActiveTrue(Long id);
}
