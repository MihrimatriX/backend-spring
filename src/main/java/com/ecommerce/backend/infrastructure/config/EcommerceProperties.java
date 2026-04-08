package com.ecommerce.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * E-ticaret demo / gerçek dünya senaryoları için uygulama bayrakları.
 */
@ConfigurationProperties(prefix = "app.ecommerce")
public class EcommerceProperties {

    /**
     * Açıkken sipariş sahibi, kargoya çıkış ve teslimat adımlarını API ile simüle
     * edebilir (eğitim / demo).
     */
    private boolean demoFulfillmentEnabled = false;

    public boolean isDemoFulfillmentEnabled() {
        return demoFulfillmentEnabled;
    }

    public void setDemoFulfillmentEnabled(boolean demoFulfillmentEnabled) {
        this.demoFulfillmentEnabled = demoFulfillmentEnabled;
    }
}
