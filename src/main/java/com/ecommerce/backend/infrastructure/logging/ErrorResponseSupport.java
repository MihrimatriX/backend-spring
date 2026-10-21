package com.ecommerce.backend.infrastructure.logging;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import org.slf4j.MDC;

/**
 * Hata yanıtlarına destek için iz (trace) bilgisini ekler; loglarla
 * eşleştirmeyi kolaylaştırır.
 */
public final class ErrorResponseSupport {

    private ErrorResponseSupport() {
    }

    public static void attachTraceId(BaseResponseDto<?> dto) {
        if (dto == null) {
            return;
        }
        String id = MDC.get(CorrelationIdConstants.MDC_KEY);
        if (id != null && !id.isBlank()) {
            dto.setTraceId(id);
        }
    }
}
