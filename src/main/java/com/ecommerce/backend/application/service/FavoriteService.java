package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.AddToFavoritesDto;
import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.FavoriteDto;
import com.ecommerce.backend.domain.entity.Favorite;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.infrastructure.repository.FavoriteRepository;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, ProductRepository productRepository) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public BaseResponseDto<List<FavoriteDto>> getUserFavorites(Long userId) {
        try {
            List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
            List<FavoriteDto> favoriteDtos = favorites.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Favorites retrieved successfully", favoriteDtos);
        } catch (Exception e) {
            log.error("Error retrieving favorites for user {}: {}", userId, e.getMessage(), e);
            return BaseResponseDto.error("Error retrieving favorites: " + e.getMessage());
        }
    }

    @Transactional
    public BaseResponseDto<FavoriteDto> addToFavorites(Long userId, AddToFavoritesDto addToFavoritesDto) {
        try {
            // Check if product exists and is active
            Product product = productRepository.findById(addToFavoritesDto.getProductId())
                    .filter(Product::getIsActive)
                    .orElse(null);

            if (product == null) {
                return BaseResponseDto.error("Product not found or inactive");
            }

            // Check if already in favorites
            Favorite existingFavorite = favoriteRepository
                    .findByUserIdAndProductId(userId, addToFavoritesDto.getProductId()).orElse(null);
            if (existingFavorite != null) {
                return BaseResponseDto.error("Product already in favorites");
            }

            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(addToFavoritesDto.getProductId());
            favorite.setCreatedAt(LocalDateTime.now());

            Favorite savedFavorite = favoriteRepository.save(favorite);
            return BaseResponseDto.success("Product added to favorites", convertToDto(savedFavorite));
        } catch (Exception e) {
            log.error("Error adding product {} to favorites for user {}: {}", addToFavoritesDto.getProductId(), userId,
                    e.getMessage(), e);
            return BaseResponseDto.error("Error adding to favorites: " + e.getMessage());
        }
    }

    @Transactional
    public BaseResponseDto<String> removeFromFavorites(Long userId, Long productId) {
        try {
            Favorite favorite = favoriteRepository.findByUserIdAndProductId(userId, productId).orElse(null);
            if (favorite == null) {
                return BaseResponseDto.error("Product not found in favorites");
            }

            favoriteRepository.delete(favorite);
            return BaseResponseDto.success("Product removed from favorites", "Product removed from favorites");
        } catch (Exception e) {
            log.error("Error removing product {} from favorites for user {}: {}", productId, userId, e.getMessage(), e);
            return BaseResponseDto.error("Error removing from favorites: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public BaseResponseDto<Boolean> isProductInFavorites(Long userId, Long productId) {
        try {
            boolean isInFavorites = favoriteRepository.findByUserIdAndProductId(userId, productId).isPresent();
            return BaseResponseDto.success("Favorite status retrieved", isInFavorites);
        } catch (Exception e) {
            log.error("Error checking favorite status for product {} and user {}: {}", productId, userId,
                    e.getMessage(), e);
            return BaseResponseDto.error("Error checking favorite status: " + e.getMessage());
        }
    }

    @Transactional
    public BaseResponseDto<String> clearFavorites(Long userId) {
        try {
            favoriteRepository.deleteByUserId(userId);
            return BaseResponseDto.success("Favorites cleared successfully", "Favorites cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing favorites for user {}: {}", userId, e.getMessage(), e);
            return BaseResponseDto.error("Error clearing favorites: " + e.getMessage());
        }
    }

    private FavoriteDto convertToDto(Favorite favorite) {
        FavoriteDto dto = new FavoriteDto();
        dto.setId(favorite.getId());
        dto.setUserId(favorite.getUserId());
        dto.setProductId(favorite.getProductId());
        dto.setProductName(favorite.getProduct() != null ? favorite.getProduct().getProductName() : "");
        dto.setProductImageUrl(favorite.getProduct() != null ? favorite.getProduct().getImageUrl() : null);
        dto.setProductPrice(favorite.getProduct() != null ? favorite.getProduct().getUnitPrice() : null);
        dto.setProductDiscount(favorite.getProduct() != null ? BigDecimal.valueOf(favorite.getProduct().getDiscount())
                : BigDecimal.ZERO);
        dto.setProductCategory(favorite.getProduct() != null && favorite.getProduct().getCategory() != null
                ? favorite.getProduct().getCategory().getCategoryName()
                : null);
        dto.setProductInStock(favorite.getProduct() != null ? favorite.getProduct().getUnitInStock() > 0 : false);
        dto.setCreatedAt(favorite.getCreatedAt());
        return dto;
    }
}