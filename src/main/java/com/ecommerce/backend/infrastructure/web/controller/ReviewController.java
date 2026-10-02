package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CreateReviewDto;
import com.ecommerce.backend.application.dto.ProductReviewSummaryDto;
import com.ecommerce.backend.application.dto.ReviewDto;
import com.ecommerce.backend.application.dto.UpdateReviewDto;
import com.ecommerce.backend.application.service.ReviewService;
import com.ecommerce.backend.infrastructure.security.CurrentUserService;
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

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Ürün yorumları (§4.11)")
public class ReviewController {

    private static final String OK = "Operation successful";

    private final ReviewService reviewService;
    private final CurrentUserService currentUserService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<List<ReviewDto>>> getAll(@RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        return ApiResponses.ok(OK, reviewService.getAllForAdmin(ApiResponses.page(pageNumber),
                ApiResponses.size(pageSize, 50)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<BaseResponseDto<List<ReviewDto>>> getByProduct(@PathVariable Long productId) {
        return ApiResponses.ok(OK, reviewService.getByProduct(productId));
    }

    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<BaseResponseDto<ProductReviewSummaryDto>> getSummary(@PathVariable Long productId) {
        return ApiResponses.ok(OK, reviewService.getSummary(productId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<ReviewDto>> getById(@PathVariable Long id) {
        return ApiResponses.ok(OK, reviewService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BaseResponseDto<ReviewDto>> create(@Valid @RequestBody CreateReviewDto request) {
        return ApiResponses.created(OK, reviewService.create(currentUserService.requireUserId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponseDto<ReviewDto>> update(@PathVariable Long id,
            @Valid @RequestBody UpdateReviewDto request) {
        return ApiResponses.ok(OK, reviewService.update(currentUserService.require(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponseDto<String>> delete(@PathVariable Long id) {
        reviewService.delete(currentUserService.require(), id);
        return ApiResponses.ok("Review deleted successfully", "Review deleted successfully");
    }
}
