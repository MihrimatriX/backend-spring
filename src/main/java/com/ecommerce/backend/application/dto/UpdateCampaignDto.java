package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCampaignDto {
    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;
    
    @Size(max = 300, message = "Subtitle cannot exceed 300 characters")
    private String subtitle;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @Min(value = 0, message = "Discount must be at least 0")
    @Max(value = 100, message = "Discount must be at most 100")
    private Integer discount;
    
    private String imageUrl;
    private String backgroundColor;
    private String timeLeft;
    
    @Size(max = 50, message = "Button text cannot exceed 50 characters")
    private String buttonText;
    
    private String buttonHref;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    
    private Boolean isActive;
}
