package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private Long id;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;

    private Boolean isVerified;
    private Boolean isHelpful;

    @NotNull(message = "User ID is required")
    private Long userId;
    private String userName; // To display user's name in review

    @NotNull(message = "Product ID is required")
    private Long productId;
    private String productName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
