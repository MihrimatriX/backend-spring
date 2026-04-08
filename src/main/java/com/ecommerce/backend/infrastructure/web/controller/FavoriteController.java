package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.AddToFavoritesDto;
import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.FavoriteDto;
import com.ecommerce.backend.application.service.FavoriteService;
import com.ecommerce.backend.infrastructure.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final JwtUtil jwtUtil;

    public FavoriteController(FavoriteService favoriteService, JwtUtil jwtUtil) {
        this.favoriteService = favoriteService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<BaseResponseDto<List<FavoriteDto>>> getUserFavorites(HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(request);
        BaseResponseDto<List<FavoriteDto>> response = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<BaseResponseDto<FavoriteDto>> addToFavorites(
            @Valid @RequestBody AddToFavoritesDto addToFavoritesDto, HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(request);
        BaseResponseDto<FavoriteDto> response = favoriteService.addToFavorites(userId, addToFavoritesDto);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<BaseResponseDto<String>> removeFromFavorites(@PathVariable Long productId,
            HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(request);
        BaseResponseDto<String> response = favoriteService.removeFromFavorites(userId, productId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<BaseResponseDto<Boolean>> isProductInFavorites(@PathVariable Long productId,
            HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(request);
        BaseResponseDto<Boolean> response = favoriteService.isProductInFavorites(userId, productId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<BaseResponseDto<String>> clearFavorites(HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(request);
        BaseResponseDto<String> response = favoriteService.clearFavorites(userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}