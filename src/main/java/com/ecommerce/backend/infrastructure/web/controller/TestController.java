package com.ecommerce.backend.infrastructure.web.controller;

import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Zarfsız tanılama uçları — docs/API_CONTRACT.md §4.16. {@code timestamp} Unix milisaniyedir (.NET ile aynı).
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    static final String BACKEND = "spring";
    static final String SERVICE = "ecommerce-backend-spring";
    static final String VERSION = "1.0.0";

    private final Environment environment;
    private final String framework;
    private final String machine;

    public TestController(Environment environment) {
        this.environment = environment;
        this.framework = frameworkName();
        this.machine = machineName();
    }

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Hello from Spring Boot!");
        body.put("status", "success");
        body.put("timestamp", System.currentTimeMillis());
        body.put("framework", framework);
        body.put("version", VERSION);
        body.put("backend", BACKEND);
        return body;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", SERVICE);
        body.put("timestamp", System.currentTimeMillis());
        body.put("environment", environmentName());
        body.put("backend", BACKEND);
        return body;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", SERVICE);
        body.put("version", VERSION);
        body.put("framework", framework);
        body.put("environment", environmentName());
        body.put("timestamp", System.currentTimeMillis());
        body.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        body.put("machine", machine);
        body.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        body.put("backend", BACKEND);
        return body;
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "pong");
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }

    /** Etkin profil(ler); yoksa {@code default}. */
    private String environmentName() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length == 0 ? "default" : String.join(",", profiles);
    }

    /** Örn. {@code Spring Boot 3.4}. */
    private static String frameworkName() {
        String version = SpringBootVersion.getVersion();
        if (version == null) {
            return "Spring Boot";
        }
        String[] parts = version.split("\\.");
        return "Spring Boot " + (parts.length >= 2 ? parts[0] + "." + parts[1] : version);
    }

    private static String machineName() {
        String host = System.getenv("HOSTNAME");
        if (host != null && !host.isBlank()) {
            return host;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
