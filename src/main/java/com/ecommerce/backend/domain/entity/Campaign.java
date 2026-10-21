package com.ecommerce.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
public class Campaign extends BaseEntity {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 300, message = "Subtitle cannot exceed 300 characters")
    @Column(name = "subtitle", length = 300)
    private String subtitle;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    @Min(value = 0, message = "Discount cannot be negative")
    @Max(value = 100, message = "Discount cannot exceed 100%")
    @Column(name = "discount")
    private Integer discount = 0;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "background_color")
    private String backgroundColor;

    @Column(name = "time_left")
    private String timeLeft;

    @Size(max = 50, message = "Button text cannot exceed 50 characters")
    @Column(name = "button_text", length = 50)
    private String buttonText;

    @Column(name = "button_href")
    private String buttonHref;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    // Constructors
    public Campaign() {
    }

    public Campaign(String title, String subtitle, String description, Integer discount,
            String imageUrl, String backgroundColor, String timeLeft,
            String buttonText, String buttonHref, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.discount = discount;
        this.imageUrl = imageUrl;
        this.backgroundColor = backgroundColor;
        this.timeLeft = timeLeft;
        this.buttonText = buttonText;
        this.buttonHref = buttonHref;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(String timeLeft) {
        this.timeLeft = timeLeft;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public String getButtonHref() {
        return buttonHref;
    }

    public void setButtonHref(String buttonHref) {
        this.buttonHref = buttonHref;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
