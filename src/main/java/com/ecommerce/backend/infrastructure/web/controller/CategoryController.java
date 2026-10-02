package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CategoryDto;
import com.ecommerce.backend.application.dto.CreateCategoryDto;
import com.ecommerce.backend.application.dto.UpdateCategoryDto;
import com.ecommerce.backend.application.service.CategoryService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Kategoriler (§4.3)")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<BaseResponseDto<List<CategoryDto>>> getAll() {
        return ApiResponses.ok("Categories retrieved successfully", categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<CategoryDto>> getById(@PathVariable Long id) {
        return ApiResponses.ok("Category retrieved successfully", categoryService.getCategory(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<CategoryDto>> create(@Valid @RequestBody CreateCategoryDto request) {
        return ApiResponses.created("Category created successfully", categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<CategoryDto>> update(@PathVariable Long id,
            @Valid @RequestBody UpdateCategoryDto request) {
        return ApiResponses.ok("Category updated successfully", categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<String>> delete(@PathVariable Long id) {
        return ApiResponses.ok("Category deleted successfully", categoryService.deleteCategory(id));
    }
}
