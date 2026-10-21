package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CreateReviewDto;
import com.ecommerce.backend.application.dto.ProductReviewSummaryDto;
import com.ecommerce.backend.application.dto.ReviewDto;
import com.ecommerce.backend.application.dto.UpdateReviewDto;
import com.ecommerce.backend.application.service.ReviewService;
import com.ecommerce.backend.infrastructure.security.CurrentUserService;
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

import java.util.List;

/** Ürün yorumları — docs/API_CONTRACT.md §4.11 ({@code /admin} SecurityConfig'te Admin). */
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
@Tag(name = "Review Management", description = "APIs for managing product reviews")
public class ReviewController {

    private static final int ADMIN_PAGE_SIZE = 50;

    private final ReviewService reviewService;
    private final CurrentUserService currentUserService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews of a product", description = "Active reviews, newest first")
    public ResponseEntity<BaseResponseDto<List<ReviewDto>>> getProductReviews(@PathVariable Long productId) {
        return ApiResponses.ok("Reviews retrieved successfully", reviewService.getProductReviews(productId));
    }

    @GetMapping("/product/{productId}/summary")
    @Operation(summary = "Get review summary of a product")
    public ResponseEntity<BaseResponseDto<ProductReviewSummaryDto>> getProductReviewSummary(
            @PathVariable Long productId) {
        return ApiResponses.ok("Review summary retrieved successfully",
                reviewService.getProductReviewSummary(productId));
    }

    @GetMapping("/admin")
    @Operation(summary = "Admin: all active reviews", description = "Newest first")
    public ResponseEntity<BaseResponseDto<List<ReviewDto>>> getAllReviews(
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        return ApiResponses.ok("Reviews retrieved successfully", reviewService.getAllReviews(
                ApiResponses.page(pageNumber), ApiResponses.size(pageSize, ADMIN_PAGE_SIZE)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<BaseResponseDto<ReviewDto>> getReview(@PathVariable Long id) {
        return ApiResponses.ok("Review retrieved successfully", reviewService.getReview(id));
    }

    @PostMapping
    @Operation(summary = "Create review", description = "Author is taken from the token")
    public ResponseEntity<BaseResponseDto<ReviewDto>> createReview(@Valid @RequestBody CreateReviewDto dto) {
        return ApiResponses.created("Review created successfully",
                reviewService.createReview(currentUserService.requireUserId(), dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update review (owner or admin)")
    public ResponseEntity<BaseResponseDto<ReviewDto>> updateReview(@PathVariable Long id,
            @Valid @RequestBody UpdateReviewDto dto) {
        return ApiResponses.ok("Review updated successfully", reviewService.updateReview(id,
                currentUserService.requireUserId(), currentUserService.isAdmin(), dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete review (owner or admin, soft)")
    public ResponseEntity<BaseResponseDto<String>> deleteReview(@PathVariable Long id) {
        String message = reviewService.deleteReview(id, currentUserService.requireUserId(),
                currentUserService.isAdmin());
        return ApiResponses.ok(message, message);
    }
}
