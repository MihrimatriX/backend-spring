package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.PagedResultDto;
import com.ecommerce.backend.application.dto.ProductDto;
import com.ecommerce.backend.application.dto.ProductFilterDto;
import com.ecommerce.backend.application.dto.ProductRequestDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.Category;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.domain.entity.SubCategory;
import com.ecommerce.backend.infrastructure.repository.CategoryRepository;
import com.ecommerce.backend.infrastructure.repository.ProductRatingView;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import com.ecommerce.backend.infrastructure.repository.ProductSpecifications;
import com.ecommerce.backend.infrastructure.repository.ReviewRepository;
import com.ecommerce.backend.infrastructure.repository.SubCategoryRepository;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Ürün kataloğu (docs/API_CONTRACT.md §4.2). Stok ve puanlar her istekte veritabanından okunur
 * (önbellek yok: stok sipariş akışında, puanlar yorumlarda değişir).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    /** Öne çıkan / indirimli vitrin listelerinin üst sınırı. */
    static final int SHOWCASE_LIMIT = 20;
    private static final Duration FEATURED_NEW_WINDOW = Duration.ofDays(7);
    /** Puan toplamı sorgusundaki IN listesinin parça boyutu. */
    private static final int RATING_LOOKUP_CHUNK = 1000;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ReviewRepository reviewRepository;
    private final MetricsService metricsService;

    public PagedResultDto<ProductDto> getProducts(ProductFilterDto filter) {
        Timer.Sample sample = metricsService.startProductSearchTimer();
        try {
            Specification<Product> spec = ProductSpecifications.activeMatching(filter.categoryId(),
                    filter.subCategoryId(), filter.minPrice(), filter.maxPrice(), filter.searchTerm());
            long offset = (long) (filter.pageNumber() - 1) * filter.pageSize();
            if (offset > Integer.MAX_VALUE) {
                return PagedResultDto.of(List.of(), productRepository.count(spec), filter.pageNumber(),
                        filter.pageSize());
            }
            Page<Product> page = productRepository.findAll(spec, PageRequest.of(filter.pageNumber() - 1,
                    filter.pageSize(), sortFor(filter.sortBy(), filter.sortOrder())));
            return PagedResultDto.of(toDtos(page.getContent()), page.getTotalElements(), filter.pageNumber(),
                    filter.pageSize());
        } finally {
            metricsService.recordProductSearchTime(sample);
        }
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findWithCatalogByIdAndIsActiveTrue(id).orElseThrow(this::productNotFound);
        metricsService.incrementProductViewCounter(id);
        return toDto(product);
    }

    public List<ProductDto> getProductsByCategory(Long categoryId) {
        return toDtos(productRepository.findAll(
                ProductSpecifications.activeMatching(categoryId, null, null, null, null), Sort.by("id")));
    }

    public List<ProductDto> searchProducts(String searchTerm) {
        return toDtos(productRepository.findAll(
                ProductSpecifications.activeMatching(null, null, null, null, searchTerm), Sort.by("id")));
    }

    /** İndirim > %20 veya son 7 günde eklenmiş; en yeni önce, en fazla 20. */
    public List<ProductDto> getFeaturedProducts() {
        LocalDateTime since = LocalDateTime.now(ZoneOffset.UTC).minus(FEATURED_NEW_WINDOW);
        return toDtos(productRepository.findFeatured(since, Limit.of(SHOWCASE_LIMIT)));
    }

    /** İndirim > 0; indirim oranı yüksek olan önce, en fazla 20. */
    public List<ProductDto> getDiscountedProducts() {
        return toDtos(productRepository.findDiscounted(Limit.of(SHOWCASE_LIMIT)));
    }

    @Transactional
    public ProductDto createProduct(ProductRequestDto request) {
        Category category = activeCategory(request.categoryId());
        SubCategory subCategory = resolveSubCategory(request.subCategoryId(), category, null);

        Product product = new Product();
        apply(product, request, category, subCategory);
        product.setIsActive(request.isActive() == null || request.isActive());
        return toDto(productRepository.saveAndFlush(product));
    }

    /** Pasif ürünler de güncellenebilir; {@code isActive} gönderilmezse değişmez. */
    @Transactional
    public ProductDto updateProduct(Long id, ProductRequestDto request) {
        Product product = productRepository.findWithCatalogById(id).orElseThrow(this::productNotFound);
        Category category = activeCategory(request.categoryId());
        SubCategory subCategory = resolveSubCategory(request.subCategoryId(), category, product.getSubCategory());

        apply(product, request, category, subCategory);
        if (request.isActive() != null) {
            product.setIsActive(request.isActive());
        }
        return toDto(productRepository.saveAndFlush(product));
    }

    /** Yumuşak silme. */
    @Transactional
    public String deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::getIsActive)
                .orElseThrow(this::productNotFound);
        product.setIsActive(false);
        productRepository.save(product);
        return "Product deleted successfully";
    }

    /**
     * Sıralama anahtarı (harf duyarsız): {@code Id}, {@code ProductName/name}, {@code UnitPrice/price},
     * {@code CreatedAt}, {@code Discount}, {@code UnitInStock/stock}; bilinmeyen → {@code Id}. Sayfalamanın
     * kararlı olması için ikincil anahtar her zaman aynı yöndeki {@code id}'dir.
     */
    static Sort sortFor(String sortBy, String sortOrder) {
        Sort.Direction direction = sortOrder != null && "desc".equalsIgnoreCase(sortOrder.trim())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        String key = sortBy == null ? "" : sortBy.trim().toLowerCase(Locale.ROOT);
        String property = switch (key) {
            case "productname", "name" -> "productName";
            case "unitprice", "price" -> "unitPrice";
            case "createdat" -> "createdAt";
            case "discount" -> "discount";
            case "unitinstock", "stock" -> "unitInStock";
            default -> "id";
        };
        Sort sort = Sort.by(direction, property);
        return "id".equals(property) ? sort : sort.and(Sort.by(direction, "id"));
    }

    private Category activeCategory(Long categoryId) {
        return categoryRepository.findByIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> ApiException.badRequest("CATEGORY_NOT_FOUND", "Category not found"));
    }

    /**
     * Alt kategori verildiyse aynı kategoriye ait ve aktif olmalıdır. Ürünün mevcut alt kategorisi sonradan
     * pasife alınmışsa, ürün aynı alt kategoriyle güncellenebilir.
     */
    private SubCategory resolveSubCategory(Long subCategoryId, Category category, SubCategory current) {
        if (subCategoryId == null) {
            return null;
        }
        return subCategoryRepository.findById(subCategoryId)
                .filter(s -> s.getCategory().getId().equals(category.getId()))
                .filter(s -> s.getIsActive() || (current != null && current.getId().equals(s.getId())))
                .orElseThrow(() -> ApiException.badRequest("SUBCATEGORY_NOT_FOUND", "SubCategory not found"));
    }

    private static void apply(Product product, ProductRequestDto request, Category category,
            SubCategory subCategory) {
        product.setProductName(request.productName().trim());
        product.setUnitPrice(request.unitPrice());
        product.setUnitInStock(request.unitInStock());
        product.setQuantityPerUnit(request.quantityPerUnit());
        product.setCategory(category);
        product.setSubCategory(subCategory);
        product.setDescription(request.description());
        product.setImageUrl(request.imageUrl());
        product.setDiscount(request.discount() == null ? 0 : request.discount());
    }

    private ApiException productNotFound() {
        return ApiException.notFound("PRODUCT_NOT_FOUND", "Product not found");
    }

    private ProductDto toDto(Product product) {
        return toDtos(List.of(product)).get(0);
    }

    /** Puanlar, listedeki tüm ürünler için tek gruplu sorguyla (büyük listelerde parça parça) okunur. */
    private List<ProductDto> toDtos(List<Product> products) {
        Map<Long, ProductRatingView> ratings = new HashMap<>();
        List<Long> ids = products.stream().map(Product::getId).toList();
        for (int from = 0; from < ids.size(); from += RATING_LOOKUP_CHUNK) {
            List<Long> chunk = ids.subList(from, Math.min(ids.size(), from + RATING_LOOKUP_CHUNK));
            reviewRepository.findRatingTotals(chunk).forEach(r -> ratings.put(r.getProductId(), r));
        }
        return products.stream().map(p -> toDto(p, ratings.get(p.getId()))).toList();
    }

    private static ProductDto toDto(Product product, ProductRatingView rating) {
        Category category = product.getCategory();
        SubCategory subCategory = product.getSubCategory();
        long totalReviews = rating == null ? 0 : rating.getReviewCount();
        double averageRating = rating == null ? 0 : RatingMath.average(rating.getRatingSum(), totalReviews);
        return new ProductDto(
                product.getId(),
                product.getProductName(),
                product.getUnitPrice(),
                product.getUnitInStock(),
                product.getQuantityPerUnit(),
                category.getId(),
                category.getCategoryName(),
                subCategory == null ? null : subCategory.getId(),
                subCategory == null ? null : subCategory.getSubCategoryName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getDiscount() == null ? 0 : product.getDiscount(),
                product.getIsActive(),
                averageRating,
                totalReviews,
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
