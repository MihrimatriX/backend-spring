package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.application.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<BaseResponseDto<CartDto>> getCart() {
        log.info("Getting cart for current user");
        BaseResponseDto<CartDto> response = cartService.getCart();

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<BaseResponseDto<CartDto>> addToCart(@Valid @RequestBody AddToCartDto addToCartDto) {
        log.info("Adding product {} to cart with quantity {}", addToCartDto.getProductId(), addToCartDto.getQuantity());
        BaseResponseDto<CartDto> response = cartService.addToCart(addToCartDto);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<BaseResponseDto<CartDto>> updateCartItem(
            @Valid @RequestBody UpdateCartItemDto updateCartItemDto) {
        log.info("Updating cart item for product {} with quantity {}", updateCartItemDto.getProductId(),
                updateCartItemDto.getQuantity());
        BaseResponseDto<CartDto> response = cartService.updateCartItem(updateCartItemDto);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<BaseResponseDto<CartDto>> removeFromCart(@PathVariable Long productId) {
        log.info("Removing product {} from cart", productId);
        BaseResponseDto<CartDto> response = cartService.removeFromCart(productId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<BaseResponseDto<String>> clearCart() {
        log.info("Clearing cart for current user");
        BaseResponseDto<String> response = cartService.clearCart();

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
