package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.ShoppingCart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    @EntityGraph("ShoppingCart.withItemsAndProducts")
    Optional<ShoppingCart> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
