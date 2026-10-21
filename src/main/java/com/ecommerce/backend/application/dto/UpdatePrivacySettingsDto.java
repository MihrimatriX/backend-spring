package com.ecommerce.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/** {@code PUT /api/settings/privacy} — kısmi güncelleme: null alan değişmez. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePrivacySettingsDto {
    private Boolean profileVisibility;
    private Boolean showEmail;
    private Boolean showPhone;
    private Boolean allowDataCollection;
    private Boolean allowAnalytics;
    private Boolean allowCookies;
    private Boolean allowMarketing;
    private Boolean dataSharing;
}
