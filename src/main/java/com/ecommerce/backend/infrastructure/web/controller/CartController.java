package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.AddToCartDto;
import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CartDto;
import com.ecommerce.backend.application.dto.UpdateCartItemDto;
import com.ecommerce.backend.application.service.CartService;
import com.ecommerce.backend.infrastructure.security.CurrentUserService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/** Sepet — docs/API_CONTRACT.md §4.6 (her zaman çağıranın sepeti). */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<BaseResponseDto<CartDto>> getCart() {
        return ApiResponses.ok("Sepet getirildi", cartService.getCart(currentUserService.requireUserId()));
    }

    @PostMapping("/add")
    public ResponseEntity<BaseResponseDto<CartDto>> addToCart(@RequestBody AddToCartDto request) {
        CartDto cart = cartService.addToCart(currentUserService.requireUserId(), request);
        return ApiResponses.ok("Product added to cart successfully", cart);
    }

    @PutMapping("/update")
    public ResponseEntity<BaseResponseDto<CartDto>> updateCartItem(@RequestBody UpdateCartItemDto request) {
        CartDto cart = cartService.updateCartItem(currentUserService.requireUserId(), request);
        boolean removed = request.getQuantity() == null || request.getQuantity() <= 0;
        return ApiResponses.ok(removed ? "Cart item removed" : "Cart item updated successfully", cart);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<BaseResponseDto<Boolean>> removeFromCart(@PathVariable Long productId) {
        cartService.removeFromCart(currentUserService.requireUserId(), productId);
        return ApiResponses.ok("Product removed from cart successfully", true);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<BaseResponseDto<Boolean>> clearCart() {
        cartService.clearCart(currentUserService.requireUserId());
        return ApiResponses.ok("Cart cleared successfully", true);
    }

    @GetMapping("/count")
    public ResponseEntity<BaseResponseDto<Integer>> getItemCount() {
        return ApiResponses.ok("Cart item count retrieved successfully",
                cartService.getItemCount(currentUserService.requireUserId()));
    }

    @GetMapping("/total")
    public ResponseEntity<BaseResponseDto<BigDecimal>> getTotal() {
        return ApiResponses.ok("Sepet toplamı (kargo dahil)",
                cartService.getGrandTotal(currentUserService.requireUserId()));
    }
}
