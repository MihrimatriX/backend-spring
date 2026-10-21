package com.ecommerce.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/** {@code PUT /api/settings/user} — kısmi güncelleme: null (veya boş metin) alan değişmez. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserSettingsDto {

    @Size(max = 10, message = "Language cannot exceed 10 characters")
    private String language;

    @Size(max = 50, message = "Timezone cannot exceed 50 characters")
    private String timezone;

    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String currency;

    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean pushNotifications;
    private Boolean marketingEmails;
    private Boolean orderUpdates;
    private Boolean priceAlerts;
    private Boolean stockNotifications;

    @Size(max = 20, message = "Theme cannot exceed 20 characters")
    private String theme;

    @Min(value = 5, message = "Items per page must be between 5 and 100")
    @Max(value = 100, message = "Items per page must be between 5 and 100")
    private Integer itemsPerPage;

    private Boolean autoSaveCart;
    private Boolean showProductRecommendations;
    private Boolean enableLocationServices;
}
