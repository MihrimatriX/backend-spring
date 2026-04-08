package com.ecommerce.backend.infrastructure.security;

import com.ecommerce.backend.application.service.MetricsService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Bucket bucket;
    private final MetricsService metricsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Skip rate limiting for certain paths
        String path = request.getRequestURI();
        if (path.equals("/")
                || path.equals("/favicon.ico")
                || path.equals("/swagger")
                || path.startsWith("/actuator/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X-Rate-Limit-Reset", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));

            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Remaining", "0");
            response.addHeader("X-Rate-Limit-Reset", String.valueOf(waitForRefill));
            response.addHeader("Retry-After", String.valueOf(waitForRefill));

            response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
            response.setContentType("application/json");

            // Record metrics
            metricsService.incrementRateLimitExceededCounter(getClientIpAddress(request));

            log.warn("Rate limit exceeded for IP: {}, Path: {}", getClientIpAddress(request), path);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
