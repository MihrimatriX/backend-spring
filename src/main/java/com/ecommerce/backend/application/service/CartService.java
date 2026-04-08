package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.domain.entity.ShoppingCart;
import com.ecommerce.backend.domain.entity.ShoppingCartItem;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import com.ecommerce.backend.infrastructure.repository.ShoppingCartRepository;
import com.ecommerce.backend.infrastructure.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final ProductRepository productRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public BaseResponseDto<CartDto> getCart() {
        try {
            Long userId = getCurrentUserId();
            return shoppingCartRepository.findByUserId(userId)
                    .map(c -> BaseResponseDto.success("Cart retrieved successfully", buildDto(userId, c)))
                    .orElseGet(() -> BaseResponseDto.success("Cart retrieved successfully", emptyCart(userId)));
        } catch (Exception e) {
            log.error("Error retrieving cart: {}", e.getMessage(), e);
            return BaseResponseDto.error("Error retrieving cart: " + e.getMessage());
        }
    }

    public BaseResponseDto<CartDto> addToCart(AddToCartDto addToCartDto) {
        try {
            Long userId = getCurrentUserId();
            Product product = productRepository.findById(addToCartDto.getProductId()).orElse(null);
            if (product == null || !Boolean.TRUE.equals(product.getIsActive())) {
                return BaseResponseDto.error("Product not found");
            }
            if (product.getUnitInStock() < addToCartDto.getQuantity()) {
                return BaseResponseDto.error("Insufficient stock");
            }

            ShoppingCart cart = shoppingCartRepository.findByUserId(userId).orElseGet(() -> {
                ShoppingCart c = new ShoppingCart();
                c.setUserId(userId);
                return shoppingCartRepository.saveAndFlush(c);
            });

            Optional<ShoppingCartItem> existing = cart.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(product.getId()))
                    .findFirst();

            if (existing.isPresent()) {
                ShoppingCartItem line = existing.get();
                int nextQty = line.getQuantity() + addToCartDto.getQuantity();
                if (nextQty > product.getUnitInStock()) {
                    return BaseResponseDto.error("Insufficient stock");
                }
                line.setQuantity(nextQty);
            } else {
                ShoppingCartItem line = new ShoppingCartItem();
                line.setCart(cart);
                line.setProduct(product);
                line.setQuantity(addToCartDto.getQuantity());
                cart.getItems().add(line);
            }

            shoppingCartRepository.save(cart);
            ShoppingCart refreshed = shoppingCartRepository.findByUserId(userId).orElse(cart);
            return BaseResponseDto.success("Item added to cart successfully", buildDto(userId, refreshed));
        } catch (Exception e) {
            log.error("Error adding item to cart: {}", e.getMessage(), e);
            return BaseResponseDto.error("Error adding item to cart: " + e.getMessage());
        }
    }

    public BaseResponseDto<CartDto> updateCartItem(UpdateCartItemDto updateCartItemDto) {
        try {
            Long userId = getCurrentUserId();
            Product product = productRepository.findById(updateCartItemDto.getProductId()).orElse(null);
            if (product == null || !Boolean.TRUE.equals(product.getIsActive())) {
                return BaseResponseDto.error("Product not found");
            }
            if (updateCartItemDto.getQuantity() <= 0) {
                return BaseResponseDto.error("Quantity must be greater than 0");
            }
            if (product.getUnitInStock() < updateCartItemDto.getQuantity()) {
                return BaseResponseDto.error("Insufficient stock");
            }

            ShoppingCart cart = shoppingCartRepository.findByUserId(userId)
                    .orElse(null);
            if (cart == null) {
                return BaseResponseDto.error("Cart item not found");
            }

            Optional<ShoppingCartItem> line = cart.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(product.getId()))
                    .findFirst();
            if (line.isEmpty()) {
                return BaseResponseDto.error("Cart item not found");
            }

            line.get().setQuantity(updateCartItemDto.getQuantity());
            shoppingCartRepository.save(cart);
            ShoppingCart refreshed = shoppingCartRepository.findByUserId(userId).orElse(cart);
            return BaseResponseDto.success("Cart item updated successfully", buildDto(userId, refreshed));
        } catch (Exception e) {
            log.error("Error updating cart item: {}", e.getMessage(), e);
            return BaseResponseDto.error("Error updating cart item: " + e.getMessage());
        }
    }

    public BaseResponseDto<CartDto> removeFromCart(Long productId) {
        try {
            Long userId = getCurrentUserId();
            ShoppingCart cart = shoppingCartRepository.findByUserId(userId).orElse(null);
            if (cart == null) {
                return BaseResponseDto.success("Item removed from cart successfully", emptyCart(userId));
            }
            cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
            if (cart.getItems().isEmpty()) {
                shoppingCartRepository.delete(cart);
                return BaseResponseDto.success("Item removed from cart successfully", emptyCart(userId));
            }
            shoppingCartRepository.save(cart);
            ShoppingCart refreshed = shoppingCartRepository.findByUserId(userId).orElse(cart);
            return BaseResponseDto.success("Item removed from cart successfully", buildDto(userId, refreshed));
        } catch (Exception e) {
            log.error("Error removing item from cart: {}", e.getMessage(), e);
            return BaseResponseDto.error("Error removing item from cart: " + e.getMessage());
        }
    }

    public BaseResponseDto<String> clearCart() {
        try {
            Long userId = getCurrentUserId();
            shoppingCartRepository.deleteByUserId(userId);
            return BaseResponseDto.success("Cart cleared successfully", "Cart cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing cart: {}", e.getMessage(), e);
            return BaseResponseDto.error("Error clearing cart: " + e.getMessage());
        }
    }

    /**
     * Clears persisted cart after a successful order (same transaction as order).
     */
    public void clearCartForUser(Long userId) {
        shoppingCartRepository.deleteByUserId(userId);
    }

    private Long getCurrentUserId() {
        return currentUserService.requireUserId();
    }

    private static CartDto emptyCart(Long userId) {
        CartDto cart = new CartDto();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setTotalItems(0);
        cart.setTotalAmount(BigDecimal.ZERO);
        return cart;
    }

    private static CartDto buildDto(Long userId, ShoppingCart cart) {
        CartDto dto = new CartDto();
        dto.setUserId(userId);
        List<CartItemDto> items = new ArrayList<>();
        int totalQty = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (ShoppingCartItem line : cart.getItems()) {
            Product p = line.getProduct();
            CartItemDto row = new CartItemDto();
            row.setProductId(p.getId());
            row.setProductName(p.getProductName());
            row.setProductImageUrl(p.getImageUrl());
            row.setUnitPrice(p.getUnitPrice());
            row.setQuantity(line.getQuantity());
            row.setTotalPrice(p.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
            row.setDiscount(p.getDiscount() != null ? p.getDiscount() : 0);
            row.setUnitInStock(p.getUnitInStock());
            row.setQuantityPerUnit(p.getQuantityPerUnit());
            items.add(row);
            totalQty += line.getQuantity();
            total = total.add(row.getTotalPrice());
        }
        dto.setItems(items);
        dto.setTotalItems(totalQty);
        dto.setTotalAmount(total);
        return dto;
    }
}
