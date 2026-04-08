package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsActiveTrue();

    List<Category> findByIsActiveTrueOrderByCategoryNameAsc();

    Page<Category> findByIsActiveTrue(Pageable pageable);

    Optional<Category> findByIdAndIsActiveTrue(Long id);

    boolean existsByCategoryNameAndIsActiveTrue(String categoryName);

    boolean existsByCategoryNameAndIdNotAndIsActiveTrue(String categoryName, Long id);
}
