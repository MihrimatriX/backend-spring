package com.ecommerce.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
    /**
     * Makine tarafından işlenebilir hata kodu (örn. STOCK_INSUFFICIENT,
     * ADDRESS_NOT_OWNED).
     */
    private String code;
    /** Bean validation veya alan bazlı hatalar. */
    private Map<String, String> fieldErrors;
    /**
     * İstek korelasyon kimliği (loglarda {@code correlationId} ile aynı). Destek ve
     * teşhis için döndürülür.
     */
    private String traceId;

    // Constructors
    public BaseResponseDto() {
    }

    public BaseResponseDto(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public BaseResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public BaseResponseDto(boolean success, String message, String error) {
        this.success = success;
        this.message = message;
        this.error = error;
    }

    // Static factory methods
    public static <T> BaseResponseDto<T> success(T data) {
        return new BaseResponseDto<>(true, "Operation successful", data);
    }

    public static <T> BaseResponseDto<T> success(String message, T data) {
        return new BaseResponseDto<>(true, message, data);
    }

    public static <T> BaseResponseDto<T> success(String message) {
        return new BaseResponseDto<>(true, message);
    }

    public static <T> BaseResponseDto<T> error(String message) {
        return new BaseResponseDto<>(false, message);
    }

    /**
     * İş kuralı / istemci tarafından işlenebilir hata (HTTP gövdesinde {@code code}
     * ile).
     */
    public static <T> BaseResponseDto<T> codedError(String code, String message) {
        BaseResponseDto<T> r = new BaseResponseDto<>(false, message);
        r.setCode(code);
        return r;
    }

    public static <T> BaseResponseDto<T> error(String message, String error) {
        return new BaseResponseDto<>(false, message, error);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
