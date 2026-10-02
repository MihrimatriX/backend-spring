package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.CreateReviewDto;
import com.ecommerce.backend.application.dto.ProductReviewSummaryDto;
import com.ecommerce.backend.application.dto.ReviewDto;
import com.ecommerce.backend.application.dto.UpdateReviewDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.Review;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import com.ecommerce.backend.infrastructure.repository.RatingCountView;
import com.ecommerce.backend.infrastructure.repository.ReviewRepository;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import com.ecommerce.backend.infrastructure.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Ürün yorumları — docs/API_CONTRACT.md §4.11.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<ReviewDto> getByProduct(Long productId) {
        return reviewRepository.findByProductIdAndIsActiveTrueOrderByCreatedAtDescIdDesc(productId).stream()
                .map(ReviewService::toDto).toList();
    }

    public List<ReviewDto> getAllForAdmin(int pageNumber, int pageSize) {
        var page = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return reviewRepository.findByIsActiveTrue(page).stream().map(ReviewService::toDto).toList();
    }

    public ReviewDto getById(Long id) {
        return reviewRepository.findWithDetailsByIdAndIsActiveTrue(id).map(ReviewService::toDto)
                .orElseThrow(ReviewService::notFound);
    }

    public ProductReviewSummaryDto getSummary(Long productId) {
        long[] counts = new long[6];
        for (RatingCountView row : reviewRepository.countByRating(productId)) {
            if (row.getRating() != null && row.getRating() >= 1 && row.getRating() <= 5) {
                counts[row.getRating()] = row.getReviewCount();
            }
        }
        long total = counts[1] + counts[2] + counts[3] + counts[4] + counts[5];
        long sum = counts[1] + 2 * counts[2] + 3 * counts[3] + 4 * counts[4] + 5 * counts[5];
        return new ProductReviewSummaryDto(productId, RatingMath.average(sum, total), total, counts[1], counts[2],
                counts[3], counts[4], counts[5]);
    }

    @Transactional
    public ReviewDto create(Long userId, CreateReviewDto request) {
        if (productRepository.findById(request.productId()).filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .isEmpty()) {
            throw ApiException.badRequest("PRODUCT_NOT_FOUND", "Product not found");
        }
        if (reviewRepository.existsByUserIdAndProductIdAndIsActiveTrue(userId, request.productId())) {
            throw ApiException.badRequest("REVIEW_EXISTS",
                    "You have already reviewed this product. Edit or remove your existing review.");
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(request.productId());
        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setComment(request.comment());
        review.setIsVerified(reviewRepository.countPurchases(userId, request.productId()) > 0);
        review.setIsHelpful(false);
        review.setIsActive(true);
        Review saved = reviewRepository.saveAndFlush(review);
        return reviewRepository.findWithDetailsByIdAndIsActiveTrue(saved.getId()).map(ReviewService::toDto)
                .orElseThrow(ReviewService::notFound);
    }

    @Transactional
    public ReviewDto update(AuthenticatedUser actor, Long id, UpdateReviewDto request) {
        Review review = reviewRepository.findWithDetailsByIdAndIsActiveTrue(id).orElseThrow(ReviewService::notFound);
        if (!actor.isAdmin() && !review.getUserId().equals(actor.id())) {
            throw ApiException.forbidden("You can only edit your own reviews");
        }
        if (request.rating() != null) {
            review.setRating(request.rating());
        }
        if (request.title() != null) {
            review.setTitle(request.title());
        }
        if (request.comment() != null) {
            review.setComment(request.comment());
        }
        return toDto(reviewRepository.save(review));
    }

    @Transactional
    public void delete(AuthenticatedUser actor, Long id) {
        Review review = reviewRepository.findWithDetailsByIdAndIsActiveTrue(id).orElseThrow(ReviewService::notFound);
        if (!actor.isAdmin() && !review.getUserId().equals(actor.id())) {
            throw ApiException.forbidden("You can only delete your own reviews");
        }
        review.setIsActive(false);
        reviewRepository.save(review);
    }

    private static ApiException notFound() {
        return ApiException.notFound("REVIEW_NOT_FOUND", "Review not found");
    }

    private static ReviewDto toDto(Review r) {
        String userName = r.getUser() == null ? null
                : (r.getUser().getFirstName() + " " + r.getUser().getLastName()).trim();
        String productName = r.getProduct() == null ? null : r.getProduct().getProductName();
        return new ReviewDto(r.getId(), r.getUserId(), r.getProductId(), r.getRating(), r.getTitle(), r.getComment(),
                r.getIsVerified(), r.getIsHelpful(), userName, productName, r.getCreatedAt(), r.getUpdatedAt());
    }
}
