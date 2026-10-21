package com.ecommerce.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sipariş. Durum değerleri düz string'tir (docs/API_CONTRACT.md §1):
 * {@code Pending, Processing, Shipped, Delivered, Cancelled, ReturnRequested, Returned}.
 * Adres/ödeme/kullanıcı ilişkileri salt okunurdur; yazma id kolonları üzerinden yapılır.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order extends BaseEntity {

    /** Eşzamanlı durum değişikliklerinde (iptal + yönetici) stok iadesinin iki kez yapılmasını önler. */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @NotBlank(message = "Order number is required")
    @Size(max = 50, message = "Order number cannot exceed 50 characters")
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /** Ürün satırlarının toplamı (kargo hariç). */
    @NotNull
    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    /** Ödenecek toplam (ara toplam + kargo). */
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status cannot exceed 50 characters")
    @Column(name = "status", nullable = false)
    private String status = "Pending";

    @Column(name = "shipping_address_id")
    private Long shippingAddressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", insertable = false, updatable = false)
    private Address shippingAddress;

    @Column(name = "billing_address_id")
    private Long billingAddressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_address_id", insertable = false, updatable = false)
    private Address billingAddress;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", insertable = false, updatable = false)
    private PaymentMethod paymentMethod;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Size(max = 64)
    @Column(name = "tracking_number", length = 64)
    private String trackingNumber;

    @Size(max = 64)
    @Column(name = "carrier", length = 64)
    private String carrier;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "estimated_delivery_at")
    private LocalDateTime estimatedDeliveryAt;

    @Size(max = 500)
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Size(max = 500)
    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Column(name = "return_requested_at")
    private LocalDateTime returnRequestedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
