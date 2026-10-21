package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/** docs/API_CONTRACT.md §3 — HelpArticle */
public record HelpArticleDto(
        Long id,
        String title,
        String content,
        String category,
        List<String> tags,
        Integer viewCount,
        Boolean isPublished,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
