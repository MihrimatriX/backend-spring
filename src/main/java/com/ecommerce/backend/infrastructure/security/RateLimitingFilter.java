package com.ecommerce.backend.infrastructure.security;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.service.MetricsService;
import com.ecommerce.backend.infrastructure.config.RateLimitProperties;
import com.ecommerce.backend.infrastructure.logging.ErrorResponseSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP başına sabit pencereli (1 dakika) istek limiti — docs/API_CONTRACT.md §1.4.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000;

    private final RateLimitProperties properties;
    private final MetricsService metricsService;
    private final ObjectMapper objectMapper;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    private static final class Window {
        long start;
        int count;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return properties.getPermitPerMinute() <= 0
                || path.equals("/health")
                || path.startsWith("/actuator")
                || path.startsWith("/api/health")
                || path.startsWith("/api/metrics")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String ip = clientIp(request);
        long now = System.currentTimeMillis();
        int limit = properties.getPermitPerMinute();
        long retryAfterSeconds;
        int remaining;
        boolean allowed;
        Window w = windows.computeIfAbsent(ip, k -> new Window());
        synchronized (w) {
            if (now - w.start >= WINDOW_MILLIS) {
                w.start = now;
                w.count = 0;
            }
            w.count++;
            allowed = w.count <= limit;
            remaining = Math.max(0, limit - w.count);
            retryAfterSeconds = Math.max(1, (w.start + WINDOW_MILLIS - now + 999) / 1000);
        }
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(e -> now - e.getValue().start >= WINDOW_MILLIS);
        }

        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(remaining));
        if (allowed) {
            filterChain.doFilter(request, response);
            return;
        }

        metricsService.incrementRateLimitExceededCounter(ip);
        log.warn("Rate limit aşıldı: ip={} path={}", ip, request.getRequestURI());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        BaseResponseDto<Void> body = BaseResponseDto.fail("RATE_LIMITED",
                "Çok fazla istek. Lütfen biraz sonra tekrar deneyin.");
        ErrorResponseSupport.attachTraceId(body);
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
