package com.ecommerce.backend.infrastructure.web.exception;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.infrastructure.exception.IdempotencyConflictException;
import com.ecommerce.backend.infrastructure.logging.ErrorResponseSupport;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tüm hataları ortak zarfa çevirir (docs/API_CONTRACT.md §1.2).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Geliştirmede {@code error} alanına teknik ayrıntı yazılır. */
    @Value("${app.expose-error-details:false}")
    private boolean exposeErrorDetails;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleApi(ApiException ex) {
        return respond(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleBind(BindException ex) {
        return validation(fromBinding(ex.getBindingResult()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleConstraint(ConstraintViolationException ex) {
        Map<String, List<String>> errors = new TreeMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String path = v.getPropertyPath().toString();
            String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            errors.computeIfAbsent(field, k -> new ArrayList<>()).add(v.getMessage());
        }
        return validation(errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleMethodValidation(HandlerMethodValidationException ex) {
        Map<String, List<String>> errors = new TreeMap<>();
        ex.getAllValidationResults().forEach(r -> r.getResolvableErrors().forEach(e -> errors
                .computeIfAbsent(r.getMethodParameter().getParameterName(), k -> new ArrayList<>())
                .add(e.getDefaultMessage())));
        return validation(errors);
    }

    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, MissingRequestHeaderException.class })
    public ResponseEntity<BaseResponseDto<Void>> handleBadRequest(Exception ex) {
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Geçersiz istek.", ex);
    }

    @ExceptionHandler({ NoResourceFoundException.class, NoHandlerFoundException.class })
    public ResponseEntity<BaseResponseDto<Void>> handleNoRoute(Exception ex) {
        return respond(HttpStatus.NOT_FOUND, "NOT_FOUND", "Kaynak bulunamadı.", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleMethod(HttpRequestMethodNotSupportedException ex) {
        return respond(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "Bu kaynak için HTTP metodu desteklenmiyor.", null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return respond(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "İçerik türü desteklenmiyor.", null);
    }

    @ExceptionHandler({ AccessDeniedException.class, AuthorizationDeniedException.class })
    public ResponseEntity<BaseResponseDto<Void>> handleAccessDenied(Exception ex) {
        return respond(HttpStatus.FORBIDDEN, "FORBIDDEN", "Bu işlem için yetkiniz yok.", null);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return respond(HttpStatus.CONFLICT, "CONFLICT",
                "Kayıt başka bir işlem tarafından değiştirildi. Tekrar deneyin.", ex);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleIdempotency(IdempotencyConflictException ex) {
        return respond(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT",
                "Yinelenen istek çakışması; lütfen tekrar deneyin.", ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Veritabanı kısıtı ihlali: {}", ex.getMostSpecificCause().getMessage());
        return respond(HttpStatus.CONFLICT, "CONFLICT", "Veritabanı kısıtı veya güncelleme hatası.", ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponseDto<Void>> handleUnexpected(Exception ex) {
        log.error("Beklenmeyen hata", ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Beklenmeyen bir hata oluştu. Destek için traceId değerini iletin.", ex);
    }

    private static Map<String, List<String>> fromBinding(BindingResult result) {
        Map<String, List<String>> errors = new TreeMap<>();
        for (ObjectError error : result.getAllErrors()) {
            String field = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            errors.computeIfAbsent(field, k -> new ArrayList<>()).add(error.getDefaultMessage());
        }
        return errors;
    }

    /** Mesaj: alfabetik sıradaki ilk alanın ilk hatası (deterministik). */
    private ResponseEntity<BaseResponseDto<Void>> validation(Map<String, List<String>> errors) {
        String first = errors.values().stream().flatMap(List::stream).findFirst().orElse("Doğrulama hatası");
        BaseResponseDto<Void> body = BaseResponseDto.fail("VALIDATION_ERROR", first);
        body.setErrors(errors);
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<BaseResponseDto<Void>> respond(HttpStatus status, String code, String message,
            Exception detail) {
        BaseResponseDto<Void> body = BaseResponseDto.fail(code, message);
        if (exposeErrorDetails && detail != null) {
            body.setError(detail.getMessage());
        }
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(status).body(body);
    }
}
