package com.ecommerce.backend.infrastructure.web.support;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Controller'lar için kısa yardımcılar. Hatalar {@code ApiException} ile fırlatılır ve
 * {@code GlobalExceptionHandler} tarafından zarfa çevrilir.
 */
public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ResponseEntity<BaseResponseDto<T>> ok(String message, T data) {
        return ResponseEntity.ok(BaseResponseDto.success(message, data));
    }

    public static <T> ResponseEntity<BaseResponseDto<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseDto.success(message, data));
    }

    public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /** {@code pageNumber < 1} → 1; {@code pageSize} 1..100 aralığına kırpılır (docs/API_CONTRACT.md §1.3). */
    public static int page(Integer pageNumber) {
        return pageNumber == null || pageNumber < 1 ? 1 : pageNumber;
    }

    public static int size(Integer pageSize, int defaultSize) {
        if (pageSize == null) {
            return defaultSize;
        }
        return Math.max(1, Math.min(100, pageSize));
    }
}
