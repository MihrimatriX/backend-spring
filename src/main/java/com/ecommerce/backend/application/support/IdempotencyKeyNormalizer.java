package com.ecommerce.backend.application.support;

import com.ecommerce.backend.application.exception.ApiException;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * İsteğe bağlı {@code Idempotency-Key} başlığı (docs/API_CONTRACT.md §5.2 adım 1): kırpılır,
 * küçük harfe çevrilir; doluysa 8–128 karakter olmalıdır.
 */
public final class IdempotencyKeyNormalizer {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 128;
    public static final String INVALID_CODE = "IDEMPOTENCY_KEY_INVALID";
    public static final String INVALID_MESSAGE = "Idempotency-Key 8-128 karakter olmalıdır.";

    private IdempotencyKeyNormalizer() {
    }

    /**
     * @return kırpılmış, küçük harfli anahtar; başlık yok veya boşsa {@code null}
     */
    public static String normalize(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * {@link #normalize(String)} + uzunluk kontrolü.
     *
     * @throws ApiException 400 {@code IDEMPOTENCY_KEY_INVALID} anahtar 8–128 karakter değilse
     */
    public static String normalizeAndValidate(String raw) {
        String key = normalize(raw);
        if (key != null && (key.length() < MIN_LENGTH || key.length() > MAX_LENGTH)) {
            throw ApiException.badRequest(INVALID_CODE, INVALID_MESSAGE);
        }
        return key;
    }
}
