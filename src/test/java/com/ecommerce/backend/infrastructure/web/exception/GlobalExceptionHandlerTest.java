package com.ecommerce.backend.infrastructure.web.exception;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.infrastructure.exception.ConflictException;
import com.ecommerce.backend.infrastructure.logging.CorrelationIdConstants;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @SuppressWarnings("unused")
    private static class SampleForm {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void conflictReturns409() {
        ResponseEntity<BaseResponseDto<Void>> response =
                handler.handleConflict(new ConflictException("Concurrent stock update"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Concurrent stock update");
        assertThat(response.getBody().getCode()).isEqualTo("CONFLICT");
    }

    @Test
    void errorBodyIncludesTraceIdWhenMdcSet() {
        MDC.put(CorrelationIdConstants.MDC_KEY, "corr-test-99");
        try {
            ResponseEntity<BaseResponseDto<Void>> response =
                    handler.handleConflict(new ConflictException("x"));
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTraceId()).isEqualTo("corr-test-99");
        } finally {
            MDC.clear();
        }
    }

    @Test
    void bindExceptionReturnsValidationPayload() {
        BindException ex = new BindException(new SampleForm(), "form");
        ex.rejectValue("email", "invalid", "geçersiz");

        ResponseEntity<BaseResponseDto<Void>> response = handler.handleBindException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getFieldErrors()).containsEntry("email", "geçersiz");
    }

    @Test
    void missingParameterReturns400() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("page", "int");

        ResponseEntity<BaseResponseDto<Void>> response = handler.handleMissingParam(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("MISSING_PARAMETER");
    }
}
