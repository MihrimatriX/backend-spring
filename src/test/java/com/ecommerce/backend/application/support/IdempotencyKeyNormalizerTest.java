package com.ecommerce.backend.application.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyKeyNormalizerTest {

    @Test
    void normalizeReturnsNullForBlank() {
        assertThat(IdempotencyKeyNormalizer.normalize(null)).isNull();
        assertThat(IdempotencyKeyNormalizer.normalize("")).isNull();
        assertThat(IdempotencyKeyNormalizer.normalize("   ")).isNull();
    }

    @Test
    void normalizeTrimsAndLowercases() {
        assertThat(IdempotencyKeyNormalizer.normalize("  ABC-123  ")).isEqualTo("abc-123");
    }
}
