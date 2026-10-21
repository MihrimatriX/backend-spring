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

/** Destek talebi — docs/API_CONTRACT.md §3 SupportTicket. */
@Entity
@Table(name = "support_tickets")
@Getter
@Setter
@NoArgsConstructor
public class SupportTicket extends BaseEntity {

    public static final String STATUS_OPEN = "Open";
    public static final String DEFAULT_PRIORITY = "Medium";

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority = DEFAULT_PRIORITY;

    @Column(name = "status", nullable = false, length = 20)
    private String status = STATUS_OPEN;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
