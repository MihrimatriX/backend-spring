package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.SupportTicket;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    @EntityGraph(attributePaths = "user")
    List<SupportTicket> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    List<SupportTicket> findByIsActiveTrue(Pageable pageable);
}
