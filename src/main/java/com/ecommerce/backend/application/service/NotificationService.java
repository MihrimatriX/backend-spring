package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.CreateNotificationDto;
import com.ecommerce.backend.application.dto.NotificationDto;
import com.ecommerce.backend.application.dto.NotificationSummaryDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.Notification;
import com.ecommerce.backend.infrastructure.repository.NotificationRepository;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Kullanıcı bildirimleri — docs/API_CONTRACT.md §4.12. Sistem bildirimleri {@link UserNotifier} ile yazılır.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    private static final int SUMMARY_RECENT_COUNT = 5;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /** {@code pageNumber} 1 tabanlı, değerler çağıran tarafından kırpılmış olmalı. */
    public List<NotificationDto> getUserNotifications(Long userId, int pageNumber, int pageSize) {
        return notificationRepository
                .findByUserIdAndIsActiveTrue(userId, PageRequest.of(pageNumber - 1, pageSize, NEWEST_FIRST))
                .stream().map(NotificationService::toDto).toList();
    }

    public NotificationDto getNotification(Long notificationId, Long userId) {
        return toDto(findOwned(notificationId, userId));
    }

    public NotificationSummaryDto getSummary(Long userId) {
        List<NotificationDto> recent = notificationRepository
                .findByUserIdAndIsActiveTrue(userId, PageRequest.of(0, SUMMARY_RECENT_COUNT, NEWEST_FIRST))
                .stream().map(NotificationService::toDto).toList();
        return new NotificationSummaryDto(
                notificationRepository.countByUserIdAndIsActiveTrue(userId),
                notificationRepository.countByUserIdAndIsActiveTrueAndIsReadFalse(userId),
                recent);
    }

    @Transactional
    public NotificationDto create(CreateNotificationDto dto) {
        if (userRepository.findByIdAndIsActiveTrue(dto.getUserId()).isEmpty()) {
            throw ApiException.badRequest("USER_NOT_FOUND", "User not found");
        }
        Notification n = new Notification(dto.getUserId(), dto.getTitle().trim(), dto.getMessage().trim(),
                dto.getType().trim(), blankToNull(dto.getActionUrl()));
        n.setIsRead(false);
        n.setIsActive(true);
        LocalDateTime now = LocalDateTime.now();
        n.setCreatedAt(now);
        n.setUpdatedAt(now);
        return toDto(notificationRepository.save(n));
    }

    /** Okundu yapılırsa {@code readAt} = şimdi (zaten okunmuşsa korunur), okunmadı yapılırsa null. */
    @Transactional
    public NotificationDto setRead(Long notificationId, Long userId, boolean isRead) {
        Notification n = findOwned(notificationId, userId);
        LocalDateTime now = LocalDateTime.now();
        if (isRead) {
            if (!Boolean.TRUE.equals(n.getIsRead()) || n.getReadAt() == null) {
                n.setReadAt(now);
            }
        } else {
            n.setReadAt(null);
        }
        n.setIsRead(isRead);
        n.setUpdatedAt(now);
        return toDto(notificationRepository.save(n));
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Notification> unread = notificationRepository.findByUserIdAndIsActiveTrueAndIsReadFalse(userId);
        for (Notification n : unread) {
            n.setIsRead(true);
            n.setReadAt(now);
            n.setUpdatedAt(now);
        }
        notificationRepository.saveAll(unread);
    }

    /** Yumuşak silme ({@code isActive=false}). */
    @Transactional
    public void delete(Long notificationId, Long userId) {
        Notification n = findOwned(notificationId, userId);
        n.setIsActive(false);
        n.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(n);
    }

    private Notification findOwned(Long notificationId, Long userId) {
        return notificationRepository.findByIdAndUserIdAndIsActiveTrue(notificationId, userId)
                .orElseThrow(() -> ApiException.notFound("NOTIFICATION_NOT_FOUND", "Notification not found"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static NotificationDto toDto(Notification n) {
        return new NotificationDto(n.getId(), n.getUserId(), n.getTitle(), n.getMessage(), n.getType(),
                n.getActionUrl(), Boolean.TRUE.equals(n.getIsRead()), n.getReadAt(), n.getIsActive(),
                n.getCreatedAt(), n.getUpdatedAt());
    }
}
