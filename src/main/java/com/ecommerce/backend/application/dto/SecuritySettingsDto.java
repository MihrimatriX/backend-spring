package com.ecommerce.backend.application.dto;

public class SecuritySettingsDto {
    private Boolean emailNotifications = true;
    private Boolean smsNotifications = false;
    private Boolean loginAlerts = true;
    private Boolean twoFactorRequired = false;
    private Integer sessionTimeout = 30; // minutes

    // Constructors
    public SecuritySettingsDto() {
    }

    public SecuritySettingsDto(Boolean emailNotifications, Boolean smsNotifications, Boolean loginAlerts,
            Boolean twoFactorRequired, Integer sessionTimeout) {
        this.emailNotifications = emailNotifications;
        this.smsNotifications = smsNotifications;
        this.loginAlerts = loginAlerts;
        this.twoFactorRequired = twoFactorRequired;
        this.sessionTimeout = sessionTimeout;
    }

    // Getters and Setters
    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public Boolean getSmsNotifications() {
        return smsNotifications;
    }

    public void setSmsNotifications(Boolean smsNotifications) {
        this.smsNotifications = smsNotifications;
    }

    public Boolean getLoginAlerts() {
        return loginAlerts;
    }

    public void setLoginAlerts(Boolean loginAlerts) {
        this.loginAlerts = loginAlerts;
    }

    public Boolean getTwoFactorRequired() {
        return twoFactorRequired;
    }

    public void setTwoFactorRequired(Boolean twoFactorRequired) {
        this.twoFactorRequired = twoFactorRequired;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
