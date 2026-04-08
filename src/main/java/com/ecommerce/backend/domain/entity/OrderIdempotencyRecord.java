package com.ecommerce.backend.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Maps a user + Idempotency-Key header to a committed order (double-submit /
 * network retry safety).
 */
@Entity
@Table(name = "order_idempotency_keys", uniqueConstraints = @UniqueConstraint(name = "uq_order_idem_user_key", columnNames = {
        "user_id", "idempotency_key" }))
@Getter
@Setter
@NoArgsConstructor
public class OrderIdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public OrderIdempotencyRecord(Long userId, String idempotencyKey, Long orderId) {
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
        this.orderId = orderId;
        this.createdAt = Instant.now();
    }
}
