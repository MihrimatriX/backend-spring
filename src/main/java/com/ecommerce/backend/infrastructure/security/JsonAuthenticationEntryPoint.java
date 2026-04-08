package com.ecommerce.backend.infrastructure.security;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.infrastructure.logging.ErrorResponseSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Kimlik doğrulanmamış isteklerde tutarlı JSON gövdesi ve loglama.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        log.warn("Unauthorized {} {} — {}", request.getMethod(), request.getRequestURI(),
                authException != null ? authException.getMessage() : "no details");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        BaseResponseDto<Void> body = BaseResponseDto.codedError("UNAUTHORIZED", "Kimlik doğrulama gerekli.");
        ErrorResponseSupport.attachTraceId(body);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
