package com.ecommerce.backend.application.exception;

import org.springframework.http.HttpStatus;

/**
 * Servislerin istemciye dönecek hataları için tek tip istisna. {@code GlobalExceptionHandler}
 * bunu {@code {success:false, message, errorCode}} zarfına ve ilgili HTTP koduna çevirir
 * (docs/API_CONTRACT.md §1.2).
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public ApiException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public ApiException(HttpStatus status, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /** 400 — iş kuralı ihlali. */
    public static ApiException badRequest(String errorCode, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, errorCode, message);
    }

    /** 404 — kayıt bulunamadı. */
    public static ApiException notFound(String errorCode, String message) {
        return new ApiException(HttpStatus.NOT_FOUND, errorCode, message);
    }

    /** 403 — rol yetersiz veya başka kullanıcının kaynağı. */
    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }

    /** 401 — kimlik doğrulama hatası. */
    public static ApiException unauthorized(String errorCode, String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, errorCode, message);
    }

    /** 409 — çakışma. */
    public static ApiException conflict(String errorCode, String message) {
        return new ApiException(HttpStatus.CONFLICT, errorCode, message);
    }
}
