package com.ecommerce.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Kullanıcı tercihleri (kullanıcı başına tek satır) — docs/API_CONTRACT.md §3 UserSettings, §4.14.
 * {@code loginAlerts}, {@code twoFactorRequired}, {@code sessionTimeout} güvenlik ayarlarıdır (§4.13);
 * {@code SecuritySettings.emailNotifications/smsNotifications} buradaki aynı sütunlara yazılır.
 */
@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
public class UserSettings extends BaseEntity {

    public static final String DEFAULT_LANGUAGE = "tr";
    public static final String DEFAULT_TIMEZONE = "Europe/Istanbul";
    public static final String DEFAULT_CURRENCY = "TRY";
    public static final String DEFAULT_THEME = "light";
    public static final int DEFAULT_ITEMS_PER_PAGE = 20;
    public static final int DEFAULT_SESSION_TIMEOUT = 30;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "email_notifications", nullable = false)
    private Boolean emailNotifications;

    @Column(name = "sms_notifications", nullable = false)
    private Boolean smsNotifications;

    @Column(name = "push_notifications", nullable = false)
    private Boolean pushNotifications;

    @Column(name = "marketing_emails", nullable = false)
    private Boolean marketingEmails;

    @Column(name = "order_updates", nullable = false)
    private Boolean orderUpdates;

    @Column(name = "price_alerts", nullable = false)
    private Boolean priceAlerts;

    @Column(name = "stock_notifications", nullable = false)
    private Boolean stockNotifications;

    @Column(name = "theme", nullable = false, length = 20)
    private String theme;

    @Column(name = "items_per_page", nullable = false)
    private Integer itemsPerPage;

    @Column(name = "auto_save_cart", nullable = false)
    private Boolean autoSaveCart;

    @Column(name = "show_product_recommendations", nullable = false)
    private Boolean showProductRecommendations;

    @Column(name = "enable_location_services", nullable = false)
    private Boolean enableLocationServices;

    @Column(name = "login_alerts", nullable = false)
    private Boolean loginAlerts;

    @Column(name = "two_factor_required", nullable = false)
    private Boolean twoFactorRequired;

    /** Dakika. */
    @Column(name = "session_timeout", nullable = false)
    private Integer sessionTimeout;

    public static UserSettings defaultsFor(Long userId) {
        UserSettings s = new UserSettings();
        s.setUserId(userId);
        s.applyPreferenceDefaults();
        s.setLoginAlerts(true);
        s.setTwoFactorRequired(false);
        s.setSessionTimeout(DEFAULT_SESSION_TIMEOUT);
        s.setIsActive(true);
        LocalDateTime now = LocalDateTime.now();
        s.setCreatedAt(now);
        s.setUpdatedAt(now);
        return s;
    }

    /** UserSettings alanlarını varsayılana döndürür (güvenlik ayarlarına dokunmaz). */
    public void applyPreferenceDefaults() {
        language = DEFAULT_LANGUAGE;
        timezone = DEFAULT_TIMEZONE;
        currency = DEFAULT_CURRENCY;
        emailNotifications = true;
        smsNotifications = false;
        pushNotifications = true;
        marketingEmails = false;
        orderUpdates = true;
        priceAlerts = true;
        stockNotifications = true;
        theme = DEFAULT_THEME;
        itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
        autoSaveCart = true;
        showProductRecommendations = true;
        enableLocationServices = false;
    }
}
