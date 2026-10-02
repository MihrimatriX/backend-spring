package com.ecommerce.backend.infrastructure.security;

import com.ecommerce.backend.application.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * İstekteki oturum sahibine erişim. Controller'lar kullanıcı id'sini yalnızca buradan alır
 * (gövde veya yol parametresinden değil).
 */
@Service
public class CurrentUserService {

    public Optional<AuthenticatedUser> current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public AuthenticatedUser require() {
        return current().orElseThrow(() -> ApiException.unauthorized("UNAUTHORIZED", "Kimlik doğrulama gerekli."));
    }

    public Long requireUserId() {
        return require().id();
    }

    public boolean isAdmin() {
        return current().map(AuthenticatedUser::isAdmin).orElse(false);
    }
}
