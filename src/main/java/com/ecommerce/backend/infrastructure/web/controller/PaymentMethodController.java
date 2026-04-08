package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.application.service.PaymentMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-method")
@Tag(name = "Payment Method Management", description = "APIs for managing user payment methods")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private com.ecommerce.backend.infrastructure.security.CurrentUserService currentUserService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user payment methods", description = "Retrieve all payment methods for a specific user")
    public ResponseEntity<BaseResponseDto<List<PaymentMethodDto>>> getUserPaymentMethods(@PathVariable Long userId) {
        try {
            Long currentUserId = getCurrentUserId();
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(BaseResponseDto.error("You can only access your own payment methods"));
            }

            BaseResponseDto<List<PaymentMethodDto>> result = paymentMethodService.getUserPaymentMethods(userId);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error retrieving user payment methods: " + ex.getMessage()));
        }
    }

    @GetMapping("/{paymentMethodId}")
    @Operation(summary = "Get payment method by ID", description = "Retrieve a specific payment method by ID")
    public ResponseEntity<BaseResponseDto<PaymentMethodDto>> getPaymentMethod(@PathVariable Long paymentMethodId) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<PaymentMethodDto> result = paymentMethodService.getPaymentMethodById(paymentMethodId,
                    currentUserId);

            if (result.isSuccess() && result.getData() != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error retrieving payment method: " + ex.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Create payment method", description = "Create a new payment method")
    public ResponseEntity<BaseResponseDto<PaymentMethodDto>> createPaymentMethod(
            @Valid @RequestBody CreatePaymentMethodDto createPaymentMethodDto) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<PaymentMethodDto> result = paymentMethodService.createPaymentMethod(currentUserId,
                    createPaymentMethodDto);

            if (result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error creating payment method: " + ex.getMessage()));
        }
    }

    @PutMapping("/{paymentMethodId}")
    @Operation(summary = "Update payment method", description = "Update an existing payment method")
    public ResponseEntity<BaseResponseDto<PaymentMethodDto>> updatePaymentMethod(
            @PathVariable Long paymentMethodId,
            @Valid @RequestBody UpdatePaymentMethodDto updatePaymentMethodDto) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<PaymentMethodDto> result = paymentMethodService.updatePaymentMethod(paymentMethodId,
                    currentUserId, updatePaymentMethodDto);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error updating payment method: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/{paymentMethodId}")
    @Operation(summary = "Delete payment method", description = "Delete a payment method")
    public ResponseEntity<BaseResponseDto<String>> deletePaymentMethod(@PathVariable Long paymentMethodId) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<String> result = paymentMethodService.deletePaymentMethod(paymentMethodId, currentUserId);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error deleting payment method: " + ex.getMessage()));
        }
    }

    @PutMapping("/{paymentMethodId}/default")
    @Operation(summary = "Set default payment method", description = "Set a payment method as default")
    public ResponseEntity<BaseResponseDto<PaymentMethodDto>> setDefaultPaymentMethod(
            @PathVariable Long paymentMethodId) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<PaymentMethodDto> result = paymentMethodService.setDefaultPaymentMethod(paymentMethodId,
                    currentUserId);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error setting default payment method: " + ex.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        return currentUserService.requireUserId();
    }
}
