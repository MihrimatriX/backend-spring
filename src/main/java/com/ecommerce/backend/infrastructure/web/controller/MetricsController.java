package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.service.MetricsService;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Zarfsız metrik uçları — docs/API_CONTRACT.md §4.16. Prometheus metni Micrometer
 * {@link PrometheusMeterRegistry}'den üretilir (testlerde kayıt defteri yoksa 503).
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private static final MediaType PROMETHEUS_TEXT = MediaType.parseMediaType("text/plain;version=0.0.4;charset=utf-8");

    private final ObjectProvider<PrometheusMeterRegistry> prometheusRegistry;
    private final MetricsService metricsService;

    @GetMapping({ "", "/prometheus" })
    public ResponseEntity<String> prometheus() {
        PrometheusMeterRegistry registry = prometheusRegistry.getIfAvailable();
        if (registry == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(MediaType.TEXT_PLAIN)
                    .body("Prometheus registry is not available\n");
        }
        return ResponseEntity.ok().contentType(PROMETHEUS_TEXT).body(registry.scrape());
    }

    @GetMapping("/custom")
    public Map<String, Object> custom() {
        return metricsService.customMetrics();
    }
}
