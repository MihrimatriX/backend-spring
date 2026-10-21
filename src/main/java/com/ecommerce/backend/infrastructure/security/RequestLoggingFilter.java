package com.ecommerce.backend.infrastructure.security;

import com.ecommerce.backend.infrastructure.logging.CorrelationIdConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_CORRELATION_HEADER_LENGTH = 128;
    private static final Pattern SAFE_CORRELATION = Pattern.compile("^[a-zA-Z0-9._:\\-]{1,128}$");

    private static boolean isQuietPath(String uri) {
        if (uri == null) {
            return false;
        }
        return uri.startsWith("/actuator/health")
                || "/actuator".equals(uri)
                || uri.startsWith("/actuator/prometheus");
    }

    private static String resolveCorrelationId(HttpServletRequest request) {
        String incoming = request.getHeader(CorrelationIdConstants.HEADER);
        if (incoming != null) {
            incoming = incoming.trim();
            if (incoming.length() > MAX_CORRELATION_HEADER_LENGTH) {
                incoming = incoming.substring(0, MAX_CORRELATION_HEADER_LENGTH);
            }
            if (SAFE_CORRELATION.matcher(incoming).matches()) {
                return incoming;
            }
            log.debug("Ignoring invalid {} header value", CorrelationIdConstants.HEADER);
        }
        return UUID.randomUUID().toString();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String correlationId = resolveCorrelationId(request);
        response.setHeader(CorrelationIdConstants.HEADER, correlationId);

        long startTime = System.currentTimeMillis();
        String uri = request.getRequestURI();
        boolean quiet = isQuietPath(uri);

        MDC.put(CorrelationIdConstants.MDC_KEY, correlationId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", uri);

        try {
            if (log.isTraceEnabled() && !quiet) {
                log.trace("Request started {} {}", request.getMethod(), uri);
            }

            filterChain.doFilter(request, response);

            long duration = System.currentTimeMillis() - startTime;
            if (quiet) {
                log.trace("{} {} → {} ({} ms)", request.getMethod(), uri, response.getStatus(), duration);
            } else if (duration > 2_000 || response.getStatus() >= 400) {
                log.warn("{} {} → {} ({} ms)", request.getMethod(), uri, response.getStatus(), duration);
            } else {
                log.info("{} {} → {} ({} ms)", request.getMethod(), uri, response.getStatus(), duration);
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("{} {} failed after {} ms", request.getMethod(), uri, duration, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
