package com.ecommerce.backend.application.support;

import org.springframework.util.StringUtils;

/**
 * Normalizes optional Idempotency-Key headers (Stripe-style safe retries).
 */
public final class IdempotencyKeyNormalizer {

    private IdempotencyKeyNormalizer() {
    }

    /**
     * @return normalized key or null if absent / blank
     */
    public static String normalize(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return raw.trim().toLowerCase();
    }
}
