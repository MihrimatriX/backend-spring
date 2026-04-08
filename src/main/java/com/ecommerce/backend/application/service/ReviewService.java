package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.domain.entity.Review;
import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import com.ecommerce.backend.infrastructure.repository.ReviewRepository;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public BaseResponseDto<List<ReviewDto>> getProductReviews(Long productId) {
        try {
            log.info("Fetching reviews for product ID: {}", productId);

            List<Review> reviews = reviewRepository.findActiveReviewsByProductId(productId);
            List<ReviewDto> reviewDtos = reviews.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            log.info("Found {} reviews for product ID: {}", reviewDtos.size(), productId);
            return BaseResponseDto.success("Reviews retrieved successfully", reviewDtos);
        } catch (Exception e) {
            log.error("Error retrieving reviews for product ID: {}", productId, e);
            return BaseResponseDto.error("Error retrieving reviews: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public BaseResponseDto<ProductReviewSummaryDto> getProductReviewSummary(Long productId) {
        try {
            log.info("Fetching review summary for product ID: {}", productId);

            Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
            Long totalReviews = reviewRepository.countActiveReviewsByProductId(productId);

            Long rating1Count = reviewRepository.countReviewsByProductIdAndRating(productId, 1);
            Long rating2Count = reviewRepository.countReviewsByProductIdAndRating(productId, 2);
            Long rating3Count = reviewRepository.countReviewsByProductIdAndRating(productId, 3);
            Long rating4Count = reviewRepository.countReviewsByProductIdAndRating(productId, 4);
            Long rating5Count = reviewRepository.countReviewsByProductIdAndRating(productId, 5);

            ProductReviewSummaryDto summary = new ProductReviewSummaryDto(
                    productId,
                    averageRating != null ? averageRating : 0.0,
                    totalReviews != null ? totalReviews : 0L,
                    rating1Count != null ? rating1Count : 0L,
                    rating2Count != null ? rating2Count : 0L,
                    rating3Count != null ? rating3Count : 0L,
                    rating4Count != null ? rating4Count : 0L,
                    rating5Count != null ? rating5Count : 0L);

            log.info("Review summary for product ID {}: {} reviews, {} average rating",
                    productId, totalReviews, averageRating);
            return BaseResponseDto.success("Review summary retrieved successfully", summary);
        } catch (Exception e) {
            log.error("Error retrieving review summary for product ID: {}", productId, e);
            return BaseResponseDto.error("Error retrieving review summary: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public BaseResponseDto<ReviewDto> getReviewById(Long reviewId) {
        try {
            log.info("Fetching review with ID: {}", reviewId);

            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

            if (!review.getIsActive()) {
                throw new RuntimeException("Review not found with ID: " + reviewId);
            }

            ReviewDto reviewDto = convertToDto(review);
            log.info("Review found with ID: {}", reviewId);
            return BaseResponseDto.success("Review retrieved successfully", reviewDto);
        } catch (Exception e) {
            log.error("Error retrieving review with ID: {}", reviewId, e);
            return BaseResponseDto.error("Error retrieving review: " + e.getMessage());
        }
    }

    public BaseResponseDto<ReviewDto> createReview(CreateReviewDto createReviewDto) {
        try {
            log.info("Creating review for product ID: {} by user ID: {}",
                    createReviewDto.getProductId(), createReviewDto.getUserId());

            // Check if product exists
            Product product = productRepository.findById(createReviewDto.getProductId())
                    .orElseThrow(
                            () -> new RuntimeException("Product not found with ID: " + createReviewDto.getProductId()));

            // Check if user exists
            User user = userRepository.findById(createReviewDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + createReviewDto.getUserId()));

            // Check if user already reviewed this product
            if (reviewRepository.existsByUserIdAndProductIdAndIsActiveTrue(
                    createReviewDto.getUserId(), createReviewDto.getProductId())) {
                throw new RuntimeException("User has already reviewed this product");
            }

            Review review = new Review();
            review.setRating(createReviewDto.getRating());
            review.setTitle(createReviewDto.getTitle());
            review.setComment(createReviewDto.getComment());
            review.setUserId(createReviewDto.getUserId());
            review.setProductId(createReviewDto.getProductId());
            review.setIsVerified(false);
            review.setIsHelpful(false);
            review.setIsActive(true);

            Review savedReview = reviewRepository.save(review);
            ReviewDto reviewDto = convertToDto(savedReview);

            log.info("Review created successfully with ID: {}", savedReview.getId());
            return BaseResponseDto.success("Review created successfully", reviewDto);
        } catch (Exception e) {
            log.error("Error creating review", e);
            return BaseResponseDto.error("Error creating review: " + e.getMessage());
        }
    }

    public BaseResponseDto<ReviewDto> updateReview(Long reviewId, UpdateReviewDto updateReviewDto) {
        try {
            log.info("Updating review with ID: {}", reviewId);

            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

            if (!review.getIsActive()) {
                throw new RuntimeException("Review not found with ID: " + reviewId);
            }

            if (updateReviewDto.getRating() != null) {
                review.setRating(updateReviewDto.getRating());
            }
            if (updateReviewDto.getTitle() != null) {
                review.setTitle(updateReviewDto.getTitle());
            }
            if (updateReviewDto.getComment() != null) {
                review.setComment(updateReviewDto.getComment());
            }

            Review updatedReview = reviewRepository.save(review);
            ReviewDto reviewDto = convertToDto(updatedReview);

            log.info("Review updated successfully with ID: {}", reviewId);
            return BaseResponseDto.success("Review updated successfully", reviewDto);
        } catch (Exception e) {
            log.error("Error updating review with ID: {}", reviewId, e);
            return BaseResponseDto.error("Error updating review: " + e.getMessage());
        }
    }

    public BaseResponseDto<String> deleteReview(Long reviewId) {
        try {
            log.info("Deleting review with ID: {}", reviewId);

            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

            if (!review.getIsActive()) {
                throw new RuntimeException("Review not found with ID: " + reviewId);
            }

            // Soft delete
            review.setIsActive(false);
            reviewRepository.save(review);

            log.info("Review deleted successfully with ID: {}", reviewId);
            return BaseResponseDto.success("Review deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting review with ID: {}", reviewId, e);
            return BaseResponseDto.error("Error deleting review: " + e.getMessage());
        }
    }

    private ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setComment(review.getComment());
        dto.setIsVerified(review.getIsVerified());
        dto.setIsHelpful(review.getIsHelpful());
        dto.setUserId(review.getUserId());
        dto.setProductId(review.getProductId());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        // Set user name if user is loaded
        if (review.getUser() != null) {
            dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
        }

        // Set product name if product is loaded
        if (review.getProduct() != null) {
            dto.setProductName(review.getProduct().getProductName());
        }

        return dto;
    }
}
