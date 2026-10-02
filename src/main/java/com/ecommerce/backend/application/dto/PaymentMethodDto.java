package com.ecommerce.backend.application.dto;

import com.ecommerce.backend.application.support.PaymentCards;
import com.ecommerce.backend.domain.entity.PaymentMethod;

import java.time.LocalDateTime;

/**
 * Ödeme yöntemi (docs/API_CONTRACT.md §3). {@code cardNumber} her zaman
 * {@code **** **** **** 1111}, {@code accountNumber} {@code ****1234} biçimindedir.
 */
public record PaymentMethodDto(
        Long id,
        Long userId,
        String type,
        String cardHolderName,
        String cardNumber,
        Integer expiryMonth,
        Integer expiryYear,
        String bankName,
        String accountNumber,
        String accountHolderName,
        Boolean isDefault,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static PaymentMethodDto from(PaymentMethod pm) {
        return new PaymentMethodDto(pm.getId(), pm.getUserId(), pm.getType(), pm.getCardHolderName(),
                PaymentCards.displayCardNumber(pm.getCardNumber()), pm.getExpiryMonth(), pm.getExpiryYear(),
                pm.getBankName(), PaymentCards.maskAccountNumber(pm.getAccountNumber()), pm.getAccountHolderName(),
                Boolean.TRUE.equals(pm.getIsDefault()), pm.getIsActive(), pm.getCreatedAt(), pm.getUpdatedAt());
    }
}
