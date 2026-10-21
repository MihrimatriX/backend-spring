package com.ecommerce.backend.infrastructure.web.exception;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.infrastructure.logging.ErrorResponseSupport;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller dışında oluşan (filtre, servlet) hatalar için Spring'in beyaz sayfası yerine
 * ortak zarfı döner.
 */
@RestController
public class JsonErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<BaseResponseDto<Void>> error(HttpServletRequest request) {
        Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = HttpStatus.resolve(code instanceof Integer i ? i : 500);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        BaseResponseDto<Void> body = switch (status) {
            case NOT_FOUND -> BaseResponseDto.fail("NOT_FOUND", "Kaynak bulunamadı.");
            case UNAUTHORIZED -> BaseResponseDto.fail("UNAUTHORIZED", "Kimlik doğrulama gerekli.");
            case FORBIDDEN -> BaseResponseDto.fail("FORBIDDEN", "Bu işlem için yetkiniz yok.");
            case BAD_REQUEST -> BaseResponseDto.fail("BAD_REQUEST", "Geçersiz istek.");
            default -> status.is4xxClientError()
                    ? BaseResponseDto.fail("BAD_REQUEST", "Geçersiz istek.")
                    : BaseResponseDto.fail("INTERNAL_ERROR",
                            "Beklenmeyen bir hata oluştu. Destek için traceId değerini iletin.");
        };
        ErrorResponseSupport.attachTraceId(body);
        return ResponseEntity.status(status).body(body);
    }
}
