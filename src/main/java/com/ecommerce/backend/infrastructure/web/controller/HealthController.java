package com.ecommerce.backend.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.NamedContributor;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Zarfsız sağlık uçları — docs/API_CONTRACT.md §4.16. {@code /api/health}, Spring Actuator sağlık
 * bileşenlerini .NET {@code HealthReport} biçiminde döner; sağlıksızsa 503.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    static final String HEALTHY = "Healthy";
    static final String DEGRADED = "Degraded";
    static final String UNHEALTHY = "Unhealthy";

    private final HealthContributorRegistry healthContributorRegistry;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        long started = System.nanoTime();
        List<Map<String, Object>> entries = new ArrayList<>();
        for (NamedContributor<HealthContributor> named : healthContributorRegistry) {
            collect(named.getName(), named.getContributor(), entries);
        }
        String overall = HEALTHY;
        for (Map<String, Object> entry : entries) {
            overall = worst(overall, (String) entry.get("status"));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", overall);
        body.put("totalDuration", millisSince(started));
        body.put("entries", entries);
        body.put("timestamp", System.currentTimeMillis());
        HttpStatus status = UNHEALTHY.equals(overall) ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.OK;
        return ResponseEntity.status(status).body(body);
    }

    @GetMapping("/ready")
    public Map<String, Object> readiness() {
        return Map.of("status", "Ready", "timestamp", System.currentTimeMillis());
    }

    @GetMapping("/live")
    public Map<String, Object> liveness() {
        return Map.of("status", "Alive", "timestamp", System.currentTimeMillis());
    }

    /** Bileşik katkıcılar {@code ad.altAd} biçiminde düzleştirilir. */
    private static void collect(String name, HealthContributor contributor, List<Map<String, Object>> out) {
        if (contributor instanceof HealthIndicator indicator) {
            long started = System.nanoTime();
            Health health;
            try {
                health = indicator.health();
            } catch (Exception e) {
                health = Health.down(e).build();
            }
            Map<String, Object> data = new LinkedHashMap<>(health.getDetails());
            Object error = data.remove("error");
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", name);
            entry.put("status", map(health.getStatus()));
            entry.put("duration", millisSince(started));
            entry.put("description", health.getStatus().getDescription().isEmpty()
                    ? null : health.getStatus().getDescription());
            entry.put("data", data);
            entry.put("exception", error == null ? null : String.valueOf(error));
            out.add(entry);
        } else if (contributor instanceof CompositeHealthContributor composite) {
            for (NamedContributor<HealthContributor> child : composite) {
                collect(name + "." + child.getName(), child.getContributor(), out);
            }
        }
    }

    private static String map(Status status) {
        if (Status.UP.equals(status)) {
            return HEALTHY;
        }
        if (Status.DOWN.equals(status) || Status.OUT_OF_SERVICE.equals(status)) {
            return UNHEALTHY;
        }
        return DEGRADED;
    }

    private static String worst(String a, String b) {
        if (UNHEALTHY.equals(a) || UNHEALTHY.equals(b)) {
            return UNHEALTHY;
        }
        return DEGRADED.equals(a) || DEGRADED.equals(b) ? DEGRADED : HEALTHY;
    }

    private static double millisSince(long startedNanos) {
        return Math.round((System.nanoTime() - startedNanos) / 10_000.0) / 100.0;
    }
}
