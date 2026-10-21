package com.ecommerce.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Rol kuralı: e-posta bu listedeyse {@code Admin}, değilse {@code User}
 * (docs/API_CONTRACT.md §2; .NET karşılığı {@code Auth:AdminEmails}).
 */
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_USER = "User";

    private List<String> adminEmails = new ArrayList<>(List.of("admin@example.com", "manager@shop.demo"));

    public List<String> getAdminEmails() {
        return adminEmails;
    }

    public void setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
    }

    public String roleFor(String email) {
        if (email == null) {
            return ROLE_USER;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return adminEmails.stream().anyMatch(a -> a.trim().equalsIgnoreCase(normalized)) ? ROLE_ADMIN : ROLE_USER;
    }
}
