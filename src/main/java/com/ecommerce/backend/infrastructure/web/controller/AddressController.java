package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.AddressDto;
import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CreateAddressDto;
import com.ecommerce.backend.application.dto.UpdateAddressDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.application.service.AddressService;
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

/** Adresler — docs/API_CONTRACT.md §4.8 (kullanıcı yalnızca kendi adreslerine erişir). */
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@Tag(name = "Address Management", description = "APIs for managing user addresses")
public class AddressController {

    private final AddressService addressService;
    private final CurrentUserService currentUserService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user addresses", description = "Default first, then newest")
    public ResponseEntity<BaseResponseDto<List<AddressDto>>> getUserAddresses(@PathVariable Long userId) {
        if (!currentUserService.requireUserId().equals(userId)) {
            throw ApiException.forbidden("You can only access your own addresses");
        }
        return ApiResponses.ok("Addresses retrieved successfully", addressService.getUserAddresses(userId));
    }

    @GetMapping("/{addressId}")
    @Operation(summary = "Get address by ID")
    public ResponseEntity<BaseResponseDto<AddressDto>> getAddress(@PathVariable Long addressId) {
        return ApiResponses.ok("Address retrieved successfully",
                addressService.getAddress(addressId, currentUserService.requireUserId()));
    }

    @PostMapping
    @Operation(summary = "Create address")
    public ResponseEntity<BaseResponseDto<AddressDto>> createAddress(@Valid @RequestBody CreateAddressDto dto) {
        return ApiResponses.created("Address created successfully",
                addressService.createAddress(currentUserService.requireUserId(), dto));
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "Update address")
    public ResponseEntity<BaseResponseDto<AddressDto>> updateAddress(@PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressDto dto) {
        return ApiResponses.ok("Address updated successfully",
                addressService.updateAddress(addressId, currentUserService.requireUserId(), dto));
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Delete address (soft)")
    public ResponseEntity<BaseResponseDto<String>> deleteAddress(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId, currentUserService.requireUserId());
        return ApiResponses.ok("Address deleted successfully", "Address deleted successfully");
    }

    @PutMapping("/{addressId}/default")
    @Operation(summary = "Set default address")
    public ResponseEntity<BaseResponseDto<AddressDto>> setDefaultAddress(@PathVariable Long addressId) {
        return ApiResponses.ok("Default address set successfully",
                addressService.setDefaultAddress(addressId, currentUserService.requireUserId()));
    }
}
