package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;

public class LoginHistoryDto {
    private Long id;
    private LocalDateTime loginAt;
    private String ipAddress;
    private String userAgent;
    private String location;
    private Boolean isSuccessful;

    // Constructors
    public LoginHistoryDto() {
    }

    public LoginHistoryDto(Long id, LocalDateTime loginAt, String ipAddress, String userAgent,
            String location, Boolean isSuccessful) {
        this.id = id;
        this.loginAt = loginAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.location = location;
        this.isSuccessful = isSuccessful;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(LocalDateTime loginAt) {
        this.loginAt = loginAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getIsSuccessful() {
        return isSuccessful;
    }

    public void setIsSuccessful(Boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }
}
