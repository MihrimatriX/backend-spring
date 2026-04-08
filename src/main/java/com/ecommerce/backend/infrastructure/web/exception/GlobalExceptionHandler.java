package com.ecommerce.backend.infrastructure.web.exception;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.infrastructure.exception.ConflictException;
import com.ecommerce.backend.infrastructure.exception.IdempotencyConflictException;
import com.ecommerce.backend.infrastructure.logging.ErrorResponseSupport;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static Map<String, String> fieldErrorsFromBinding(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (ObjectError error : bindingResult.getAllErrors()) {
            if (error instanceof FieldError fe) {
                errors.put(fe.getField(), fe.getDefaultMessage());
            } else {
                errors.put("_global", error.getDefaultMessage());
            }
        }
        return errors;
    }

    private static <T> ResponseEntity<BaseResponseDto<T>> validationResponse(Map<String, String> errors) {
        BaseResponseDto<T> response = new BaseResponseDto<>();
        response.setSuccess(false);
        response.setMessage("Doğrulama hatası");
        response.setCode("VALIDATION_ERROR");
        response.setFieldErrors(errors);
        ErrorResponseSupport.attachTraceId(response);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * {@link org.springframework.web.bind.MethodArgumentNotValidException} dahil
     * tüm {@link BindException} alt tipleri.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleBindException(BindException ex) {
        Map<String, String> errors = fieldErrorsFromBinding(ex.getBindingResult());
        log.warn("Validation failed: {} field(s)", errors.size());
        return validationResponse(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath() != null ? v.getPropertyPath().toString() : "_global",
                        ConstraintViolation::getMessage,
                        (a, b) -> a + "; " + b));
        log.warn("Constraint violation: {} path(s)", errors.size());
        return validationResponse(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMostSpecificCause().getMessage());
        BaseResponseDto<Void> body = BaseResponseDto.codedError("MALFORMED_REQUEST_BODY",
                "İstek gövdesi okunamadı veya JSON geçersiz.");
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        log.warn("Parameter type mismatch: {} — {}", name, ex.getMessage());
        BaseResponseDto<Void> body = BaseResponseDto.codedError("TYPE_MISMATCH",
                "Geçersiz parametre: " + name);
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing parameter: {}", ex.getParameterName());
        BaseResponseDto<Void> body = BaseResponseDto.codedError("MISSING_PARAMETER",
                "Eksik parametre: " + ex.getParameterName());
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleMissingPathVar(MissingPathVariableException ex) {
        log.warn("Missing path variable: {}", ex.getVariableName());
        BaseResponseDto<Void> body = BaseResponseDto.codedError("MISSING_PATH_VARIABLE",
                "Eksik yol değişkeni: " + ex.getVariableName());
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("HTTP method not allowed: {} {}", ex.getMethod(), ex.getSupportedHttpMethods());
        BaseResponseDto<Void> body = BaseResponseDto.codedError("METHOD_NOT_ALLOWED",
                "Bu kaynak için HTTP metodu desteklenmiyor.");
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponseDto<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.debug("Invalid argument: {}", ex.getMessage());
        BaseResponseDto<String> body = BaseResponseDto.codedError("INVALID_ARGUMENT", ex.getMessage());
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleConflict(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        BaseResponseDto<Void> body = BaseResponseDto.codedError("CONFLICT", ex.getMessage());
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleIdempotencyConflict(IdempotencyConflictException ex) {
        log.info("Idempotency conflict (parallel duplicate request)");
        BaseResponseDto<Void> body = BaseResponseDto.codedError("IDEMPOTENCY_CONFLICT",
                "Yinelenen istek çakışması; lütfen tekrar deneyin.");
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleDataAccess(DataAccessException ex) {
        log.error("Database access error", ex);
        BaseResponseDto<Void> body = BaseResponseDto.codedError("DATA_ACCESS_ERROR",
                "Veri erişiminde geçici veya kalıcı bir sorun oluştu.");
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Unhandled runtime exception", ex);
        BaseResponseDto<Void> body = BaseResponseDto.codedError("INTERNAL_ERROR",
                "Beklenmeyen bir hata oluştu. Lütfen daha sonra tekrar deneyin.");
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponseDto<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected checked exception", ex);
        BaseResponseDto<Void> body = BaseResponseDto.codedError("INTERNAL_ERROR", "İşlem tamamlanamadı.");
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
