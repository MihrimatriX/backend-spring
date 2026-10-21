package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.SupportMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    @EntityGraph(attributePaths = "user")
    List<SupportMessage> findByTicketIdInAndIsActiveTrueOrderByCreatedAtAscIdAsc(Collection<Long> ticketIds);
}
