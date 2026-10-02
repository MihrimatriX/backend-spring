package com.ecommerce.backend.infrastructure.security;

import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * HS256 JWT üretir/doğrular. Claim seti .NET backend ile aynıdır: {@code sub} (kullanıcı id),
 * {@code email}, {@code role}, {@code jti}, {@code iat}, {@code exp}, {@code iss}, {@code aud}
 * (docs/API_CONTRACT.md §2).
 */
@Service
public class JwtService {

    /** .NET JwtBearer varsayılanı ile aynı saat kayması toleransı. */
    private static final long CLOCK_SKEW_SECONDS = 300;

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", role)
                .id(UUID.randomUUID().toString())
                .issuer(properties.getIssuer())
                .audience().add(properties.getAudience()).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(properties.getExpiration())))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** İmza, süre, issuer ve audience geçerliyse claim'leri döner. */
    public Optional<Claims> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(properties.getIssuer())
                    .requireAudience(properties.getAudience())
                    .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
