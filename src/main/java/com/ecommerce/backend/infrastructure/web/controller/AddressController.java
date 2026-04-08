package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.application.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@Tag(name = "Address Management", description = "APIs for managing user addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private com.ecommerce.backend.infrastructure.security.CurrentUserService currentUserService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user addresses", description = "Retrieve all addresses for a specific user")
    public ResponseEntity<BaseResponseDto<List<AddressDto>>> getUserAddresses(@PathVariable Long userId) {
        try {
            Long currentUserId = getCurrentUserId();
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(BaseResponseDto.error("You can only access your own addresses"));
            }

            BaseResponseDto<List<AddressDto>> result = addressService.getUserAddresses(userId);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error retrieving user addresses: " + ex.getMessage()));
        }
    }

    @GetMapping("/{addressId}")
    @Operation(summary = "Get address by ID", description = "Retrieve a specific address by ID")
    public ResponseEntity<BaseResponseDto<AddressDto>> getAddress(@PathVariable Long addressId) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<AddressDto> result = addressService.getAddressById(addressId, currentUserId);

            if (result.isSuccess() && result.getData() != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error retrieving address: " + ex.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Create address", description = "Create a new address")
    public ResponseEntity<BaseResponseDto<AddressDto>> createAddress(
            @Valid @RequestBody CreateAddressDto createAddressDto) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<AddressDto> result = addressService.createAddress(currentUserId, createAddressDto);

            if (result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error creating address: " + ex.getMessage()));
        }
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "Update address", description = "Update an existing address")
    public ResponseEntity<BaseResponseDto<AddressDto>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressDto updateAddressDto) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<AddressDto> result = addressService.updateAddress(addressId, currentUserId,
                    updateAddressDto);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error updating address: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Delete address", description = "Delete an address")
    public ResponseEntity<BaseResponseDto<String>> deleteAddress(@PathVariable Long addressId) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<String> result = addressService.deleteAddress(addressId, currentUserId);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error deleting address: " + ex.getMessage()));
        }
    }

    @PutMapping("/{addressId}/default")
    @Operation(summary = "Set default address", description = "Set an address as default")
    public ResponseEntity<BaseResponseDto<AddressDto>> setDefaultAddress(@PathVariable Long addressId) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<AddressDto> result = addressService.setDefaultAddress(addressId, currentUserId);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error setting default address: " + ex.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        return currentUserService.requireUserId();
    }
}
