package com.ecommerce.backend.application.dto;

public class UpdateNotificationDto {
    private Boolean isRead;

    // Constructors
    public UpdateNotificationDto() {}

    public UpdateNotificationDto(Boolean isRead) {
        this.isRead = isRead;
    }

    // Getters and Setters
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
}
