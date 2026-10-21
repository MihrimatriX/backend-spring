package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Ürün listesi filtreleri (docs/API_CONTRACT.md §4.2). Yalnızca aktif ürünler; verilmeyen filtre uygulanmaz.
 */
public final class ProductSpecifications {

    private static final char LIKE_ESCAPE = '!';

    private ProductSpecifications() {
    }

    public static Specification<Product> activeMatching(Long categoryId, Long subCategoryId, BigDecimal minPrice,
            BigDecimal maxPrice, String searchTerm) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.<Boolean>get("isActive")));
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (subCategoryId != null) {
                predicates.add(cb.equal(root.get("subCategory").get("id"), subCategoryId));
            }
            // Fiyat filtreleri indirimsiz liste fiyatına uygulanır.
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("unitPrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("unitPrice"), maxPrice));
            }
            if (StringUtils.hasText(searchTerm)) {
                String pattern = containsPattern(searchTerm);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("productName")), pattern, LIKE_ESCAPE),
                        cb.like(cb.lower(root.<String>get("description")), pattern, LIKE_ESCAPE)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /** Büyük/küçük harf duyarsız "içerir" kalıbı; {@code %} ve {@code _} karakterleri düz metin sayılır. */
    static String containsPattern(String searchTerm) {
        String term = searchTerm.trim().toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + term + "%";
    }
}
