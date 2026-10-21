package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CreatePaymentMethodDto;
import com.ecommerce.backend.application.dto.PaymentMethodDto;
import com.ecommerce.backend.application.dto.UpdatePaymentMethodDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.application.service.PaymentMethodService;
import com.ecommerce.backend.infrastructure.security.CurrentUserService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Ödeme yöntemleri — docs/API_CONTRACT.md §4.9 (kullanıcı yalnızca kendi kayıtlarına erişir). */
@RestController
@RequestMapping("/api/paymentmethod")
@RequiredArgsConstructor
@Tag(name = "Payment Method Management", description = "APIs for managing user payment methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;
    private final CurrentUserService currentUserService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user payment methods", description = "Default first, then newest")
    public ResponseEntity<BaseResponseDto<List<PaymentMethodDto>>> getUserPaymentMethods(@PathVariable Long userId) {
        if (!currentUserService.requireUserId().equals(userId)) {
            throw ApiException.forbidden("You can only access your own payment methods");
        }
        return ApiResponses.ok("Payment methods retrieved successfully",
                paymentMethodService.getUserPaymentMethods(userId));
    }

    @GetMapping("/{paymentMethodId}")
    @Operation(summary = "Get payment method by ID")
    public ResponseEntity<BaseResponseDto<PaymentMethodDto>> getPaymentMethod(@PathVariable Long paymentMethodId) {
        return ApiResponses.ok("Payment method retrieved successfully",
                paymentMethodService.getPaymentMethod(paymentMethodId, currentUserService.requireUserId()));
    }

    @PostMapping
    @Operation(summary = "Create payment method")
    public ResponseEntity<BaseResponseDto<PaymentMethodDto>> createPaymentMethod(
            @Valid @RequestBody CreatePaymentMethodDto dto) {
        return ApiResponses.created("Payment method created successfully",
                paymentMethodService.createPaymentMethod(currentUserService.requireUserId(), dto));
    }

    @PutMapping("/{paymentMethodId}")
    @Operation(summary = "Update payment method")
    public ResponseEntity<BaseResponseDto<PaymentMethodDto>> updatePaymentMethod(@PathVariable Long paymentMethodId,
            @Valid @RequestBody UpdatePaymentMethodDto dto) {
        return ApiResponses.ok("Payment method updated successfully",
                paymentMethodService.updatePaymentMethod(paymentMethodId, currentUserService.requireUserId(), dto));
    }

    @DeleteMapping("/{paymentMethodId}")
    @Operation(summary = "Delete payment method (soft)")
    public ResponseEntity<BaseResponseDto<String>> deletePaymentMethod(@PathVariable Long paymentMethodId) {
        paymentMethodService.deletePaymentMethod(paymentMethodId, currentUserService.requireUserId());
        return ApiResponses.ok("Payment method deleted successfully", "Payment method deleted successfully");
    }

    @PutMapping("/{paymentMethodId}/default")
    @Operation(summary = "Set default payment method")
    public ResponseEntity<BaseResponseDto<PaymentMethodDto>> setDefaultPaymentMethod(
            @PathVariable Long paymentMethodId) {
        return ApiResponses.ok("Default payment method set successfully",
                paymentMethodService.setDefaultPaymentMethod(paymentMethodId, currentUserService.requireUserId()));
    }
}
