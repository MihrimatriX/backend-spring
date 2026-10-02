package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.CategoryDto;
import com.ecommerce.backend.application.dto.CreateCategoryDto;
import com.ecommerce.backend.application.dto.UpdateCategoryDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.Category;
import com.ecommerce.backend.infrastructure.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Kategoriler (docs/API_CONTRACT.md §4.3). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /** Aktif kategoriler, ada göre sıralı. */
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByCategoryNameAsc().stream()
                .map(CategoryService::toDto)
                .toList();
    }

    public CategoryDto getCategory(Long id) {
        return categoryRepository.findByIdAndIsActiveTrue(id)
                .map(CategoryService::toDto)
                .orElseThrow(CategoryService::categoryNotFound);
    }

    @Transactional
    public CategoryDto createCategory(CreateCategoryDto request) {
        String name = request.categoryName().trim();
        if (categoryRepository.existsByCategoryNameIgnoreCase(name)) {
            throw categoryExists();
        }
        Category category = new Category(name, request.description(), request.imageUrl());
        category.setIsActive(request.isActive() == null || request.isActive());
        return toDto(categoryRepository.saveAndFlush(category));
    }

    /** Pasif kategoriler de güncellenebilir; {@code isActive} gönderilmezse değişmez. */
    @Transactional
    public CategoryDto updateCategory(Long id, UpdateCategoryDto request) {
        Category category = categoryRepository.findById(id).orElseThrow(CategoryService::categoryNotFound);
        String name = request.categoryName().trim();
        if (categoryRepository.existsByCategoryNameIgnoreCaseAndIdNot(name, id)) {
            throw categoryExists();
        }
        category.setCategoryName(name);
        category.setDescription(request.description());
        category.setImageUrl(request.imageUrl());
        if (request.isActive() != null) {
            category.setIsActive(request.isActive());
        }
        return toDto(categoryRepository.saveAndFlush(category));
    }

    /** Yumuşak silme. */
    @Transactional
    public String deleteCategory(Long id) {
        Category category = categoryRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(CategoryService::categoryNotFound);
        category.setIsActive(false);
        categoryRepository.save(category);
        return "Category deleted successfully";
    }

    private static ApiException categoryNotFound() {
        return ApiException.notFound("CATEGORY_NOT_FOUND", "Category not found");
    }

    private static ApiException categoryExists() {
        return ApiException.badRequest("CATEGORY_EXISTS", "Category name already exists");
    }

    static CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getCategoryName(),
                category.getDescription(),
                category.getImageUrl(),
                category.getIsActive(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
