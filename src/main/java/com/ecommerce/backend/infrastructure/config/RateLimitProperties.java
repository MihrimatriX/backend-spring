package com.ecommerce.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IP başına dakikalık istek limiti (docs/API_CONTRACT.md §1.4). 0 = kapalı.
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private int permitPerMinute = 300;

    public int getPermitPerMinute() {
        return permitPerMinute;
    }

    public void setPermitPerMinute(int permitPerMinute) {
        this.permitPerMinute = permitPerMinute;
    }
}
