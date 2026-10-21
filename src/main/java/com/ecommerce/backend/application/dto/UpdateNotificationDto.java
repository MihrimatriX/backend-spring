package com.ecommerce.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** {@code PUT /api/notification/{id}} — gönderilmezse {@code false} (okunmadı) kabul edilir (.NET ile aynı). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationDto {
    private Boolean isRead;
}
