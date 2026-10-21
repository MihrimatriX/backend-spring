package com.ecommerce.backend.application.dto;

import com.ecommerce.backend.domain.entity.Address;

import java.time.LocalDateTime;

/** Adres (docs/API_CONTRACT.md §3). Siparişlerde de aynı tam şekil kullanılır. */
public record AddressDto(
        Long id,
        Long userId,
        String title,
        String fullAddress,
        String city,
        String district,
        String postalCode,
        String country,
        Boolean isDefault,
        String phoneNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static AddressDto from(Address a) {
        return new AddressDto(a.getId(), a.getUserId(), a.getTitle(), a.getFullAddress(), a.getCity(),
                a.getDistrict(), a.getPostalCode(), a.getCountry(), Boolean.TRUE.equals(a.getIsDefault()),
                a.getPhoneNumber(), a.getCreatedAt(), a.getUpdatedAt());
    }
}
