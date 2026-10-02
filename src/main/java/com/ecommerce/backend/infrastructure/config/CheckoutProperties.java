package com.ecommerce.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Kargo ve ödeme simülasyonu kuralları (TRY) — docs/API_CONTRACT.md §5.1.
 * .NET karşılığı {@code Checkout:*}.
 */
@ConfigurationProperties(prefix = "app.checkout")
public class CheckoutProperties {

    /** Ara toplam bu tutarın altındaysa kargo uygulanır. */
    private BigDecimal freeShippingThreshold = new BigDecimal("150.00");
    /** Standart kargo ücreti. */
    private BigDecimal standardShippingFee = new BigDecimal("34.99");
    /** Ödeme simülasyonunun reddetme olasılığı (0–100). Üretimde 0 bırakın. */
    private int simulatedPaymentDeclinePercent = 0;

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public BigDecimal getStandardShippingFee() {
        return standardShippingFee;
    }

    public void setStandardShippingFee(BigDecimal standardShippingFee) {
        this.standardShippingFee = standardShippingFee;
    }

    public int getSimulatedPaymentDeclinePercent() {
        return simulatedPaymentDeclinePercent;
    }

    public void setSimulatedPaymentDeclinePercent(int simulatedPaymentDeclinePercent) {
        this.simulatedPaymentDeclinePercent = simulatedPaymentDeclinePercent;
    }
}
