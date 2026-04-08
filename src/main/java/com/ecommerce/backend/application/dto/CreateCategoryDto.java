package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryDto {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String categoryName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;
}
