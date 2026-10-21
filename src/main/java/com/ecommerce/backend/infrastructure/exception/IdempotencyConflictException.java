package com.ecommerce.backend.infrastructure.exception;

/**
 * Two parallel requests used the same idempotency key; outer flow should load
 * the winning order.
 */
public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(Throwable cause) {
        super(cause);
    }
}
