package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SecurityDto {
    private Long userId;
    private String email;
    private Boolean isEmailVerified;
    private LocalDateTime lastPasswordChange;
    private Boolean twoFactorEnabled;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private List<LoginHistoryDto> recentLogins;

    // Constructors
    public SecurityDto() {
    }

    public SecurityDto(Long userId, String email, Boolean isEmailVerified, LocalDateTime lastPasswordChange,
            Boolean twoFactorEnabled, LocalDateTime lastLoginAt, String lastLoginIp,
            List<LoginHistoryDto> recentLogins) {
        this.userId = userId;
        this.email = email;
        this.isEmailVerified = isEmailVerified;
        this.lastPasswordChange = lastPasswordChange;
        this.twoFactorEnabled = twoFactorEnabled;
        this.lastLoginAt = lastLoginAt;
        this.lastLoginIp = lastLoginIp;
        this.recentLogins = recentLogins;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    public LocalDateTime getLastPasswordChange() {
        return lastPasswordChange;
    }

    public void setLastPasswordChange(LocalDateTime lastPasswordChange) {
        this.lastPasswordChange = lastPasswordChange;
    }

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public List<LoginHistoryDto> getRecentLogins() {
        return recentLogins;
    }

    public void setRecentLogins(List<LoginHistoryDto> recentLogins) {
        this.recentLogins = recentLogins;
    }
}
