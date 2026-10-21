package com.ecommerce.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

/**
 * Ortak cevap zarfı (docs/API_CONTRACT.md §1.1). {@code data}, {@code error}, {@code errorCode},
 * {@code errors} ve {@code traceId} null ise yazılmaz.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "success", "message", "data", "error", "errorCode", "errors", "traceId" })
public class BaseResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    /** Yalnızca geliştirme ortamında teknik ayrıntı. */
    private String error;
    /** Makine tarafından işlenebilir hata kodu (örn. ORDER_NOT_FOUND). */
    private String errorCode;
    /** Doğrulama hataları: alan adı (camelCase) → mesajlar. */
    private Map<String, List<String>> errors;
    /** İstek korelasyon kimliği ({@code X-Correlation-Id}). */
    private String traceId;

    public BaseResponseDto() {
    }

    public BaseResponseDto(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseResponseDto<T> success(T data) {
        return new BaseResponseDto<>(true, "Operation successful", data);
    }

    public static <T> BaseResponseDto<T> success(String message, T data) {
        return new BaseResponseDto<>(true, message, data);
    }

    /** Hata cevabı; {@code errorCode} her zaman doldurulmalıdır. */
    public static <T> BaseResponseDto<T> fail(String errorCode, String message) {
        BaseResponseDto<T> r = new BaseResponseDto<>(false, message, null);
        r.setErrorCode(errorCode);
        return r;
    }

    /**
     * @deprecated Kodsuz hata; yeni kodda {@link #fail(String, String)} veya
     *             {@code ApiException} kullanın.
     */
    @Deprecated
    public static <T> BaseResponseDto<T> error(String message) {
        return fail("BAD_REQUEST", message);
    }

    /** @deprecated {@link #fail(String, String)} kullanın. */
    @Deprecated
    public static <T> BaseResponseDto<T> codedError(String errorCode, String message) {
        return fail(errorCode, message);
    }

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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<String>> errors) {
        this.errors = errors;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
