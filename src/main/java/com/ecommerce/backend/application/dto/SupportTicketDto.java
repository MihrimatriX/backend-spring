package com.ecommerce.backend.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/** docs/API_CONTRACT.md §3 — SupportTicket */
public record SupportTicketDto(
        Long id,
        Long userId,
        String userName,
        String subject,
        String description,
        String category,
        String priority,
        String status,
        String assignedTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SupportMessageDto> messages) {
}
