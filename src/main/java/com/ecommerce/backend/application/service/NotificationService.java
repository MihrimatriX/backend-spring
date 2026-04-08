package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.domain.entity.*;
import com.ecommerce.backend.infrastructure.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public BaseResponseDto<List<NotificationDto>> getUserNotifications(Long userId, int pageNumber, int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
            Page<Notification> notificationPage = notificationRepository
                    .findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable);

            List<NotificationDto> notificationDtos = notificationPage.getContent().stream()
                    .map(this::convertToNotificationDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Notifications retrieved successfully", notificationDtos);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error retrieving notifications: " + ex.getMessage());
        }
    }

    public BaseResponseDto<NotificationDto> getNotificationById(Long notificationId, Long userId) {
        try {
            Notification notification = notificationRepository.findByIdAndUserIdAndIsActiveTrue(notificationId, userId)
                    .orElse(null);
            if (notification == null) {
                return BaseResponseDto.error("Notification not found");
            }

            NotificationDto notificationDto = convertToNotificationDto(notification);
            return BaseResponseDto.success("Notification retrieved successfully", notificationDto);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error retrieving notification: " + ex.getMessage());
        }
    }

    public BaseResponseDto<NotificationDto> createNotification(CreateNotificationDto createNotificationDto) {
        try {
            Notification notification = new Notification();
            notification.setUserId(createNotificationDto.getUserId());
            notification.setTitle(createNotificationDto.getTitle());
            notification.setMessage(createNotificationDto.getMessage());
            notification.setType(createNotificationDto.getType());
            notification.setActionUrl(createNotificationDto.getActionUrl());
            notification.setIsRead(false);
            notification.setIsActive(true);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUpdatedAt(LocalDateTime.now());

            Notification savedNotification = notificationRepository.save(notification);
            NotificationDto notificationDto = convertToNotificationDto(savedNotification);

            return BaseResponseDto.success("Notification created successfully", notificationDto);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error creating notification: " + ex.getMessage());
        }
    }

    public BaseResponseDto<NotificationDto> updateNotification(Long notificationId, Long userId,
            UpdateNotificationDto updateNotificationDto) {
        try {
            Notification notification = notificationRepository.findByIdAndUserIdAndIsActiveTrue(notificationId, userId)
                    .orElse(null);
            if (notification == null) {
                return BaseResponseDto.error("Notification not found");
            }

            notification.setIsRead(updateNotificationDto.getIsRead());
            if (updateNotificationDto.getIsRead() && !notification.getIsRead()) {
                notification.setReadAt(LocalDateTime.now());
            }
            notification.setUpdatedAt(LocalDateTime.now());

            Notification savedNotification = notificationRepository.save(notification);
            NotificationDto notificationDto = convertToNotificationDto(savedNotification);

            return BaseResponseDto.success("Notification updated successfully", notificationDto);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error updating notification: " + ex.getMessage());
        }
    }

    public BaseResponseDto<String> deleteNotification(Long notificationId, Long userId) {
        try {
            Notification notification = notificationRepository.findByIdAndUserIdAndIsActiveTrue(notificationId, userId)
                    .orElse(null);
            if (notification == null) {
                return BaseResponseDto.error("Notification not found");
            }

            notification.setIsActive(false);
            notification.setUpdatedAt(LocalDateTime.now());
            notificationRepository.save(notification);

            return BaseResponseDto.success("Notification deleted successfully", "Notification deleted successfully");
        } catch (Exception ex) {
            return BaseResponseDto.error("Error deleting notification: " + ex.getMessage());
        }
    }

    public BaseResponseDto<String> markAllAsRead(Long userId) {
        try {
            List<Notification> unreadNotifications = notificationRepository
                    .findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, Pageable.unpaged())
                    .getContent().stream()
                    .filter(n -> !n.getIsRead())
                    .collect(Collectors.toList());

            for (Notification notification : unreadNotifications) {
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
                notification.setUpdatedAt(LocalDateTime.now());
            }

            notificationRepository.saveAll(unreadNotifications);

            return BaseResponseDto.success("All notifications marked as read", "All notifications marked as read");
        } catch (Exception ex) {
            return BaseResponseDto.error("Error marking notifications as read: " + ex.getMessage());
        }
    }

    public BaseResponseDto<NotificationSummaryDto> getNotificationSummary(Long userId) {
        try {
            Long totalNotifications = notificationRepository.countByUserIdAndIsActiveTrue(userId);
            Long unreadNotifications = notificationRepository.countByUserIdAndIsActiveTrueAndIsReadFalse(userId);

            List<Notification> recentNotifications = notificationRepository
                    .findTop5ByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
            List<NotificationDto> recentNotificationDtos = recentNotifications.stream()
                    .map(this::convertToNotificationDto)
                    .collect(Collectors.toList());

            NotificationSummaryDto summary = new NotificationSummaryDto(
                    totalNotifications,
                    unreadNotifications,
                    recentNotificationDtos);

            return BaseResponseDto.success("Notification summary retrieved successfully", summary);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error retrieving notification summary: " + ex.getMessage());
        }
    }

    private NotificationDto convertToNotificationDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getActionUrl(),
                notification.getIsRead(),
                notification.getReadAt(),
                notification.getIsActive(),
                notification.getCreatedAt(),
                notification.getUpdatedAt());
    }
}
