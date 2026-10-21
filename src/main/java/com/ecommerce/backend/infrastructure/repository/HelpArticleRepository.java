package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.HelpArticle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HelpArticleRepository extends JpaRepository<HelpArticle, Long> {

    List<HelpArticle> findByIsPublishedTrueAndIsActiveTrue(Pageable pageable);

    List<HelpArticle> findByCategoryAndIsPublishedTrueAndIsActiveTrue(String category, Pageable pageable);

    Optional<HelpArticle> findByIdAndIsPublishedTrueAndIsActiveTrue(Long id);
}
