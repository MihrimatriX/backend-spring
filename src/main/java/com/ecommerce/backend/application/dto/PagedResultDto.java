package com.ecommerce.backend.application.dto;

import java.util.List;

/**
 * Sayfalı sonuç (docs/API_CONTRACT.md §1.3). {@code pageNumber} 1 tabanlıdır.
 */
public record PagedResultDto<T>(
        List<T> items,
        long totalCount,
        int pageNumber,
        int pageSize,
        int totalPages,
        boolean hasPreviousPage,
        boolean hasNextPage) {

    public static <T> PagedResultDto<T> of(List<T> items, long totalCount, int pageNumber, int pageSize) {
        int totalPages = pageSize <= 0 ? 0 : (int) Math.ceil((double) totalCount / pageSize);
        return new PagedResultDto<>(items, totalCount, pageNumber, pageSize, totalPages,
                pageNumber > 1, pageNumber < totalPages);
    }
}
