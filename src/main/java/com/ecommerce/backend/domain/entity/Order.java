package com.ecommerce.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity {

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

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status cannot exceed 50 characters")
    @Column(name = "status", nullable = false)
    private String status = "Pending"; // e.g., Pending, Processing, Shipped, Delivered, Cancelled

    @Column(name = "shipping_address_id")
    private Long shippingAddressId;

    @Column(name = "billing_address_id")
    private Long billingAddressId;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

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

    // Navigation property
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

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
