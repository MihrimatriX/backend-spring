package com.ecommerce.backend.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String userName;
    private String userEmail;
    private List<OrderItemDto> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private String status;
    private AddressDto shippingAddress;
    private AddressDto billingAddress;
    private PaymentMethodDto paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String trackingNumber;
    private String carrier;
    private LocalDateTime shippedAt;
    private LocalDateTime estimatedDeliveryAt;
    private String cancelReason;
    private String returnReason;
    private LocalDateTime returnRequestedAt;

    /**
     * İstemci demo butonu gösterebilir: {@code DEMO_ADVANCE_FULFILLMENT} veya null.
     */
    private String demoNextAction;
}
