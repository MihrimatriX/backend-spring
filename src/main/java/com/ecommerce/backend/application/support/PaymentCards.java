package com.ecommerce.backend.application.support;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Kart numarası doğrulama/maskeleme ve son kullanma kontrolü (docs/API_CONTRACT.md §3, §4.9).
 * Tam kart numarası ve CVV hiçbir zaman saklanmaz; yalnızca {@code **** **** **** 1111} biçimi tutulur.
 */
public final class PaymentCards {

    public static final String MASK_PREFIX = "**** **** **** ";

    private PaymentCards() {
    }

    /** Boşluk/tire temizlenmiş numara 12–19 rakamdan oluşuyorsa rakamları, aksi halde {@code null} döner. */
    public static String normalizeCardNumber(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[\\s-]", "");
        return digits.matches("\\d{12,19}") ? digits : null;
    }

    public static boolean isMasked(String value) {
        return value != null && value.indexOf('*') >= 0;
    }

    /** Geçerli rakam dizisinden saklanacak maskeli biçim: {@code **** **** **** 1111}. */
    public static String maskCardNumber(String digits) {
        return MASK_PREFIX + digits.substring(digits.length() - 4);
    }

    /** Saklanan değeri (eski kayıtlar dahil) her zaman maskeli gösterir. */
    public static String displayCardNumber(String stored) {
        if (stored == null || isMasked(stored)) {
            return stored;
        }
        String digits = stored.replaceAll("\\D", "");
        return digits.length() >= 4 ? maskCardNumber(digits) : MASK_PREFIX + "****";
    }

    /** Hesap numarası → {@code ****1234}; boşsa {@code null}. */
    public static String maskAccountNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        if (isMasked(raw)) {
            return raw.trim();
        }
        String compact = raw.replaceAll("\\s", "");
        return compact.length() > 4 ? "****" + compact.substring(compact.length() - 4) : "****";
    }

    /** Kart, son kullanma ayının son günü (UTC) geçtiyse süresi dolmuştur. */
    public static boolean isExpired(Integer expiryMonth, Integer expiryYear, LocalDate todayUtc) {
        if (expiryMonth == null || expiryYear == null || expiryMonth < 1 || expiryMonth > 12) {
            return true;
        }
        return todayUtc.isAfter(YearMonth.of(expiryYear, expiryMonth).atEndOfMonth());
    }
}
