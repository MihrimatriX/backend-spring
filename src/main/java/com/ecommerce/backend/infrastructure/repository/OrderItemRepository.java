package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderIdAndIsActiveTrue(Long orderId);

    List<OrderItem> findByOrderId(Long orderId);
}
