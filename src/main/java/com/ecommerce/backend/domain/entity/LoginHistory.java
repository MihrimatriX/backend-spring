package com.ecommerce.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_histories")
public class LoginHistory extends BaseEntity {

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull(message = "Login time is required")
    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 500, message = "User agent cannot exceed 500 characters")
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Size(max = 100, message = "Location cannot exceed 100 characters")
    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "is_successful")
    private Boolean isSuccessful = true;

    @Size(max = 200, message = "Failure reason cannot exceed 200 characters")
    @Column(name = "failure_reason", length = 200)
    private String failureReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    // Constructors
    public LoginHistory() {
    }

    public LoginHistory(Long userId, LocalDateTime loginAt, String ipAddress, String userAgent,
            String location, Boolean isSuccessful, String failureReason) {
        this.userId = userId;
        this.loginAt = loginAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.location = location;
        this.isSuccessful = isSuccessful;
        this.failureReason = failureReason;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
