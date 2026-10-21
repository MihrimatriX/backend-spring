package com.ecommerce.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Yardım makalesi — docs/API_CONTRACT.md §3 HelpArticle. Etiketler virgülle ayrılmış saklanır. */
@Entity
@Table(name = "help_articles")
@Getter
@Setter
@NoArgsConstructor
public class HelpArticle extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /** Virgülle ayrılmış etiketler. */
    @Column(name = "tags", nullable = false, columnDefinition = "TEXT")
    private String tags = "";

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;
}
