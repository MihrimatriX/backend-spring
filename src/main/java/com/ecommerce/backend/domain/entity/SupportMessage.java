package com.ecommerce.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Destek talebi mesajı — docs/API_CONTRACT.md §3 SupportMessage. */
@Entity
@Table(name = "support_messages")
@Getter
@Setter
@NoArgsConstructor
public class SupportMessage extends BaseEntity {

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "is_from_support", nullable = false)
    private Boolean isFromSupport = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", insertable = false, updatable = false)
    private SupportTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
