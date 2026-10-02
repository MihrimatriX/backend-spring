package com.ecommerce.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * docs/API_CONTRACT.md §3 — SecuritySettings. İstekte null alan değişmez; cevapta tüm alanlar doludur.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecuritySettingsDto {
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean loginAlerts;
    private Boolean twoFactorRequired;

    /** Dakika. */
    @Min(value = 1, message = "Session timeout must be between 1 and 1440 minutes")
    @Max(value = 1440, message = "Session timeout must be between 1 and 1440 minutes")
    private Integer sessionTimeout;
}
