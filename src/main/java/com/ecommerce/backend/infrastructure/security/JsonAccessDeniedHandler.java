package com.ecommerce.backend.infrastructure.security;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.infrastructure.logging.ErrorResponseSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Yetkisi olmayan (authenticated ama izin yok) isteklerde JSON yanıt.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Forbidden {} {} — {}", request.getMethod(), request.getRequestURI(),
                accessDeniedException != null ? accessDeniedException.getMessage() : "no details");

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        BaseResponseDto<Void> body = BaseResponseDto.codedError("FORBIDDEN", "Bu işlem için yetkiniz yok.");
        ErrorResponseSupport.attachTraceId(body);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
