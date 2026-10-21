package com.ecommerce.backend.infrastructure.messaging;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Published to RabbitMQ when an order is placed (inventory already updated in
 * DB).
 */
public record OrderCreatedEvent(
                Long orderId,
                Long userId,
                String orderNumber,
                BigDecimal totalAmount,
                long createdAtEpochMillis) implements Serializable {
}
