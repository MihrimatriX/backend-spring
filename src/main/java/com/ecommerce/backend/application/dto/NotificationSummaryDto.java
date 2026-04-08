package com.ecommerce.backend.application.dto;

import java.util.List;

public class NotificationSummaryDto {
    private Long totalNotifications;
    private Long unreadNotifications;
    private List<NotificationDto> recentNotifications;

    // Constructors
    public NotificationSummaryDto() {
    }

    public NotificationSummaryDto(Long totalNotifications, Long unreadNotifications,
            List<NotificationDto> recentNotifications) {
        this.totalNotifications = totalNotifications;
        this.unreadNotifications = unreadNotifications;
        this.recentNotifications = recentNotifications;
    }

    // Getters and Setters
    public Long getTotalNotifications() {
        return totalNotifications;
    }

    public void setTotalNotifications(Long totalNotifications) {
        this.totalNotifications = totalNotifications;
    }

    public Long getUnreadNotifications() {
        return unreadNotifications;
    }

    public void setUnreadNotifications(Long unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
    }

    public List<NotificationDto> getRecentNotifications() {
        return recentNotifications;
    }

    public void setRecentNotifications(List<NotificationDto> recentNotifications) {
        this.recentNotifications = recentNotifications;
    }
}
