package com.ecommerce.backend.application.service;

import com.ecommerce.backend.domain.entity.Notification;
import com.ecommerce.backend.infrastructure.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Sistem bildirimleri (sipariş alındı, durum değişti …) — docs/API_CONTRACT.md §4.12.
 * Çağıranın transaction'ına katılır; sipariş ile bildirim birlikte kaydedilir.
 */
@Service
@RequiredArgsConstructor
public class UserNotifier {

    public static final String TYPE_ORDER = "Order";

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notify(Long userId, String title, String message, String type, String actionUrl) {
        Notification n = new Notification(userId, title, message, type, actionUrl);
        n.setIsRead(false);
        n.setIsActive(true);
        LocalDateTime now = LocalDateTime.now();
        n.setCreatedAt(now);
        n.setUpdatedAt(now);
        notificationRepository.save(n);
    }
}
