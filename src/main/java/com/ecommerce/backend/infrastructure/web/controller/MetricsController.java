package com.ecommerce.backend.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> metrics() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "ecommerce-backend-spring");
        response.put("timestamp", System.currentTimeMillis());

        // Basic metrics
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("uptime", System.currentTimeMillis());
        metrics.put("memory", Runtime.getRuntime().totalMemory());
        metrics.put("freeMemory", Runtime.getRuntime().freeMemory());
        metrics.put("maxMemory", Runtime.getRuntime().maxMemory());

        response.put("metrics", metrics);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/prometheus")
    public ResponseEntity<String> prometheus() {
        // Basic Prometheus metrics format
        StringBuilder metrics = new StringBuilder();
        metrics.append("# HELP jvm_memory_used_bytes Used memory in bytes\n");
        metrics.append("# TYPE jvm_memory_used_bytes gauge\n");
        metrics.append("jvm_memory_used_bytes ")
                .append(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).append("\n");

        metrics.append("# HELP jvm_memory_max_bytes Max memory in bytes\n");
        metrics.append("# TYPE jvm_memory_max_bytes gauge\n");
        metrics.append("jvm_memory_max_bytes ").append(Runtime.getRuntime().maxMemory()).append("\n");

        return ResponseEntity.ok(metrics.toString());
    }
}
