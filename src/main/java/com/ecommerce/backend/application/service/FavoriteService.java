package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.AddToFavoritesDto;
import com.ecommerce.backend.application.dto.FavoriteDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.Favorite;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.infrastructure.repository.FavoriteRepository;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Favoriler — docs/API_CONTRACT.md §4.7. Kaldırılan favori satırı silinir; böylece
 * PostgreSQL'deki aktif satırlara özel tekil indeks yeniden eklemeyi engellemez.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {

    private static final String ALREADY_FAVORITE = "Product already in favorites";

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    /** En yeni önce. */
    @Transactional(readOnly = true)
    public List<FavoriteDto> getUserFavorites(Long userId) {
        return favoriteRepository.findActiveByUserIdWithProduct(userId).stream()
                .map(f -> toDto(f, f.getProduct()))
                .toList();
    }

    public FavoriteDto addToFavorites(Long userId, AddToFavoritesDto request) {
        Product product = Optional.ofNullable(request.getProductId())
                .flatMap(productRepository::findById)
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .orElseThrow(() -> ApiException.badRequest("PRODUCT_NOT_FOUND", "Product not found or inactive"));
        if (favoriteRepository.existsByUserIdAndProductIdAndIsActiveTrue(userId, product.getId())) {
            throw ApiException.badRequest("ALREADY_FAVORITE", ALREADY_FAVORITE);
        }

        Favorite favorite = new Favorite(userId, product.getId());
        favorite.setIsActive(true);
        try {
            favorite = favoriteRepository.saveAndFlush(favorite);
        } catch (DataIntegrityViolationException ex) {
            // Aynı anda gelen ikinci istek tekil indekse takıldı.
            throw ApiException.badRequest("ALREADY_FAVORITE", ALREADY_FAVORITE);
        }
        return toDto(favorite, product);
    }

    public void removeFromFavorites(Long userId, Long productId) {
        List<Favorite> rows = favoriteRepository.findByUserIdAndProductIdAndIsActiveTrue(userId, productId);
        if (rows.isEmpty()) {
            throw ApiException.badRequest("NOT_FAVORITE", "Product not found in favorites");
        }
        favoriteRepository.deleteAll(rows);
    }

    @Transactional(readOnly = true)
    public boolean isProductInFavorites(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductIdAndIsActiveTrue(userId, productId);
    }

    public void clearFavorites(Long userId) {
        favoriteRepository.deleteAllByUserId(userId);
    }

    private static FavoriteDto toDto(Favorite favorite, Product product) {
        String category = product.getCategory() != null ? product.getCategory().getCategoryName() : null;
        Integer stock = product.getUnitInStock();
        return new FavoriteDto(
                favorite.getId(),
                favorite.getUserId(),
                product.getId(),
                product.getProductName(),
                product.getImageUrl(),
                product.getUnitPrice(),
                product.getDiscount(),
                category,
                stock != null && stock > 0,
                favorite.getCreatedAt());
    }
}
