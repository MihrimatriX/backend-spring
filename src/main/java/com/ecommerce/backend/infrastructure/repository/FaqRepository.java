package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Faq;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByIsPublishedTrueAndIsActiveTrue(Pageable pageable);

    List<Faq> findByCategoryAndIsPublishedTrueAndIsActiveTrue(String category, Pageable pageable);
}
