package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CategoryDto;
import com.ecommerce.backend.application.dto.CreateCategoryDto;
import com.ecommerce.backend.application.dto.UpdateCategoryDto;
import com.ecommerce.backend.application.service.CategoryService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Kategoriler — docs/API_CONTRACT.md §4.3 (yazma uçları Admin, SecurityConfig'te). */
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management operations")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Active categories, ordered by name")
    public ResponseEntity<BaseResponseDto<List<CategoryDto>>> getAllCategories() {
        return ApiResponses.ok("Categories retrieved successfully", categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<BaseResponseDto<CategoryDto>> getCategory(@PathVariable Long id) {
        return ApiResponses.ok("Category retrieved successfully", categoryService.getCategory(id));
    }

    @PostMapping
    @Operation(summary = "Create category")
    public ResponseEntity<BaseResponseDto<CategoryDto>> createCategory(@Valid @RequestBody CreateCategoryDto dto) {
        return ApiResponses.created("Category created successfully", categoryService.createCategory(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<BaseResponseDto<CategoryDto>> updateCategory(@PathVariable Long id,
            @Valid @RequestBody UpdateCategoryDto dto) {
        return ApiResponses.ok("Category updated successfully", categoryService.updateCategory(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category (soft)")
    public ResponseEntity<BaseResponseDto<String>> deleteCategory(@PathVariable Long id) {
        String message = categoryService.deleteCategory(id);
        return ApiResponses.ok(message, message);
    }
}
