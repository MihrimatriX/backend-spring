package com.ecommerce.backend.application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final EntityManager entityManager;

    /**
     * {@code GET /api/metrics/custom} gövdesi (docs/API_CONTRACT.md §4.16). Sipariş ve stoktaki ürün sayıları
     * veritabanından, HTTP sayıları Micrometer {@code http.server.requests} ölçümlerinden okunur.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> customMetrics() {
        long httpRequests = meterRegistry.find("http.server.requests").timers().stream()
                .mapToLong(Timer::count).sum();
        long activeRequests = meterRegistry.find("http.server.requests.active").longTaskTimers().stream()
                .mapToLong(LongTaskTimer::activeTasks).sum();
        long orders = entityManager.createQuery(
                "SELECT COUNT(o) FROM Order o WHERE o.isActive = true", Long.class).getSingleResult();
        long productsInStock = entityManager.createQuery(
                "SELECT COUNT(p) FROM Product p WHERE p.isActive = true AND p.unitInStock > 0", Long.class)
                .getSingleResult();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("httpRequests", Map.of("total", httpRequests, "description", "Total HTTP requests"));
        body.put("httpRequestDuration", Map.of("description", "HTTP request duration histogram"));
        body.put("activeConnections", Map.of("value", activeRequests, "description", "Number of active connections"));
        body.put("orders", Map.of("total", orders, "description", "Total orders"));
        body.put("products", Map.of("inStock", productsInStock, "description", "Number of products in stock"));
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }

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
