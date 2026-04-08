package com.ecommerce.backend.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitingConfig {

    @Value("${rate-limiting.capacity:100}")
    private int capacity;

    @Value("${rate-limiting.refill-tokens:100}")
    private int refillTokens;

    @Value("${rate-limiting.refill-duration-minutes:1}")
    private int refillDurationMinutes;

    @Bean
    public Bucket bucket() {
        Bandwidth limit = Bandwidth.classic(capacity,
                Refill.intervally(refillTokens, Duration.ofMinutes(refillDurationMinutes)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
