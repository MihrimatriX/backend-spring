package com.ecommerce.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    /**
     * Optimistic lock — prevents lost updates when two checkouts touch the same
     * SKU.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    @Column(name = "product_name", nullable = false)
    private String productName;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Unit price must have at most 10 integer digits and 2 decimal places")
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "Unit in stock is required")
    @Min(value = 0, message = "Unit in stock cannot be negative")
    @Column(name = "unit_in_stock", nullable = false)
    private Integer unitInStock;

    @NotBlank(message = "Quantity per unit is required")
    @Size(max = 50, message = "Quantity per unit cannot exceed 50 characters")
    @Column(name = "quantity_per_unit", nullable = false, length = 50)
    private String quantityPerUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Min(value = 0, message = "Discount cannot be negative")
    @Max(value = 100, message = "Discount cannot exceed 100%")
    @Column(name = "discount")
    private Integer discount = 0;

    // Constructors
    public Product() {
    }

    public Product(String productName, BigDecimal unitPrice, Integer unitInStock,
            String quantityPerUnit, Category category, String description,
            String imageUrl, Integer discount) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.unitInStock = unitInStock;
        this.quantityPerUnit = quantityPerUnit;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.discount = discount;
    }

    // Getters and Setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getUnitInStock() {
        return unitInStock;
    }

    public void setUnitInStock(Integer unitInStock) {
        this.unitInStock = unitInStock;
    }

    public String getQuantityPerUnit() {
        return quantityPerUnit;
    }

    public void setQuantityPerUnit(String quantityPerUnit) {
        this.quantityPerUnit = quantityPerUnit;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
