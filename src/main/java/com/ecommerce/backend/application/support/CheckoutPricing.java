package com.ecommerce.backend.application.support;

import com.ecommerce.backend.infrastructure.config.CheckoutProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Satış fiyatı ve kargo hesabı (docs/API_CONTRACT.md §5.1). Sepet ve sipariş aynı kuralları
 * kullanır; eşik ve ücret {@code app.checkout.*} ayarlarından gelir.
 */
@Component
public class CheckoutPricing {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal ZERO = money(BigDecimal.ZERO);

    private final CheckoutProperties properties;

    public CheckoutPricing(CheckoutProperties properties) {
        this.properties = properties;
    }

    /** {@code round(unitPrice × (1 − discount/100), 2)} — yarım değerler yukarı yuvarlanır. */
    public static BigDecimal salePrice(BigDecimal unitPrice, Integer discountPercent) {
        int discount = discountPercent == null ? 0 : Math.max(0, Math.min(100, discountPercent));
        return unitPrice.multiply(BigDecimal.valueOf(100L - discount))
                .divide(HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal lineTotal(BigDecimal unitPrice, int quantity) {
        return money(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    /**
     * Kargo ve genel toplam. Boş sepette kargo ve toplam 0, kalan = eşik; ara toplam eşiğe
     * ulaştıysa kargo 0 ve kalan {@code null}; aksi halde standart ücret ve kalan = eşik − ara toplam.
     */
    public Quote quote(BigDecimal subtotal, boolean empty) {
        BigDecimal threshold = money(nonNegative(properties.getFreeShippingThreshold()));
        BigDecimal sub = money(subtotal);
        if (empty) {
            return new Quote(sub, ZERO, ZERO, threshold);
        }
        if (sub.compareTo(threshold) >= 0) {
            return new Quote(sub, ZERO, sub, null);
        }
        BigDecimal fee = money(nonNegative(properties.getStandardShippingFee()));
        return new Quote(sub, fee, sub.add(fee), threshold.subtract(sub));
    }

    private static BigDecimal nonNegative(BigDecimal value) {
        return value == null || value.signum() < 0 ? BigDecimal.ZERO : value;
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public record Quote(BigDecimal subtotal, BigDecimal shippingFee, BigDecimal grandTotal,
            BigDecimal freeShippingRemaining) {
    }
}
