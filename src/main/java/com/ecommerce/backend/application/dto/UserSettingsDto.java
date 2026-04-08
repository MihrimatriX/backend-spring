package com.ecommerce.backend.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDto {
    private Long id;
    private Long userId;
    private String language = "tr";
    private String currency = "TRY";
    private String timezone = "Europe/Istanbul";
    private boolean emailNotifications = true;
    private boolean smsNotifications = false;
    private boolean pushNotifications = true;
    private boolean marketingEmails = false;
    private String theme = "light";
    private int itemsPerPage = 12;
    private boolean twoFactorEnabled = false;
    private String dateFormat = "dd/MM/yyyy";
    private String timeFormat = "24h";
}
