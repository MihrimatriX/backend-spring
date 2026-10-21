package com.ecommerce.backend.infrastructure.security;

import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Bearer token'ı doğrular; kullanıcı aktif değilse veya token {@code logout-all-devices} ile
 * iptal edildiyse kimlik atanmaz (korumalı uçlar 401 döner).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(header.substring(7).trim(), request).ifPresent(auth -> SecurityContextHolder.getContext()
                    .setAuthentication(auth));
        }
        filterChain.doFilter(request, response);
    }

    private Optional<UsernamePasswordAuthenticationToken> authenticate(String token, HttpServletRequest request) {
        Optional<Claims> parsed = jwtService.parse(token);
        if (parsed.isEmpty()) {
            log.debug("Geçersiz veya süresi dolmuş JWT");
            return Optional.empty();
        }
        Claims claims = parsed.get();
        Long userId;
        try {
            userId = Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        Optional<User> user = userRepository.findByIdAndIsActiveTrue(userId);
        if (user.isEmpty() || isRevoked(user.get(), claims)) {
            return Optional.empty();
        }
        String role = claims.get("role", String.class);
        AuthenticatedUser principal = new AuthenticatedUser(userId, user.get().getEmail(), role, claims.getId());
        var authority = new SimpleGrantedAuthority("ROLE_" + (role == null ? "USER" : role.toUpperCase(Locale.ROOT)));
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(authority));
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return Optional.of(auth);
    }

    /** docs/API_CONTRACT.md §5.6 */
    private static boolean isRevoked(User user, Claims claims) {
        if (user.getTokensRevokedAt() == null || claims.getIssuedAt() == null) {
            return false;
        }
        long revokedAt = user.getTokensRevokedAt().toEpochSecond(ZoneOffset.UTC);
        long issuedAt = claims.getIssuedAt().toInstant().getEpochSecond();
        return issuedAt <= revokedAt
                && (user.getRevokeExceptJti() == null || !user.getRevokeExceptJti().equals(claims.getId()));
    }
}
