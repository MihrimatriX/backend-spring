package com.ecommerce.backend.infrastructure.logging;

/**
 * HTTP istekleri ile log satırlarını eşlemek için ortak başlık ve MDC anahtarı.
 */
public final class CorrelationIdConstants {

    public static final String HEADER = "X-Correlation-ID";
    /** SLF4J MDC anahtarı — log pattern'de {@code %X{correlationId}} */
    public static final String MDC_KEY = "correlationId";

    private CorrelationIdConstants() {
    }
}
