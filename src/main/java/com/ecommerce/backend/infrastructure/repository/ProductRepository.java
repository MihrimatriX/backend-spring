package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

       List<Product> findByIsActiveTrue();

       List<Product> findByCategoryIdAndIsActiveTrue(Long categoryId);

       @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
                     "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
                     "(:minPrice IS NULL OR p.unitPrice >= :minPrice) AND " +
                     "(:maxPrice IS NULL OR p.unitPrice <= :maxPrice)")
       Page<Product> findWithFiltersWithoutSearch(@Param("categoryId") Long categoryId,
                     @Param("minPrice") BigDecimal minPrice,
                     @Param("maxPrice") BigDecimal maxPrice,
                     Pageable pageable);

       @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
                     "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
                     "(:minPrice IS NULL OR p.unitPrice >= :minPrice) AND " +
                     "(:maxPrice IS NULL OR p.unitPrice <= :maxPrice) AND " +
                     "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                     "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
       Page<Product> findWithFiltersWithSearch(@Param("categoryId") Long categoryId,
                     @Param("minPrice") BigDecimal minPrice,
                     @Param("maxPrice") BigDecimal maxPrice,
                     @Param("searchTerm") String searchTerm,
                     Pageable pageable);

       @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.discount > 20")
       List<Product> findFeaturedProducts();

       @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.discount > 0")
       List<Product> findDiscountedProducts();

       @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
                     "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                     "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
       List<Product> searchProducts(@Param("searchTerm") String searchTerm);

       boolean existsByProductNameAndIsActiveTrue(String productName);
}
