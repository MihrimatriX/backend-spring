package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.application.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review Management", description = "APIs for managing product reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get all reviews for a specific product", description = "Retrieves all active reviews for the specified product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponseDto<List<ReviewDto>>> getProductReviews(
            @Parameter(description = "Product ID") @PathVariable Long productId) {

        log.info("GET /api/review/product/{} - Getting reviews for product", productId);
        BaseResponseDto<List<ReviewDto>> response = reviewService.getProductReviews(productId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/product/{productId}/summary")
    @Operation(summary = "Get review summary for a product", description = "Retrieves summary statistics for product reviews")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review summary retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponseDto<ProductReviewSummaryDto>> getProductReviewSummary(
            @Parameter(description = "Product ID") @PathVariable Long productId) {

        log.info("GET /api/review/product/{}/summary - Getting review summary for product", productId);
        BaseResponseDto<ProductReviewSummaryDto> response = reviewService.getProductReviewSummary(productId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get a review by ID", description = "Retrieves a specific review by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponseDto<ReviewDto>> getReviewById(
            @Parameter(description = "Review ID") @PathVariable Long reviewId) {

        log.info("GET /api/review/{} - Getting review by ID", reviewId);
        BaseResponseDto<ReviewDto> response = reviewService.getReviewById(reviewId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping
    @Operation(summary = "Create a new review", description = "Creates a new review for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "User has already reviewed this product"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponseDto<ReviewDto>> createReview(
            @Parameter(description = "Review data") @Valid @RequestBody CreateReviewDto createReviewDto) {

        log.info("POST /api/review - Creating new review for product {} by user {}",
                createReviewDto.getProductId(), createReviewDto.getUserId());
        BaseResponseDto<ReviewDto> response = reviewService.createReview(createReviewDto);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            if (response.getMessage().contains("already reviewed")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "Update an existing review", description = "Updates an existing review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponseDto<ReviewDto>> updateReview(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @Parameter(description = "Updated review data") @Valid @RequestBody UpdateReviewDto updateReviewDto) {

        log.info("PUT /api/review/{} - Updating review", reviewId);
        BaseResponseDto<ReviewDto> response = reviewService.updateReview(reviewId, updateReviewDto);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            if (response.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete a review", description = "Soft deletes a review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponseDto<String>> deleteReview(
            @Parameter(description = "Review ID") @PathVariable Long reviewId) {

        log.info("DELETE /api/review/{} - Deleting review", reviewId);
        BaseResponseDto<String> response = reviewService.deleteReview(reviewId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
