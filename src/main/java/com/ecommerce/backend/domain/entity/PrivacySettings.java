package com.ecommerce.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Gizlilik tercihleri (kullanıcı başına tek satır) — docs/API_CONTRACT.md §3 PrivacySettings. */
@Entity
@Table(name = "privacy_settings")
@Getter
@Setter
@NoArgsConstructor
public class PrivacySettings extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "profile_visibility", nullable = false)
    private Boolean profileVisibility;

    @Column(name = "show_email", nullable = false)
    private Boolean showEmail;

    @Column(name = "show_phone", nullable = false)
    private Boolean showPhone;

    @Column(name = "allow_data_collection", nullable = false)
    private Boolean allowDataCollection;

    @Column(name = "allow_analytics", nullable = false)
    private Boolean allowAnalytics;

    @Column(name = "allow_cookies", nullable = false)
    private Boolean allowCookies;

    @Column(name = "allow_marketing", nullable = false)
    private Boolean allowMarketing;

    @Column(name = "data_sharing", nullable = false)
    private Boolean dataSharing;

    public static PrivacySettings defaultsFor(Long userId) {
        PrivacySettings s = new PrivacySettings();
        s.setUserId(userId);
        s.applyDefaults();
        s.setIsActive(true);
        LocalDateTime now = LocalDateTime.now();
        s.setCreatedAt(now);
        s.setUpdatedAt(now);
        return s;
    }

    public void applyDefaults() {
        profileVisibility = true;
        showEmail = false;
        showPhone = false;
        allowDataCollection = true;
        allowAnalytics = true;
        allowCookies = true;
        allowMarketing = false;
        dataSharing = false;
    }
}
