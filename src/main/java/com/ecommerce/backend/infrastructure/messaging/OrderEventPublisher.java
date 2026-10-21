package com.ecommerce.backend.infrastructure.messaging;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Retry(name = "orderEvents")
    @CircuitBreaker(name = "orderEvents", fallbackMethod = "publishOrderCreatedFallback")
    public void publishOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE, RabbitMQConfig.ORDER_ROUTING_KEY, event);
        log.debug("Published order.created for orderId={}", event.orderId());
    }

    @SuppressWarnings("unused")
    private void publishOrderCreatedFallback(OrderCreatedEvent event, Exception ex) {
        log.error("RabbitMQ unavailable after retries — order {} may need manual reconciliation", event.orderId(), ex);
    }
}