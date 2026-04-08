package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.domain.entity.*;
import com.ecommerce.backend.infrastructure.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    public BaseResponseDto<List<PaymentMethodDto>> getUserPaymentMethods(Long userId) {
        try {
            List<PaymentMethod> paymentMethods = paymentMethodRepository
                    .findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(userId);
            List<PaymentMethodDto> paymentMethodDtos = paymentMethods.stream()
                    .map(this::convertToPaymentMethodDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Payment methods retrieved successfully", paymentMethodDtos);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error retrieving payment methods: " + ex.getMessage());
        }
    }

    public BaseResponseDto<PaymentMethodDto> getPaymentMethodById(Long paymentMethodId, Long userId) {
        try {
            PaymentMethod paymentMethod = paymentMethodRepository
                    .findByIdAndUserIdAndIsActiveTrue(paymentMethodId, userId).orElse(null);
            if (paymentMethod == null) {
                return BaseResponseDto.error("Payment method not found");
            }

            PaymentMethodDto paymentMethodDto = convertToPaymentMethodDto(paymentMethod);
            return BaseResponseDto.success("Payment method retrieved successfully", paymentMethodDto);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error retrieving payment method: " + ex.getMessage());
        }
    }

    public BaseResponseDto<PaymentMethodDto> createPaymentMethod(Long userId,
            CreatePaymentMethodDto createPaymentMethodDto) {
        try {
            // If this is set as default, remove default from other payment methods
            if (createPaymentMethodDto.getIsDefault()) {
                List<PaymentMethod> existingDefaultPaymentMethods = paymentMethodRepository
                        .findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId);
                for (PaymentMethod existingPaymentMethod : existingDefaultPaymentMethods) {
                    existingPaymentMethod.setIsDefault(false);
                    existingPaymentMethod.setUpdatedAt(LocalDateTime.now());
                }
            }

            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setUserId(userId);
            paymentMethod.setType(createPaymentMethodDto.getType());
            paymentMethod.setCardHolderName(createPaymentMethodDto.getCardHolderName());
            paymentMethod.setCardNumber(encryptCardNumber(createPaymentMethodDto.getCardNumber()));
            paymentMethod.setExpiryMonth(createPaymentMethodDto.getExpiryMonth());
            paymentMethod.setExpiryYear(createPaymentMethodDto.getExpiryYear());
            paymentMethod.setCvv(
                    createPaymentMethodDto.getCvv() != null ? encryptCvv(createPaymentMethodDto.getCvv()) : null);
            paymentMethod.setBankName(createPaymentMethodDto.getBankName());
            paymentMethod.setAccountNumber(createPaymentMethodDto.getAccountNumber() != null
                    ? encryptAccountNumber(createPaymentMethodDto.getAccountNumber())
                    : null);
            paymentMethod.setAccountHolderName(createPaymentMethodDto.getAccountHolderName());
            paymentMethod.setIsDefault(createPaymentMethodDto.getIsDefault());
            paymentMethod.setIsActive(true);
            paymentMethod.setCreatedAt(LocalDateTime.now());
            paymentMethod.setUpdatedAt(LocalDateTime.now());

            PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
            PaymentMethodDto paymentMethodDto = convertToPaymentMethodDto(savedPaymentMethod);

            return BaseResponseDto.success("Payment method created successfully", paymentMethodDto);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error creating payment method: " + ex.getMessage());
        }
    }

    public BaseResponseDto<PaymentMethodDto> updatePaymentMethod(Long paymentMethodId, Long userId,
            UpdatePaymentMethodDto updatePaymentMethodDto) {
        try {
            PaymentMethod paymentMethod = paymentMethodRepository
                    .findByIdAndUserIdAndIsActiveTrue(paymentMethodId, userId).orElse(null);
            if (paymentMethod == null) {
                return BaseResponseDto.error("Payment method not found");
            }

            // If this is set as default, remove default from other payment methods
            if (updatePaymentMethodDto.getIsDefault() && !paymentMethod.getIsDefault()) {
                List<PaymentMethod> existingDefaultPaymentMethods = paymentMethodRepository
                        .findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId);
                for (PaymentMethod existingPaymentMethod : existingDefaultPaymentMethods) {
                    if (!existingPaymentMethod.getId().equals(paymentMethodId)) {
                        existingPaymentMethod.setIsDefault(false);
                        existingPaymentMethod.setUpdatedAt(LocalDateTime.now());
                    }
                }
            }

            paymentMethod.setType(updatePaymentMethodDto.getType());
            paymentMethod.setCardHolderName(updatePaymentMethodDto.getCardHolderName());
            paymentMethod.setCardNumber(encryptCardNumber(updatePaymentMethodDto.getCardNumber()));
            paymentMethod.setExpiryMonth(updatePaymentMethodDto.getExpiryMonth());
            paymentMethod.setExpiryYear(updatePaymentMethodDto.getExpiryYear());
            paymentMethod.setCvv(
                    updatePaymentMethodDto.getCvv() != null ? encryptCvv(updatePaymentMethodDto.getCvv()) : null);
            paymentMethod.setBankName(updatePaymentMethodDto.getBankName());
            paymentMethod.setAccountNumber(updatePaymentMethodDto.getAccountNumber() != null
                    ? encryptAccountNumber(updatePaymentMethodDto.getAccountNumber())
                    : null);
            paymentMethod.setAccountHolderName(updatePaymentMethodDto.getAccountHolderName());
            paymentMethod.setIsDefault(updatePaymentMethodDto.getIsDefault());
            paymentMethod.setUpdatedAt(LocalDateTime.now());

            PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
            PaymentMethodDto paymentMethodDto = convertToPaymentMethodDto(savedPaymentMethod);

            return BaseResponseDto.success("Payment method updated successfully", paymentMethodDto);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error updating payment method: " + ex.getMessage());
        }
    }

    public BaseResponseDto<String> deletePaymentMethod(Long paymentMethodId, Long userId) {
        try {
            PaymentMethod paymentMethod = paymentMethodRepository
                    .findByIdAndUserIdAndIsActiveTrue(paymentMethodId, userId).orElse(null);
            if (paymentMethod == null) {
                return BaseResponseDto.error("Payment method not found");
            }

            paymentMethod.setIsActive(false);
            paymentMethod.setUpdatedAt(LocalDateTime.now());
            paymentMethodRepository.save(paymentMethod);

            return BaseResponseDto.success("Payment method deleted successfully",
                    "Payment method deleted successfully");
        } catch (Exception ex) {
            return BaseResponseDto.error("Error deleting payment method: " + ex.getMessage());
        }
    }

    public BaseResponseDto<PaymentMethodDto> setDefaultPaymentMethod(Long paymentMethodId, Long userId) {
        try {
            PaymentMethod paymentMethod = paymentMethodRepository
                    .findByIdAndUserIdAndIsActiveTrue(paymentMethodId, userId).orElse(null);
            if (paymentMethod == null) {
                return BaseResponseDto.error("Payment method not found");
            }

            // Remove default from other payment methods
            List<PaymentMethod> existingDefaultPaymentMethods = paymentMethodRepository
                    .findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId);
            for (PaymentMethod existingPaymentMethod : existingDefaultPaymentMethods) {
                if (!existingPaymentMethod.getId().equals(paymentMethodId)) {
                    existingPaymentMethod.setIsDefault(false);
                    existingPaymentMethod.setUpdatedAt(LocalDateTime.now());
                }
            }

            paymentMethod.setIsDefault(true);
            paymentMethod.setUpdatedAt(LocalDateTime.now());
            PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
            PaymentMethodDto paymentMethodDto = convertToPaymentMethodDto(savedPaymentMethod);

            return BaseResponseDto.success("Default payment method set successfully", paymentMethodDto);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error setting default payment method: " + ex.getMessage());
        }
    }

    private PaymentMethodDto convertToPaymentMethodDto(PaymentMethod paymentMethod) {
        return new PaymentMethodDto(
                paymentMethod.getId(),
                paymentMethod.getUserId(),
                paymentMethod.getType(),
                paymentMethod.getCardHolderName(),
                maskCardNumber(paymentMethod.getCardNumber()),
                paymentMethod.getExpiryMonth(),
                paymentMethod.getExpiryYear(),
                paymentMethod.getBankName(),
                paymentMethod.getAccountNumber() != null ? maskAccountNumber(paymentMethod.getAccountNumber()) : null,
                paymentMethod.getAccountHolderName(),
                paymentMethod.getIsDefault(),
                paymentMethod.getIsActive(),
                paymentMethod.getCreatedAt(),
                paymentMethod.getUpdatedAt());
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    private String encryptCardNumber(String cardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((cardNumber + "salt").getBytes());
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return cardNumber; // Fallback to original if encryption fails
        }
    }

    private String encryptCvv(String cvv) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((cvv + "salt").getBytes());
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return cvv; // Fallback to original if encryption fails
        }
    }

    private String encryptAccountNumber(String accountNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((accountNumber + "salt").getBytes());
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return accountNumber; // Fallback to original if encryption fails
        }
    }
}
