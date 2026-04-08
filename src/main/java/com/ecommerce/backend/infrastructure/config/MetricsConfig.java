package com.ecommerce.backend.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter orderCounter(MeterRegistry meterRegistry) {
        return Counter.builder("ecommerce.orders.total")
                .description("Total number of orders created")
                .register(meterRegistry);
    }

    @Bean
    public Counter productViewCounter(MeterRegistry meterRegistry) {
        return Counter.builder("ecommerce.products.views")
                .description("Total number of product views")
                .register(meterRegistry);
    }

    @Bean
    public Counter userRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("ecommerce.users.registrations")
                .description("Total number of user registrations")
                .register(meterRegistry);
    }

    @Bean
    public Timer orderProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("ecommerce.orders.processing.time")
                .description("Time taken to process orders")
                .register(meterRegistry);
    }

    @Bean
    public Timer productSearchTimer(MeterRegistry meterRegistry) {
        return Timer.builder("ecommerce.products.search.time")
                .description("Time taken to search products")
                .register(meterRegistry);
    }

    @Bean
    public Counter authenticationFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("ecommerce.auth.failures")
                .description("Total number of authentication failures")
                .register(meterRegistry);
    }

    @Bean
    public Counter rateLimitExceededCounter(MeterRegistry meterRegistry) {
        return Counter.builder("ecommerce.rate_limit.exceeded")
                .description("Total number of rate limit exceeded events")
                .register(meterRegistry);
    }
}
