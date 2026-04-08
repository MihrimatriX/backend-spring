package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CategoryDto;
import com.ecommerce.backend.application.dto.CreateCategoryDto;
import com.ecommerce.backend.application.dto.UpdateCategoryDto;
import com.ecommerce.backend.domain.entity.Category;
import com.ecommerce.backend.infrastructure.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private CategoryService self;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Autowired
    @Lazy
    public void setSelf(CategoryService self) {
        this.self = self;
    }

    public BaseResponseDto<List<CategoryDto>> getAllCategories() {
        try {
            return BaseResponseDto.success("Categories retrieved successfully", self.cachedAllCategoryDtos());
        } catch (Exception e) {
            log.error("Error retrieving categories", e);
            return BaseResponseDto.error("Error retrieving categories: " + e.getMessage());
        }
    }

    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryDto> cachedAllCategoryDtos() {
        return categoryRepository.findByIsActiveTrueOrderByCategoryNameAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BaseResponseDto<CategoryDto> getCategoryById(Long id) {
        try {
            CategoryDto dto = self.cachedCategoryDtoById(id);
            if (dto == null) {
                return BaseResponseDto.error("Category not found");
            }
            return BaseResponseDto.success("Category retrieved successfully", dto);
        } catch (Exception e) {
            log.error("Error retrieving category with id: {}", id, e);
            return BaseResponseDto.error("Error retrieving category: " + e.getMessage());
        }
    }

    @Cacheable(value = "categories", key = "#id", unless = "#result == null")
    public CategoryDto cachedCategoryDtoById(Long id) {
        return categoryRepository.findByIdAndIsActiveTrue(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public BaseResponseDto<CategoryDto> createCategory(CreateCategoryDto createCategoryDto) {
        try {
            // Check if category name already exists
            if (categoryRepository.existsByCategoryNameAndIsActiveTrue(createCategoryDto.getCategoryName())) {
                return BaseResponseDto.error("Category name already exists");
            }

            Category category = new Category();
            category.setCategoryName(createCategoryDto.getCategoryName());
            category.setDescription(createCategoryDto.getDescription());
            category.setImageUrl(createCategoryDto.getImageUrl());
            category.setIsActive(true);
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());

            Category savedCategory = categoryRepository.save(category);
            CategoryDto categoryDto = convertToDto(savedCategory);

            log.info("Category created successfully with id: {}", savedCategory.getId());
            return BaseResponseDto.success("Category created successfully", categoryDto);
        } catch (Exception e) {
            log.error("Error creating category", e);
            return BaseResponseDto.error("Error creating category: " + e.getMessage());
        }
    }

    @CacheEvict(value = "categories", allEntries = true)
    public BaseResponseDto<CategoryDto> updateCategory(Long id, UpdateCategoryDto updateCategoryDto) {
        try {
            Category category = categoryRepository.findByIdAndIsActiveTrue(id)
                    .orElse(null);

            if (category == null) {
                return BaseResponseDto.error("Category not found");
            }

            // Check if new category name already exists (excluding current category)
            if (!category.getCategoryName().equals(updateCategoryDto.getCategoryName()) &&
                    categoryRepository.existsByCategoryNameAndIsActiveTrue(updateCategoryDto.getCategoryName())) {
                return BaseResponseDto.error("Category name already exists");
            }

            category.setCategoryName(updateCategoryDto.getCategoryName());
            category.setDescription(updateCategoryDto.getDescription());
            category.setImageUrl(updateCategoryDto.getImageUrl());
            category.setUpdatedAt(LocalDateTime.now());

            Category savedCategory = categoryRepository.save(category);
            CategoryDto categoryDto = convertToDto(savedCategory);

            log.info("Category updated successfully with id: {}", savedCategory.getId());
            return BaseResponseDto.success("Category updated successfully", categoryDto);
        } catch (Exception e) {
            log.error("Error updating category with id: {}", id, e);
            return BaseResponseDto.error("Error updating category: " + e.getMessage());
        }
    }

    @CacheEvict(value = "categories", allEntries = true)
    public BaseResponseDto<Void> deleteCategory(Long id) {
        try {
            Category category = categoryRepository.findByIdAndIsActiveTrue(id)
                    .orElse(null);

            if (category == null) {
                return BaseResponseDto.error("Category not found");
            }

            // Soft delete
            category.setIsActive(false);
            category.setUpdatedAt(LocalDateTime.now());
            categoryRepository.save(category);

            log.info("Category deleted successfully with id: {}", id);
            return BaseResponseDto.success("Category deleted successfully", null);
        } catch (Exception e) {
            log.error("Error deleting category with id: {}", id, e);
            return BaseResponseDto.error("Error deleting category: " + e.getMessage());
        }
    }

    public BaseResponseDto<Page<CategoryDto>> getCategories(int page, int size, String sortBy, String sortOrder) {
        try {
            Sort sort = Sort.by(
                    sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                    sortBy);

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Category> categories = categoryRepository.findByIsActiveTrue(pageable);
            Page<CategoryDto> categoryDtos = categories.map(this::convertToDto);

            return BaseResponseDto.success("Categories retrieved successfully", categoryDtos);
        } catch (Exception e) {
            log.error("Error retrieving categories with pagination", e);
            return BaseResponseDto.error("Error retrieving categories: " + e.getMessage());
        }
    }

    private CategoryDto convertToDto(Category category) {
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
