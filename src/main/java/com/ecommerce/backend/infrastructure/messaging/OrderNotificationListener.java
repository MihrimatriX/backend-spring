package com.ecommerce.backend.infrastructure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Demonstrates async processing: e-mail/SMS/analytics could subscribe to the
 * same routing key.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class OrderNotificationListener {

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("RabbitMQ consumer: order {} ({}) for user {} — total {}",
                event.orderId(), event.orderNumber(), event.userId(), event.totalAmount());
    }
}
