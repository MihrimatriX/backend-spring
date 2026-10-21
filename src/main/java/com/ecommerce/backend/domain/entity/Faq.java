package com.ecommerce.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Sık sorulan soru — docs/API_CONTRACT.md §3 Faq. */
@Entity
@Table(name = "faqs")
@Getter
@Setter
@NoArgsConstructor
public class Faq extends BaseEntity {

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;
}
