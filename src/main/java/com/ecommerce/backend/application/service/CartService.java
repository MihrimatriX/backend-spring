package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.AddToCartDto;
import com.ecommerce.backend.application.dto.CartDto;
import com.ecommerce.backend.application.dto.CartItemDto;
import com.ecommerce.backend.application.dto.UpdateCartItemDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.application.support.CheckoutPricing;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.domain.entity.ShoppingCart;
import com.ecommerce.backend.domain.entity.ShoppingCartItem;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import com.ecommerce.backend.infrastructure.repository.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Sunucu tarafı sepet (docs/API_CONTRACT.md §4.6, §5.1). Pasif ürünlerin satırları sepette
 * gösterilmez; fiyatlar indirimli satış fiyatıdır.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ProductRepository productRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CheckoutPricing pricing;

    @Transactional(readOnly = true)
    public CartDto getCart(Long userId) {
        return buildDto(userId, shoppingCartRepository.findByUserId(userId).orElse(null));
    }

    @Transactional(readOnly = true)
    public int getItemCount(Long userId) {
        return getCart(userId).totalItems();
    }

    @Transactional(readOnly = true)
    public BigDecimal getGrandTotal(Long userId) {
        return getCart(userId).grandTotal();
    }

    /** Miktar mevcut satıra eklenir ve stokla sınırlanır. */
    public CartDto addToCart(Long userId, AddToCartDto request) {
        int quantity = request.getQuantity() == null ? 0 : request.getQuantity();
        if (quantity <= 0) {
            throw ApiException.badRequest("INVALID_QUANTITY", "Adet 0'dan büyük olmalıdır.");
        }
        Product product = requireActiveProduct(request.getProductId());
        int stock = product.getUnitInStock();
        if (stock < 1) {
            throw ApiException.badRequest("OUT_OF_STOCK", "Bu ürün stokta yok.");
        }

        ShoppingCart cart = shoppingCartRepository.findByUserId(userId).orElseGet(() -> {
            ShoppingCart created = new ShoppingCart();
            created.setUserId(userId);
            return created;
        });
        Optional<ShoppingCartItem> existing = findLine(cart, product.getId());
        long requested = (long) existing.map(ShoppingCartItem::getQuantity).orElse(0) + quantity;
        int next = (int) Math.min(requested, stock);
        if (existing.isPresent()) {
            existing.get().setQuantity(next);
        } else {
            ShoppingCartItem line = new ShoppingCartItem();
            line.setCart(cart);
            line.setProduct(product);
            line.setQuantity(next);
            cart.getItems().add(line);
        }
        if (cart.getId() == null) {
            shoppingCartRepository.save(cart);
        }
        return buildDto(userId, cart);
    }

    /** Miktarı ayarlar; {@code quantity ≤ 0} satırı siler. */
    public CartDto updateCartItem(Long userId, UpdateCartItemDto request) {
        int quantity = request.getQuantity() == null ? 0 : request.getQuantity();
        if (quantity <= 0) {
            removeFromCart(userId, request.getProductId());
            return getCart(userId);
        }
        Product product = requireActiveProduct(request.getProductId());
        if (product.getUnitInStock() < quantity) {
            throw ApiException.badRequest("INSUFFICIENT_STOCK", "Stokta yeterli ürün yok. Miktarı düşürün.");
        }
        ShoppingCart cart = shoppingCartRepository.findByUserId(userId).orElse(null);
        ShoppingCartItem line = cart == null ? null : findLine(cart, product.getId()).orElse(null);
        if (line == null) {
            throw ApiException.badRequest("NOT_IN_CART", "Bu ürün sepetinizde yok.");
        }
        line.setQuantity(quantity);
        return buildDto(userId, cart);
    }

    /** İdempotent: ürün sepette yoksa da başarılıdır. */
    public void removeFromCart(Long userId, Long productId) {
        if (productId == null) {
            return;
        }
        shoppingCartRepository.findByUserId(userId)
                .ifPresent(cart -> cart.getItems().removeIf(line -> productId.equals(line.getProduct().getId())));
    }

    /** Sepeti boşaltır (sipariş sonrası da aynı transaction içinde çağrılır). */
    public void clearCart(Long userId) {
        shoppingCartRepository.deleteByUserId(userId);
    }

    private Product requireActiveProduct(Long productId) {
        return Optional.ofNullable(productId)
                .flatMap(productRepository::findById)
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .orElseThrow(() -> ApiException.badRequest("PRODUCT_NOT_FOUND", "Ürün bulunamadı veya satışta değil."));
    }

    private static Optional<ShoppingCartItem> findLine(ShoppingCart cart, Long productId) {
        return cart.getItems().stream()
                .filter(line -> productId.equals(line.getProduct().getId()))
                .findFirst();
    }

    private CartDto buildDto(Long userId, ShoppingCart cart) {
        List<CartItemDto> items = new ArrayList<>();
        int totalItems = 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        List<ShoppingCartItem> lines = cart == null ? List.of() : cart.getItems().stream()
                .filter(line -> Boolean.TRUE.equals(line.getProduct().getIsActive()))
                .sorted(Comparator.comparing(ShoppingCartItem::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        for (ShoppingCartItem line : lines) {
            Product p = line.getProduct();
            int quantity = line.getQuantity();
            int stock = p.getUnitInStock() == null ? 0 : p.getUnitInStock();
            BigDecimal unit = CheckoutPricing.salePrice(p.getUnitPrice(), p.getDiscount());
            BigDecimal lineTotal = CheckoutPricing.lineTotal(unit, Math.max(0, Math.min(quantity, stock)));
            items.add(new CartItemDto(p.getId(), p.getProductName(), p.getImageUrl(), unit, quantity, lineTotal,
                    stock > 0 && stock >= quantity));
            totalItems += quantity;
            subtotal = subtotal.add(lineTotal);
        }
        CheckoutPricing.Quote quote = pricing.quote(subtotal, items.isEmpty());
        return new CartDto(userId, items, totalItems, quote.subtotal(), quote.shippingFee(), quote.grandTotal(),
                quote.freeShippingRemaining());
    }
}
