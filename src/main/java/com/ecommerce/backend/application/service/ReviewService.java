package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.CreateReviewDto;
import com.ecommerce.backend.application.dto.ProductReviewSummaryDto;
import com.ecommerce.backend.application.dto.ReviewDto;
import com.ecommerce.backend.application.dto.UpdateReviewDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.domain.entity.Review;
import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import com.ecommerce.backend.infrastructure.repository.RatingCountView;
import com.ecommerce.backend.infrastructure.repository.ReviewRepository;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Ürün yorumları (docs/API_CONTRACT.md §4.11). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /** Aktif yorumlar, en yeni önce. */
    public List<ReviewDto> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdAndIsActiveTrueOrderByCreatedAtDescIdDesc(productId).stream()
                .map(r -> toDto(r, r.getUser(), r.getProduct()))
                .toList();
    }

    /** Yorum yoksa tüm değerler 0. */
    public ProductReviewSummaryDto getProductReviewSummary(Long productId) {
        long[] counts = new long[6];
        long total = 0;
        long sum = 0;
        for (RatingCountView row : reviewRepository.countByRating(productId)) {
            int rating = row.getRating();
            long count = row.getReviewCount();
            if (rating >= 1 && rating <= 5) {
                counts[rating] = count;
            }
            total += count;
            sum += rating * count;
        }
        return new ProductReviewSummaryDto(productId, RatingMath.average(sum, total), total,
                counts[1], counts[2], counts[3], counts[4], counts[5]);
    }

    public ReviewDto getReview(Long id) {
        Review review = activeReview(id);
        return toDto(review, review.getUser(), review.getProduct());
    }

    /** Yönetici listesi: tüm aktif yorumlar, en yeni önce. */
    public List<ReviewDto> getAllReviews(int pageNumber, int pageSize) {
        Sort newestFirst = Sort.by(Sort.Direction.DESC, "createdAt", "id");
        return reviewRepository.findByIsActiveTrue(PageRequest.of(pageNumber - 1, pageSize, newestFirst)).stream()
                .map(r -> toDto(r, r.getUser(), r.getProduct()))
                .toList();
    }

    /**
     * Kullanıcı başına ürün başına tek aktif yorum. {@code isVerified}: kullanıcının bu ürünü içeren
     * iptal edilmemiş bir siparişi var mı.
     */
    @Transactional
    public ReviewDto createReview(Long userId, CreateReviewDto request) {
        Product product = productRepository.findById(request.productId())
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .orElseThrow(() -> ApiException.badRequest("PRODUCT_NOT_FOUND", "Product not found"));
        if (reviewRepository.existsByUserIdAndProductIdAndIsActiveTrue(userId, product.getId())) {
            throw ApiException.badRequest("REVIEW_EXISTS",
                    "You have already reviewed this product. Edit or remove your existing review.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("UNAUTHORIZED", "Kimlik doğrulama gerekli."));

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(product.getId());
        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setComment(request.comment());
        review.setIsVerified(reviewRepository.countPurchases(userId, product.getId()) > 0);
        review.setIsHelpful(false);
        review.setIsActive(true);
        return toDto(reviewRepository.saveAndFlush(review), user, product);
    }

    /** Kısmi güncelleme; yalnızca sahip veya yönetici. */
    @Transactional
    public ReviewDto updateReview(Long id, Long userId, boolean admin, UpdateReviewDto request) {
        Review review = activeReview(id);
        if (!admin && !review.getUserId().equals(userId)) {
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
        reviewRepository.saveAndFlush(review);
        return toDto(review, review.getUser(), review.getProduct());
    }

    /** Yumuşak silme; yalnızca sahip veya yönetici. */
    @Transactional
    public String deleteReview(Long id, Long userId, boolean admin) {
        Review review = activeReview(id);
        if (!admin && !review.getUserId().equals(userId)) {
            throw ApiException.forbidden("You can only delete your own reviews");
        }
        review.setIsActive(false);
        reviewRepository.save(review);
        return "Review deleted successfully";
    }

    private Review activeReview(Long id) {
        return reviewRepository.findWithDetailsByIdAndIsActiveTrue(id)
                .orElseThrow(() -> ApiException.notFound("REVIEW_NOT_FOUND", "Review not found"));
    }

    private static ReviewDto toDto(Review review, User user, Product product) {
        return new ReviewDto(
                review.getId(),
                review.getUserId(),
                review.getProductId(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                review.getIsVerified(),
                review.getIsHelpful(),
                user == null ? null : (user.getFirstName() + " " + user.getLastName()).trim(),
                product == null ? null : product.getProductName(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}
