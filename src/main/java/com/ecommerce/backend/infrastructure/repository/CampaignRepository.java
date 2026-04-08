package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

       List<Campaign> findByIsActiveTrue();

       List<Campaign> findByIsActiveTrueOrderByCreatedAtDesc();

       List<Campaign> findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByCreatedAtDesc(
                     LocalDateTime startDate, LocalDateTime endDate);

       java.util.Optional<Campaign> findByIdAndIsActiveTrue(Long id);

       @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND " +
                     "c.startDate <= :now AND c.endDate >= :now")
       List<Campaign> findActiveCampaigns(@Param("now") LocalDateTime now);

       @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND " +
                     "c.startDate <= CURRENT_TIMESTAMP AND c.endDate >= CURRENT_TIMESTAMP")
       List<Campaign> findCurrentActiveCampaigns();
}
