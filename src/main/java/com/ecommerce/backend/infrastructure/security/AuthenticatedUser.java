package com.ecommerce.backend.infrastructure.security;

import com.ecommerce.backend.infrastructure.config.AuthProperties;

import java.security.Principal;

/**
 * JWT'den çıkarılan oturum sahibi. {@link #getName()} kullanıcı id'sini döner.
 */
public record AuthenticatedUser(Long id, String email, String role, String jti) implements Principal {

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    public boolean isAdmin() {
        return AuthProperties.ROLE_ADMIN.equals(role);
    }
}
