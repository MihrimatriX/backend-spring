package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.SubCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    @Query("SELECT s FROM SubCategory s JOIN FETCH s.category c WHERE s.isActive = true "
            + "ORDER BY c.categoryName, s.subCategoryName")
    List<SubCategory> findAllActive();

    @EntityGraph(attributePaths = "category")
    List<SubCategory> findByCategoryIdAndIsActiveTrueOrderBySubCategoryNameAsc(Long categoryId);

    @EntityGraph(attributePaths = "category")
    Optional<SubCategory> findWithCategoryById(Long id);

    @EntityGraph(attributePaths = "category")
    Optional<SubCategory> findWithCategoryByIdAndIsActiveTrue(Long id);

    /** (kategori, ad) benzersizdir; kontrol harf duyarsızdır ve pasif kayıtları da kapsar. */
    boolean existsByCategoryIdAndSubCategoryNameIgnoreCase(Long categoryId, String subCategoryName);

    boolean existsByCategoryIdAndSubCategoryNameIgnoreCaseAndIdNot(Long categoryId, String subCategoryName, Long id);
}
