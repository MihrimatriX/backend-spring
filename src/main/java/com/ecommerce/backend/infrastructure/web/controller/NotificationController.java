package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CreateNotificationDto;
import com.ecommerce.backend.application.dto.NotificationDto;
import com.ecommerce.backend.application.dto.NotificationSummaryDto;
import com.ecommerce.backend.application.dto.UpdateNotificationDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.application.service.NotificationService;
import com.ecommerce.backend.infrastructure.security.CurrentUserService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * docs/API_CONTRACT.md §4.12 — kullanıcı her zaman yalnızca kendi bildirimlerine erişir.
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Kullanıcı bildirimleri")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Kullanıcının bildirimleri (yalnızca sahibi)")
    public ResponseEntity<BaseResponseDto<List<NotificationDto>>> getUserNotifications(@PathVariable Long userId,
            @RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize) {
        if (!currentUserService.requireUserId().equals(userId)) {
            throw ApiException.forbidden("You can only access your own notifications");
        }
        return ApiResponses.ok("Notifications retrieved successfully", notificationService
                .getUserNotifications(userId, ApiResponses.page(pageNumber), ApiResponses.size(pageSize, 10)));
    }

    @GetMapping("/summary")
    @Operation(summary = "Toplam/okunmamış sayısı ve en yeni 5 bildirim")
    public ResponseEntity<BaseResponseDto<NotificationSummaryDto>> getSummary() {
        return ApiResponses.ok("Notification summary retrieved successfully",
                notificationService.getSummary(currentUserService.requireUserId()));
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Bildirim ayrıntısı; yoksa 404 NOTIFICATION_NOT_FOUND")
    public ResponseEntity<BaseResponseDto<NotificationDto>> getNotification(@PathVariable Long notificationId) {
        return ApiResponses.ok("Notification retrieved successfully",
                notificationService.getNotification(notificationId, currentUserService.requireUserId()));
    }

    @PutMapping("/mark-all-read")
    @Operation(summary = "Tüm bildirimleri okundu yap")
    public ResponseEntity<BaseResponseDto<String>> markAllAsRead() {
        notificationService.markAllAsRead(currentUserService.requireUserId());
        return ApiResponses.ok("All notifications marked as read", "All notifications marked as read");
    }

    @PutMapping("/{notificationId}")
    @Operation(summary = "Okundu / okunmadı yap")
    public ResponseEntity<BaseResponseDto<NotificationDto>> updateNotification(@PathVariable Long notificationId,
            @Valid @RequestBody UpdateNotificationDto request) {
        boolean isRead = Boolean.TRUE.equals(request.getIsRead());
        return ApiResponses.ok("Notification updated successfully",
                notificationService.setRead(notificationId, currentUserService.requireUserId(), isRead));
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Bildirimi sil (yumuşak)")
    public ResponseEntity<BaseResponseDto<String>> deleteNotification(@PathVariable Long notificationId) {
        notificationService.delete(notificationId, currentUserService.requireUserId());
        return ApiResponses.ok("Notification deleted successfully", "Notification deleted successfully");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Yönetici: kullanıcıya bildirim gönder (201); kullanıcı yoksa 400 USER_NOT_FOUND")
    public ResponseEntity<BaseResponseDto<NotificationDto>> createNotification(
            @Valid @RequestBody CreateNotificationDto request) {
        return ApiResponses.created("Notification created successfully", notificationService.create(request));
    }

    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Yönetici: bir kullanıcının bildirimleri")
    public ResponseEntity<BaseResponseDto<List<NotificationDto>>> adminGetUserNotifications(@PathVariable Long userId,
            @RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize) {
        return ApiResponses.ok("Notifications retrieved successfully", notificationService
                .getUserNotifications(userId, ApiResponses.page(pageNumber), ApiResponses.size(pageSize, 30)));
    }
}
