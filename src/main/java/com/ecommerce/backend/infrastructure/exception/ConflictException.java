package com.ecommerce.backend.infrastructure.exception;

import com.ecommerce.backend.application.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * İyimser kilit veya eşzamanlı iş kuralı çakışması (HTTP 409, {@code CONFLICT}).
 */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, "CONFLICT", message);
    }

    public ConflictException(String message, Throwable cause) {
        super(HttpStatus.CONFLICT, "CONFLICT", message, cause);
    }
}
