package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.application.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@Tag(name = "Notification Management", description = "APIs for managing user notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private com.ecommerce.backend.infrastructure.security.CurrentUserService currentUserService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user notifications", description = "Retrieve all notifications for a specific user")
    public ResponseEntity<BaseResponseDto<List<NotificationDto>>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Long currentUserId = getCurrentUserId();
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(BaseResponseDto.error("You can only access your own notifications"));
            }

            BaseResponseDto<List<NotificationDto>> result = notificationService.getUserNotifications(userId, pageNumber,
                    pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error retrieving user notifications: " + ex.getMessage()));
        }
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get notification by ID", description = "Retrieve a specific notification by ID")
    public ResponseEntity<BaseResponseDto<NotificationDto>> getNotification(@PathVariable Long notificationId) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<NotificationDto> result = notificationService.getNotificationById(notificationId,
                    currentUserId);

            if (result.isSuccess() && result.getData() != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error retrieving notification: " + ex.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Create notification", description = "Create a new notification")
    public ResponseEntity<BaseResponseDto<NotificationDto>> createNotification(
            @Valid @RequestBody CreateNotificationDto createNotificationDto) {
        try {
            BaseResponseDto<NotificationDto> result = notificationService.createNotification(createNotificationDto);

            if (result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error creating notification: " + ex.getMessage()));
        }
    }

    @PutMapping("/{notificationId}")
    @Operation(summary = "Update notification", description = "Update an existing notification")
    public ResponseEntity<BaseResponseDto<NotificationDto>> updateNotification(
            @PathVariable Long notificationId,
            @Valid @RequestBody UpdateNotificationDto updateNotificationDto) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<NotificationDto> result = notificationService.updateNotification(notificationId,
                    currentUserId, updateNotificationDto);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error updating notification: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification", description = "Delete a notification")
    public ResponseEntity<BaseResponseDto<String>> deleteNotification(@PathVariable Long notificationId) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<String> result = notificationService.deleteNotification(notificationId, currentUserId);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error deleting notification: " + ex.getMessage()));
        }
    }

    @PutMapping("/mark-all-read")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read")
    public ResponseEntity<BaseResponseDto<String>> markAllAsRead() {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<String> result = notificationService.markAllAsRead(currentUserId);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error marking notifications as read: " + ex.getMessage()));
        }
    }

    @GetMapping("/summary")
    @Operation(summary = "Get notification summary", description = "Get notification summary for user")
    public ResponseEntity<BaseResponseDto<NotificationSummaryDto>> getNotificationSummary() {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<NotificationSummaryDto> result = notificationService.getNotificationSummary(currentUserId);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error retrieving notification summary: " + ex.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            String userId = authentication.getName();
            return Long.parseLong(userId);
        }
        throw new RuntimeException("Invalid user ID");
    }
}
