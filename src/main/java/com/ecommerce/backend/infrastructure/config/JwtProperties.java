package com.ecommerce.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT ayarları (docs/API_CONTRACT.md §2). Varsayılanlar .NET backend ile aynıdır; üretimde
 * {@code JWT_SECRET} ortam değişkeni ile değiştirin.
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "mySecretKeyThatIsAtLeast256BitsLongForJWTTokenSecurity";
    /** Token ömrü (milisaniye). */
    private long expiration = 86_400_000L;
    private String issuer = "EcommerceBackend";
    private String audience = "EcommerceUsers";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }
}
