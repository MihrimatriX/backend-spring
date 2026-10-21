package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.CreateSubCategoryDto;
import com.ecommerce.backend.application.dto.SubCategoryDto;
import com.ecommerce.backend.application.dto.UpdateSubCategoryDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.Category;
import com.ecommerce.backend.domain.entity.SubCategory;
import com.ecommerce.backend.infrastructure.repository.CategoryRepository;
import com.ecommerce.backend.infrastructure.repository.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Alt kategoriler (docs/API_CONTRACT.md §4.4). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubCategoryService {

    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;

    /** Aktif alt kategoriler; kategori adı, sonra alt kategori adı sıralı. */
    public List<SubCategoryDto> getAllSubCategories() {
        return subCategoryRepository.findAllActive().stream().map(SubCategoryService::toDto).toList();
    }

    public List<SubCategoryDto> getByCategory(Long categoryId) {
        return subCategoryRepository.findByCategoryIdAndIsActiveTrueOrderBySubCategoryNameAsc(categoryId).stream()
                .map(SubCategoryService::toDto)
                .toList();
    }

    public SubCategoryDto getSubCategory(Long id) {
        return subCategoryRepository.findWithCategoryByIdAndIsActiveTrue(id)
                .map(SubCategoryService::toDto)
                .orElseThrow(SubCategoryService::subCategoryNotFound);
    }

    @Transactional
    public SubCategoryDto createSubCategory(CreateSubCategoryDto request) {
        Category category = activeCategory(request.categoryId());
        String name = request.subCategoryName().trim();
        if (subCategoryRepository.existsByCategoryIdAndSubCategoryNameIgnoreCase(category.getId(), name)) {
            throw subCategoryExists();
        }
        SubCategory subCategory = new SubCategory(name, request.description(), category);
        subCategory.setImageUrl(request.imageUrl());
        subCategory.setIsActive(request.isActive() == null || request.isActive());
        return toDto(subCategoryRepository.saveAndFlush(subCategory));
    }

    /** Pasif alt kategoriler de güncellenebilir; {@code isActive} gönderilmezse değişmez. */
    @Transactional
    public SubCategoryDto updateSubCategory(Long id, UpdateSubCategoryDto request) {
        if (request.id() != null && !request.id().equals(id)) {
            throw ApiException.badRequest("ID_MISMATCH", "ID mismatch");
        }
        SubCategory subCategory = subCategoryRepository.findWithCategoryById(id)
                .orElseThrow(SubCategoryService::subCategoryNotFound);
        Category category = activeCategory(request.categoryId());
        String name = request.subCategoryName().trim();
        if (subCategoryRepository.existsByCategoryIdAndSubCategoryNameIgnoreCaseAndIdNot(category.getId(), name, id)) {
            throw subCategoryExists();
        }
        subCategory.setSubCategoryName(name);
        subCategory.setDescription(request.description());
        subCategory.setImageUrl(request.imageUrl());
        subCategory.setCategory(category);
        if (request.isActive() != null) {
            subCategory.setIsActive(request.isActive());
        }
        return toDto(subCategoryRepository.saveAndFlush(subCategory));
    }

    /** Yumuşak silme; önceden pasife alınmış kayıt da silinebilir. */
    @Transactional
    public void deleteSubCategory(Long id) {
        SubCategory subCategory = subCategoryRepository.findWithCategoryById(id)
                .orElseThrow(SubCategoryService::subCategoryNotFound);
        subCategory.setIsActive(false);
        subCategoryRepository.save(subCategory);
    }

    private Category activeCategory(Long categoryId) {
        return categoryRepository.findByIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> ApiException.badRequest("CATEGORY_NOT_FOUND", "Category not found"));
    }

    private static ApiException subCategoryNotFound() {
        return ApiException.notFound("SUBCATEGORY_NOT_FOUND", "SubCategory not found");
    }

    private static ApiException subCategoryExists() {
        return ApiException.badRequest("SUBCATEGORY_EXISTS", "SubCategory name already exists in this category");
    }

    static SubCategoryDto toDto(SubCategory subCategory) {
        Category category = subCategory.getCategory();
        return new SubCategoryDto(
                subCategory.getId(),
                subCategory.getSubCategoryName(),
                subCategory.getDescription(),
                subCategory.getImageUrl(),
                category.getId(),
                category.getCategoryName(),
                subCategory.getIsActive(),
                subCategory.getCreatedAt(),
                subCategory.getUpdatedAt());
    }
}
