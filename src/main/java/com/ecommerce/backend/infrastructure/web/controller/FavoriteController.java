package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.AddToFavoritesDto;
import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.FavoriteDto;
import com.ecommerce.backend.application.service.FavoriteService;
import com.ecommerce.backend.infrastructure.security.CurrentUserService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Favoriler — docs/API_CONTRACT.md §4.7 (her zaman çağıranın favorileri). */
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<BaseResponseDto<List<FavoriteDto>>> getUserFavorites() {
        return ApiResponses.ok("Favorites retrieved successfully",
                favoriteService.getUserFavorites(currentUserService.requireUserId()));
    }

    @PostMapping("/add")
    public ResponseEntity<BaseResponseDto<FavoriteDto>> addToFavorites(@RequestBody AddToFavoritesDto request) {
        return ApiResponses.ok("Product added to favorites",
                favoriteService.addToFavorites(currentUserService.requireUserId(), request));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<BaseResponseDto<String>> removeFromFavorites(@PathVariable Long productId) {
        favoriteService.removeFromFavorites(currentUserService.requireUserId(), productId);
        return ApiResponses.ok("Product removed from favorites", "Product removed from favorites");
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<BaseResponseDto<Boolean>> isProductInFavorites(@PathVariable Long productId) {
        return ApiResponses.ok("Favorite status retrieved",
                favoriteService.isProductInFavorites(currentUserService.requireUserId(), productId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<BaseResponseDto<String>> clearFavorites() {
        favoriteService.clearFavorites(currentUserService.requireUserId());
        return ApiResponses.ok("Favorites cleared successfully", "Favorites cleared successfully");
    }
}
