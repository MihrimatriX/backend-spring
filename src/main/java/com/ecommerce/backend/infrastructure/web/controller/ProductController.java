package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.application.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@CrossOrigin(origins = "*")
@Tag(name = "Products", description = "Product management operations")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve a paginated list of products with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    public ResponseEntity<BaseResponseDto<Page<ProductDto>>> getProducts(
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Minimum price filter") @RequestParam(required = false) String minPrice,
            @Parameter(description = "Maximum price filter") @RequestParam(required = false) String maxPrice,
            @Parameter(description = "Search term for product name or description") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort order (asc/desc)") @RequestParam(defaultValue = "asc") String sortOrder,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer pageNumber,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "12") Integer pageSize) {

        ProductFilterDto filterDto = new ProductFilterDto();
        filterDto.setCategoryId(categoryId);
        if (minPrice != null && !minPrice.isEmpty()) {
            filterDto.setMinPrice(new java.math.BigDecimal(minPrice));
        }
        if (maxPrice != null && !maxPrice.isEmpty()) {
            filterDto.setMaxPrice(new java.math.BigDecimal(maxPrice));
        }
        filterDto.setSearchTerm(searchTerm);
        filterDto.setSortBy(sortBy);
        filterDto.setSortOrder(sortOrder);
        filterDto.setPageNumber(pageNumber);
        filterDto.setPageSize(pageSize);

        BaseResponseDto<Page<ProductDto>> response = productService.getProducts(filterDto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<BaseResponseDto<ProductDto>> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        BaseResponseDto<ProductDto> response = productService.getProductById(id);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<BaseResponseDto<java.util.List<ProductDto>>> getProductsByCategory(
            @PathVariable Long categoryId) {
        BaseResponseDto<java.util.List<ProductDto>> response = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponseDto<java.util.List<ProductDto>>> searchProducts(@RequestParam String q) {
        BaseResponseDto<java.util.List<ProductDto>> response = productService.searchProducts(q);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/featured")
    public ResponseEntity<BaseResponseDto<java.util.List<ProductDto>>> getFeaturedProducts() {
        BaseResponseDto<java.util.List<ProductDto>> response = productService.getFeaturedProducts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/discounted")
    public ResponseEntity<BaseResponseDto<java.util.List<ProductDto>>> getDiscountedProducts() {
        BaseResponseDto<java.util.List<ProductDto>> response = productService.getDiscountedProducts();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<BaseResponseDto<ProductDto>> createProduct(@Valid @RequestBody ProductDto productDto) {
        BaseResponseDto<ProductDto> response = productService.createProduct(productDto);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponseDto<ProductDto>> updateProduct(@PathVariable Long id,
            @Valid @RequestBody ProductDto productDto) {
        BaseResponseDto<ProductDto> response = productService.updateProduct(id, productDto);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponseDto<String>> deleteProduct(@PathVariable Long id) {
        BaseResponseDto<String> response = productService.deleteProduct(id);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
