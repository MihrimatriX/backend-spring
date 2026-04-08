package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.domain.entity.Category;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import com.ecommerce.backend.infrastructure.repository.CategoryRepository;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MetricsService metricsService;

    public BaseResponseDto<Page<ProductDto>> getProducts(ProductFilterDto filterDto) {
        Timer.Sample sample = metricsService.startProductSearchTimer();
        try {
            Sort sort = Sort.by(
                    filterDto.getSortOrder().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                    filterDto.getSortBy());

            Pageable pageable = PageRequest.of(
                    filterDto.getPageNumber() - 1,
                    filterDto.getPageSize(),
                    sort);

            Page<Product> products = StringUtils.hasText(filterDto.getSearchTerm())
                    ? productRepository.findWithFiltersWithSearch(
                            filterDto.getCategoryId(),
                            filterDto.getMinPrice(),
                            filterDto.getMaxPrice(),
                            filterDto.getSearchTerm().trim(),
                            pageable)
                    : productRepository.findWithFiltersWithoutSearch(
                            filterDto.getCategoryId(),
                            filterDto.getMinPrice(),
                            filterDto.getMaxPrice(),
                            pageable);

            Page<ProductDto> productDtos = products.map(this::convertToDto);

            metricsService.recordProductSearchTime(sample);
            return BaseResponseDto.success("Products retrieved successfully", productDtos);
        } catch (Exception e) {
            metricsService.recordProductSearchTime(sample);
            return BaseResponseDto.error("Error retrieving products: " + e.getMessage());
        }
    }

    @Cacheable(value = "products", key = "#id")
    public BaseResponseDto<ProductDto> getProductById(Long id) {
        try {
            Product product = productRepository.findById(id)
                    .filter(Product::getIsActive)
                    .orElse(null);

            if (product == null) {
                return BaseResponseDto.error("Product not found");
            }

            // Record product view metrics
            metricsService.incrementProductViewCounter(id);

            return BaseResponseDto.success("Product retrieved successfully", convertToDto(product));
        } catch (Exception e) {
            return BaseResponseDto.error("Error retrieving product: " + e.getMessage());
        }
    }

    @Cacheable(value = "products", key = "'category_' + #categoryId")
    public BaseResponseDto<List<ProductDto>> getProductsByCategory(Long categoryId) {
        try {
            List<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId);
            List<ProductDto> productDtos = products.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Products retrieved successfully", productDtos);
        } catch (Exception e) {
            return BaseResponseDto.error("Error retrieving products: " + e.getMessage());
        }
    }

    public BaseResponseDto<List<ProductDto>> searchProducts(String searchTerm) {
        try {
            List<Product> products = productRepository.searchProducts(searchTerm);
            List<ProductDto> productDtos = products.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Products retrieved successfully", productDtos);
        } catch (Exception e) {
            return BaseResponseDto.error("Error searching products: " + e.getMessage());
        }
    }

    public BaseResponseDto<List<ProductDto>> getFeaturedProducts() {
        try {
            List<Product> products = productRepository.findFeaturedProducts();
            List<ProductDto> productDtos = products.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Featured products retrieved successfully", productDtos);
        } catch (Exception e) {
            return BaseResponseDto.error("Error retrieving featured products: " + e.getMessage());
        }
    }

    public BaseResponseDto<List<ProductDto>> getDiscountedProducts() {
        try {
            List<Product> products = productRepository.findDiscountedProducts();
            List<ProductDto> productDtos = products.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Discounted products retrieved successfully", productDtos);
        } catch (Exception e) {
            return BaseResponseDto.error("Error retrieving discounted products: " + e.getMessage());
        }
    }

    public BaseResponseDto<ProductDto> createProduct(ProductDto productDto) {
        try {
            if (productRepository.existsByProductNameAndIsActiveTrue(productDto.getProductName())) {
                return BaseResponseDto.error("Product with this name already exists");
            }

            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .filter(Category::getIsActive)
                    .orElse(null);

            if (category == null) {
                return BaseResponseDto.error("Category not found");
            }

            Product product = convertToEntity(productDto, category);
            Product savedProduct = productRepository.save(product);

            return BaseResponseDto.success("Product created successfully", convertToDto(savedProduct));
        } catch (Exception e) {
            return BaseResponseDto.error("Error creating product: " + e.getMessage());
        }
    }

    public BaseResponseDto<ProductDto> updateProduct(Long id, ProductDto productDto) {
        try {
            Product existingProduct = productRepository.findById(id)
                    .filter(Product::getIsActive)
                    .orElse(null);

            if (existingProduct == null) {
                return BaseResponseDto.error("Product not found");
            }

            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .filter(Category::getIsActive)
                    .orElse(null);

            if (category == null) {
                return BaseResponseDto.error("Category not found");
            }

            updateEntity(existingProduct, productDto, category);
            Product updatedProduct = productRepository.save(existingProduct);

            return BaseResponseDto.success("Product updated successfully", convertToDto(updatedProduct));
        } catch (Exception e) {
            return BaseResponseDto.error("Error updating product: " + e.getMessage());
        }
    }

    public BaseResponseDto<String> deleteProduct(Long id) {
        try {
            Product product = productRepository.findById(id)
                    .filter(Product::getIsActive)
                    .orElse(null);

            if (product == null) {
                return BaseResponseDto.error("Product not found");
            }

            product.setIsActive(false);
            productRepository.save(product);

            return BaseResponseDto.success("Product deleted successfully");
        } catch (Exception e) {
            return BaseResponseDto.error("Error deleting product: " + e.getMessage());
        }
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setUnitPrice(product.getUnitPrice());
        dto.setUnitInStock(product.getUnitInStock());
        dto.setQuantityPerUnit(product.getQuantityPerUnit());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getCategoryName());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setDiscount(product.getDiscount());
        dto.setIsActive(product.getIsActive());
        return dto;
    }

    private Product convertToEntity(ProductDto dto, Category category) {
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setUnitPrice(dto.getUnitPrice());
        product.setUnitInStock(dto.getUnitInStock());
        product.setQuantityPerUnit(dto.getQuantityPerUnit());
        product.setCategory(category);
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        product.setDiscount(dto.getDiscount());
        product.setIsActive(dto.getIsActive());
        return product;
    }

    private void updateEntity(Product product, ProductDto dto, Category category) {
        product.setProductName(dto.getProductName());
        product.setUnitPrice(dto.getUnitPrice());
        product.setUnitInStock(dto.getUnitInStock());
        product.setQuantityPerUnit(dto.getQuantityPerUnit());
        product.setCategory(category);
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        product.setDiscount(dto.getDiscount());
        product.setIsActive(dto.getIsActive());
    }
}
