package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.PagedResultDto;
import com.ecommerce.backend.application.dto.ProductDto;
import com.ecommerce.backend.application.dto.ProductFilterDto;
import com.ecommerce.backend.application.dto.ProductRequestDto;
import com.ecommerce.backend.application.service.ProductService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** Ürünler — docs/API_CONTRACT.md §4.2 (yazma uçları Admin, SecurityConfig'te). */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management operations")
public class ProductController {

    private static final int DEFAULT_PAGE_SIZE = 12;

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get products", description = "Paged, filterable list of active products")
    public ResponseEntity<BaseResponseDto<PagedResultDto<ProductDto>>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "Id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        ProductFilterDto filter = new ProductFilterDto(categoryId, subCategoryId, minPrice, maxPrice, searchTerm,
                sortBy, sortOrder, ApiResponses.page(pageNumber), ApiResponses.size(pageSize, DEFAULT_PAGE_SIZE));
        return ApiResponses.ok("Products retrieved successfully", productService.getProducts(filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<BaseResponseDto<ProductDto>> getProduct(@PathVariable Long id) {
        return ApiResponses.ok("Product retrieved successfully", productService.getProduct(id));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products of a category")
    public ResponseEntity<BaseResponseDto<List<ProductDto>>> getProductsByCategory(@PathVariable Long categoryId) {
        return ApiResponses.ok("Products retrieved successfully", productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name or description")
    public ResponseEntity<BaseResponseDto<List<ProductDto>>> searchProducts(@RequestParam String q) {
        return ApiResponses.ok("Products retrieved successfully", productService.searchProducts(q));
    }

    @GetMapping("/featured")
    @Operation(summary = "Featured products", description = "Discount > 20% or added in the last 7 days")
    public ResponseEntity<BaseResponseDto<List<ProductDto>>> getFeaturedProducts() {
        return ApiResponses.ok("Featured products retrieved successfully", productService.getFeaturedProducts());
    }

    @GetMapping("/discounted")
    @Operation(summary = "Discounted products", description = "Highest discount first")
    public ResponseEntity<BaseResponseDto<List<ProductDto>>> getDiscountedProducts() {
        return ApiResponses.ok("Discounted products retrieved successfully", productService.getDiscountedProducts());
    }

    @PostMapping
    @Operation(summary = "Create product")
    public ResponseEntity<BaseResponseDto<ProductDto>> createProduct(@Valid @RequestBody ProductRequestDto dto) {
        return ApiResponses.created("Product created successfully", productService.createProduct(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<BaseResponseDto<ProductDto>> updateProduct(@PathVariable Long id,
            @Valid @RequestBody ProductRequestDto dto) {
        return ApiResponses.ok("Product updated successfully", productService.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product (soft)")
    public ResponseEntity<BaseResponseDto<String>> deleteProduct(@PathVariable Long id) {
        String message = productService.deleteProduct(id);
        return ApiResponses.ok(message, message);
    }
}
