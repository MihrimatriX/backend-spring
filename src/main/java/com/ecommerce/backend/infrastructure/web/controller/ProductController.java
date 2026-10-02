package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.PagedResultDto;
import com.ecommerce.backend.application.dto.ProductDto;
import com.ecommerce.backend.application.dto.ProductFilterDto;
import com.ecommerce.backend.application.dto.ProductRequestDto;
import com.ecommerce.backend.application.service.ProductService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Ürünler (§4.2)")
public class ProductController {

    private final ProductService productService;

    @GetMapping
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
        var filter = new ProductFilterDto(categoryId, subCategoryId, minPrice, maxPrice, searchTerm, sortBy, sortOrder,
                ApiResponses.page(pageNumber), ApiResponses.size(pageSize, 12));
        return ApiResponses.ok("Products retrieved successfully", productService.getProducts(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<ProductDto>> getById(@PathVariable Long id) {
        return ApiResponses.ok("Product retrieved successfully", productService.getProduct(id));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<BaseResponseDto<List<ProductDto>>> getByCategory(@PathVariable Long categoryId) {
        return ApiResponses.ok("Products retrieved successfully", productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponseDto<List<ProductDto>>> search(@RequestParam String q) {
        return ApiResponses.ok("Products retrieved successfully", productService.searchProducts(q));
    }

    @GetMapping("/featured")
    public ResponseEntity<BaseResponseDto<List<ProductDto>>> featured() {
        return ApiResponses.ok("Featured products retrieved successfully", productService.getFeaturedProducts());
    }

    @GetMapping("/discounted")
    public ResponseEntity<BaseResponseDto<List<ProductDto>>> discounted() {
        return ApiResponses.ok("Discounted products retrieved successfully", productService.getDiscountedProducts());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<ProductDto>> create(@Valid @RequestBody ProductRequestDto request) {
        return ApiResponses.created("Product created successfully", productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<ProductDto>> update(@PathVariable Long id,
            @Valid @RequestBody ProductRequestDto request) {
        return ApiResponses.ok("Product updated successfully", productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<String>> delete(@PathVariable Long id) {
        return ApiResponses.ok("Product deleted successfully", productService.deleteProduct(id));
    }
}
