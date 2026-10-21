package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.domain.entity.SubCategory;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Ürün sorguları. Liste sorguları kategori ve alt kategoriyi aynı sorguda getirir (N+1 yok);
 * filtreler {@link ProductSpecifications} ile kurulur.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Override
    @EntityGraph(attributePaths = { "category", "subCategory" })
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "category", "subCategory" })
    List<Product> findAll(Specification<Product> spec, Sort sort);

    @EntityGraph(attributePaths = { "category", "subCategory" })
    Optional<Product> findWithCatalogById(Long id);

    @EntityGraph(attributePaths = { "category", "subCategory" })
    Optional<Product> findWithCatalogByIdAndIsActiveTrue(Long id);

    /** Öne çıkanlar: indirim > %20 veya {@code since} sonrasında eklenmiş; en yeni önce. */
    @EntityGraph(attributePaths = { "category", "subCategory" })
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (p.discount > 20 OR p.createdAt >= :since) "
            + "ORDER BY p.createdAt DESC, p.id DESC")
    List<Product> findFeatured(@Param("since") LocalDateTime since, Limit limit);

    /** İndirimli ürünler: indirim oranı yüksek olan önce. */
    @EntityGraph(attributePaths = { "category", "subCategory" })
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.discount > 0 ORDER BY p.discount DESC, p.id ASC")
    List<Product> findDiscounted(Limit limit);

    /** Alt kategori atanmamış ürünler (alt kategori tohumlaması için): [id, productName]. */
    @Query("SELECT p.id, p.productName FROM Product p WHERE p.category.id = :categoryId AND p.subCategory IS NULL "
            + "ORDER BY p.id")
    List<Object[]> findUnassignedNames(@Param("categoryId") Long categoryId);

    @Modifying
    @Query("UPDATE Product p SET p.subCategory = :subCategory WHERE p.id IN :ids")
    int assignSubCategory(@Param("subCategory") SubCategory subCategory, @Param("ids") Collection<Long> ids);
}
