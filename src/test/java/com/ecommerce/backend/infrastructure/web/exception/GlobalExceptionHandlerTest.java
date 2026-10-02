package com.ecommerce.backend.infrastructure.web.exception;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.infrastructure.exception.ConflictException;
import com.ecommerce.backend.infrastructure.logging.CorrelationIdConstants;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @SuppressWarnings("unused")
    private static class SampleForm {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void conflictReturns409WithErrorCode() {
        ResponseEntity<BaseResponseDto<Void>> response =
                handler.handleApi(new ConflictException("Concurrent stock update"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Concurrent stock update");
        assertThat(response.getBody().getErrorCode()).isEqualTo("CONFLICT");
    }

    @Test
    void apiExceptionKeepsStatusAndCode() {
        ResponseEntity<BaseResponseDto<Void>> response =
                handler.handleApi(ApiException.notFound("ORDER_NOT_FOUND", "Sipariş bulunamadı"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getErrorCode()).isEqualTo("ORDER_NOT_FOUND");
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    void errorBodyIncludesTraceIdWhenMdcSet() {
        MDC.put(CorrelationIdConstants.MDC_KEY, "corr-test-99");
        try {
            ResponseEntity<BaseResponseDto<Void>> response = handler.handleApi(new ConflictException("x"));
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTraceId()).isEqualTo("corr-test-99");
        } finally {
            MDC.clear();
        }
    }

    @Test
    void bindExceptionReturnsDeterministicValidationPayload() {
        BindException ex = new BindException(new SampleForm(), "form");
        ex.rejectValue("password", "size", "Password must be at least 6 characters");
        ex.rejectValue("email", "invalid", "Invalid email format");

        ResponseEntity<BaseResponseDto<Void>> response = handler.handleBind(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
        // Alfabetik ilk alan (email) mesajı
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid email format");
        assertThat(response.getBody().getErrors()).containsEntry("email", List.of("Invalid email format"));
    }

    @Test
    void missingParameterReturnsBadRequest() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("q", "String");

        ResponseEntity<BaseResponseDto<Void>> response = handler.handleBadRequest(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo("BAD_REQUEST");
    }
}
