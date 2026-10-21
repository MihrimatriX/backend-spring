package com.ecommerce.backend.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sipariş (docs/API_CONTRACT.md §3). {@code totalAmount} kargo dahildir, {@code subtotalAmount}
 * ürün toplamıdır. Adresler tam {@link AddressDto}, ödeme yöntemi maskeli {@link PaymentMethodDto}.
 */
@Data
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String userName;
    private String userEmail;
    private List<OrderItemDto> items = new ArrayList<>();
    private BigDecimal subtotalAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String status;
    private String notes;
    private AddressDto shippingAddress;
    private AddressDto billingAddress;
    private PaymentMethodDto paymentMethod;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime estimatedDeliveryAt;
    private String cancelReason;
    private String returnReason;
    private LocalDateTime returnRequestedAt;
    /** Demo lojistik açıksa ve sipariş ilerletilebilirse {@code DEMO_ADVANCE_FULFILLMENT}, aksi {@code null}. */
    private String demoNextAction;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
