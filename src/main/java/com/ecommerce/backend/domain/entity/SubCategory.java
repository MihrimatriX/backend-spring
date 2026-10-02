package com.ecommerce.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Alt kategori (docs/API_CONTRACT.md §4.4). Ad, kategori içinde benzersizdir.
 */
@Entity
@Table(name = "subcategories", uniqueConstraints = @UniqueConstraint(name = "uk_subcategories_category_name",
        columnNames = { "category_id", "sub_category_name" }))
public class SubCategory extends BaseEntity {

    @NotBlank(message = "SubCategory name is required")
    @Size(max = 100, message = "SubCategory name cannot exceed 100 characters")
    @Column(name = "sub_category_name", nullable = false, length = 100)
    private String subCategoryName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public SubCategory() {
    }

    public SubCategory(String subCategoryName, String description, Category category) {
        this.subCategoryName = subCategoryName;
        this.description = description;
        this.category = category;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
