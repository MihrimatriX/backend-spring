package com.ecommerce.backend.infrastructure.exception;

/**
 * Thrown when optimistic locking or concurrent business rules block progress
 * (HTTP 409).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
