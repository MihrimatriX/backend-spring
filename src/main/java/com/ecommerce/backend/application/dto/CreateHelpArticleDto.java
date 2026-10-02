package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Yönetici — {@code POST /api/helpsupport/articles}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateHelpArticleDto {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    private List<String> tags = new ArrayList<>();

    private Boolean isPublished = true;
}
