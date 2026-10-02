package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsActiveTrueOrderByCategoryNameAsc();

    Optional<Category> findByIdAndIsActiveTrue(Long id);

    /** Ad benzersizliği harf duyarsızdır ve pasif kategorileri de kapsar (veritabanında benzersiz indeks var). */
    boolean existsByCategoryNameIgnoreCase(String categoryName);

    boolean existsByCategoryNameIgnoreCaseAndIdNot(String categoryName, Long id);
}
