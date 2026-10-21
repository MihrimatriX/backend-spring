package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.CreatePaymentMethodDto;
import com.ecommerce.backend.application.dto.PaymentMethodDto;
import com.ecommerce.backend.application.dto.UpdatePaymentMethodDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.application.support.PaymentCards;
import com.ecommerce.backend.domain.entity.PaymentMethod;
import com.ecommerce.backend.infrastructure.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Ödeme yöntemleri — docs/API_CONTRACT.md §4.9. Tam kart numarası ve CVV saklanmaz; yalnızca
 * maskeli numara ({@code **** **** **** 1111}) ve maskeli hesap numarası ({@code ****1234}) tutulur.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    /** Varsayılan önce, sonra en yeni. */
    @Transactional(readOnly = true)
    public List<PaymentMethodDto> getUserPaymentMethods(Long userId) {
        return paymentMethodRepository.findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDescIdDesc(userId)
                .stream()
                .map(PaymentMethodDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentMethodDto getPaymentMethod(Long paymentMethodId, Long userId) {
        return PaymentMethodDto.from(require(paymentMethodId, userId));
    }

    public PaymentMethodDto createPaymentMethod(Long userId, CreatePaymentMethodDto dto) {
        String maskedNumber = maskValidCardNumber(dto.getCardNumber());
        ensureNotExpired(dto.getExpiryMonth(), dto.getExpiryYear());
        boolean makeDefault = Boolean.TRUE.equals(dto.getIsDefault());
        if (makeDefault) {
            clearDefault(userId, null);
        }

        PaymentMethod pm = new PaymentMethod();
        pm.setUserId(userId);
        pm.setType(dto.getType());
        pm.setCardHolderName(dto.getCardHolderName());
        pm.setCardNumber(maskedNumber);
        pm.setExpiryMonth(dto.getExpiryMonth());
        pm.setExpiryYear(dto.getExpiryYear());
        pm.setBankName(dto.getBankName());
        pm.setAccountNumber(PaymentCards.maskAccountNumber(dto.getAccountNumber()));
        pm.setAccountHolderName(dto.getAccountHolderName());
        pm.setIsDefault(makeDefault);
        pm.setIsActive(true);
        return PaymentMethodDto.from(paymentMethodRepository.saveAndFlush(pm));
    }

    /** Maskeli ({@code *} içeren) kart/hesap numarası gelirse mevcut değer korunur. */
    public PaymentMethodDto updatePaymentMethod(Long paymentMethodId, Long userId, UpdatePaymentMethodDto dto) {
        PaymentMethod pm = require(paymentMethodId, userId);
        if (!PaymentCards.isMasked(dto.getCardNumber())) {
            pm.setCardNumber(maskValidCardNumber(dto.getCardNumber()));
        }
        ensureNotExpired(dto.getExpiryMonth(), dto.getExpiryYear());
        if (dto.getIsDefault() != null) {
            if (dto.getIsDefault()) {
                clearDefault(userId, paymentMethodId);
            }
            pm.setIsDefault(dto.getIsDefault());
        }
        if (!PaymentCards.isMasked(dto.getAccountNumber())) {
            pm.setAccountNumber(PaymentCards.maskAccountNumber(dto.getAccountNumber()));
        }
        pm.setType(dto.getType());
        pm.setCardHolderName(dto.getCardHolderName());
        pm.setExpiryMonth(dto.getExpiryMonth());
        pm.setExpiryYear(dto.getExpiryYear());
        pm.setBankName(dto.getBankName());
        pm.setAccountHolderName(dto.getAccountHolderName());
        pm.setUpdatedAt(LocalDateTime.now());
        return PaymentMethodDto.from(paymentMethodRepository.saveAndFlush(pm));
    }

    /** Yumuşak silme. */
    public void deletePaymentMethod(Long paymentMethodId, Long userId) {
        PaymentMethod pm = require(paymentMethodId, userId);
        pm.setIsActive(false);
        pm.setIsDefault(false);
        pm.setUpdatedAt(LocalDateTime.now());
    }

    public PaymentMethodDto setDefaultPaymentMethod(Long paymentMethodId, Long userId) {
        PaymentMethod pm = require(paymentMethodId, userId);
        clearDefault(userId, paymentMethodId);
        pm.setIsDefault(true);
        pm.setUpdatedAt(LocalDateTime.now());
        return PaymentMethodDto.from(paymentMethodRepository.saveAndFlush(pm));
    }

    private PaymentMethod require(Long paymentMethodId, Long userId) {
        return paymentMethodRepository.findByIdAndUserIdAndIsActiveTrue(paymentMethodId, userId)
                .orElseThrow(() -> ApiException.notFound("PAYMENT_METHOD_NOT_FOUND", "Payment method not found"));
    }

    private static String maskValidCardNumber(String raw) {
        String digits = PaymentCards.normalizeCardNumber(raw);
        if (digits == null) {
            throw ApiException.badRequest("INVALID_CARD_NUMBER", "Geçersiz kart numarası.");
        }
        return PaymentCards.maskCardNumber(digits);
    }

    private static void ensureNotExpired(Integer month, Integer year) {
        if (PaymentCards.isExpired(month, year, LocalDate.now(ZoneOffset.UTC))) {
            throw ApiException.badRequest("CARD_EXPIRED", "Kartın son kullanma tarihi geçmiş.");
        }
    }

    private void clearDefault(Long userId, Long exceptId) {
        LocalDateTime now = LocalDateTime.now();
        for (PaymentMethod other : paymentMethodRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId)) {
            if (!other.getId().equals(exceptId)) {
                other.setIsDefault(false);
                other.setUpdatedAt(now);
            }
        }
    }
}
