package com.ecommerce.backend.application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public void incrementOrderCounter() {
        Counter.builder("ecommerce.orders.total")
                .description("Total number of orders created")
                .register(meterRegistry)
                .increment();
        log.debug("Order counter incremented");
    }

    public void incrementProductViewCounter(Long productId) {
        Counter.builder("ecommerce.products.views")
                .description("Total number of product views")
                .tag("product_id", String.valueOf(productId))
                .register(meterRegistry)
                .increment();
        log.debug("Product view counter incremented for product: {}", productId);
    }

    public void incrementUserRegistrationCounter() {
        Counter.builder("ecommerce.users.registrations")
                .description("Total number of user registrations")
                .register(meterRegistry)
                .increment();
        log.debug("User registration counter incremented");
    }

    public void incrementAuthenticationFailureCounter(String reason) {
        Counter.builder("ecommerce.auth.failures")
                .description("Total number of authentication failures")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
        log.debug("Authentication failure counter incremented for reason: {}", reason);
    }

    public void incrementRateLimitExceededCounter(String clientIp) {
        Counter.builder("ecommerce.rate_limit.exceeded")
                .description("Total number of rate limit exceeded events")
                .tag("client_ip", clientIp)
                .register(meterRegistry)
                .increment();
        log.debug("Rate limit exceeded counter incremented for IP: {}", clientIp);
    }

    public Timer.Sample startOrderProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordOrderProcessingTime(Timer.Sample sample) {
        Timer timer = Timer.builder("ecommerce.orders.processing.time")
                .description("Time taken to process orders")
                .register(meterRegistry);
        sample.stop(timer);
        log.debug("Order processing time recorded");
    }

    public Timer.Sample startProductSearchTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordProductSearchTime(Timer.Sample sample) {
        Timer timer = Timer.builder("ecommerce.products.search.time")
                .description("Time taken to search products")
                .register(meterRegistry);
        sample.stop(timer);
        log.debug("Product search time recorded");
    }
}
