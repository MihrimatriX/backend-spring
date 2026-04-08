package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.*;

public class CreateNotificationDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;

    @NotBlank(message = "Type is required")
    @Size(max = 50, message = "Type cannot exceed 50 characters")
    private String type;

    @Size(max = 100, message = "Action URL cannot exceed 100 characters")
    private String actionUrl;

    // Constructors
    public CreateNotificationDto() {
    }

    public CreateNotificationDto(Long userId, String title, String message, String type, String actionUrl) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.actionUrl = actionUrl;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}
