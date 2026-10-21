package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CreateSubCategoryDto;
import com.ecommerce.backend.application.dto.SubCategoryDto;
import com.ecommerce.backend.application.dto.UpdateSubCategoryDto;
import com.ecommerce.backend.application.service.SubCategoryService;
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

/** Alt kategoriler — docs/API_CONTRACT.md §4.4 (yazma uçları Admin, SecurityConfig'te). */
@RestController
@RequestMapping("/api/subcategory")
@RequiredArgsConstructor
@Tag(name = "SubCategories", description = "SubCategory management operations")
public class SubCategoryController {

    private final SubCategoryService subCategoryService;

    @GetMapping
    @Operation(summary = "Get all subcategories", description = "Active subcategories, by category then name")
    public ResponseEntity<BaseResponseDto<List<SubCategoryDto>>> getAllSubCategories() {
        return ApiResponses.ok("SubCategories retrieved successfully", subCategoryService.getAllSubCategories());
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get subcategories of a category")
    public ResponseEntity<BaseResponseDto<List<SubCategoryDto>>> getByCategory(@PathVariable Long categoryId) {
        return ApiResponses.ok("SubCategories retrieved successfully", subCategoryService.getByCategory(categoryId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subcategory by ID")
    public ResponseEntity<BaseResponseDto<SubCategoryDto>> getSubCategory(@PathVariable Long id) {
        return ApiResponses.ok("SubCategory retrieved successfully", subCategoryService.getSubCategory(id));
    }

    @PostMapping
    @Operation(summary = "Create subcategory")
    public ResponseEntity<BaseResponseDto<SubCategoryDto>> createSubCategory(
            @Valid @RequestBody CreateSubCategoryDto dto) {
        return ApiResponses.created("SubCategory created successfully", subCategoryService.createSubCategory(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update subcategory")
    public ResponseEntity<BaseResponseDto<SubCategoryDto>> updateSubCategory(@PathVariable Long id,
            @Valid @RequestBody UpdateSubCategoryDto dto) {
        return ApiResponses.ok("SubCategory updated successfully", subCategoryService.updateSubCategory(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete subcategory (soft)")
    public ResponseEntity<BaseResponseDto<Boolean>> deleteSubCategory(@PathVariable Long id) {
        subCategoryService.deleteSubCategory(id);
        return ApiResponses.ok("SubCategory deleted successfully", true);
    }
}
