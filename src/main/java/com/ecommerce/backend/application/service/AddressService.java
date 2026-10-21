package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.AddressDto;
import com.ecommerce.backend.application.dto.CreateAddressDto;
import com.ecommerce.backend.application.dto.UpdateAddressDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.Address;
import com.ecommerce.backend.infrastructure.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adresler — docs/API_CONTRACT.md §4.8. Tüm işlemler çağıran kullanıcının adresleriyle
 * sınırlıdır; başkasının adresi "bulunamadı" (404) sayılır.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    static final String DEFAULT_COUNTRY = "Turkey";

    private final AddressRepository addressRepository;

    /** Varsayılan önce, sonra en yeni. */
    @Transactional(readOnly = true)
    public List<AddressDto> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDescIdDesc(userId).stream()
                .map(AddressDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AddressDto getAddress(Long addressId, Long userId) {
        return AddressDto.from(require(addressId, userId));
    }

    /** Kullanıcının ilk adresi otomatik varsayılan olur; {@code isDefault:true} diğerlerini kaldırır. */
    public AddressDto createAddress(Long userId, CreateAddressDto dto) {
        boolean makeDefault = Boolean.TRUE.equals(dto.getIsDefault())
                || !addressRepository.existsByUserIdAndIsActiveTrue(userId);
        if (makeDefault) {
            clearDefault(userId, null);
        }
        Address address = new Address();
        address.setUserId(userId);
        address.setTitle(dto.getTitle());
        address.setFullAddress(dto.getFullAddress());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(countryOrDefault(dto.getCountry()));
        address.setIsDefault(makeDefault);
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setIsActive(true);
        return AddressDto.from(addressRepository.saveAndFlush(address));
    }

    /** {@code country} boşsa "Turkey"; {@code isDefault} gönderilmezse değişmez. */
    public AddressDto updateAddress(Long addressId, Long userId, UpdateAddressDto dto) {
        Address address = require(addressId, userId);
        if (dto.getIsDefault() != null) {
            if (dto.getIsDefault()) {
                clearDefault(userId, addressId);
            }
            address.setIsDefault(dto.getIsDefault());
        }
        address.setTitle(dto.getTitle());
        address.setFullAddress(dto.getFullAddress());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(countryOrDefault(dto.getCountry()));
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setUpdatedAt(LocalDateTime.now());
        return AddressDto.from(addressRepository.saveAndFlush(address));
    }

    /** Yumuşak silme; varsayılan silinirse kalan en yeni adres varsayılan olur. */
    public void deleteAddress(Long addressId, Long userId) {
        Address address = require(addressId, userId);
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        address.setIsActive(false);
        address.setIsDefault(false);
        address.setUpdatedAt(LocalDateTime.now());
        addressRepository.saveAndFlush(address);
        if (wasDefault) {
            addressRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDescIdDesc(userId).stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setIsDefault(true);
                        next.setUpdatedAt(LocalDateTime.now());
                    });
        }
    }

    public AddressDto setDefaultAddress(Long addressId, Long userId) {
        Address address = require(addressId, userId);
        clearDefault(userId, addressId);
        address.setIsDefault(true);
        address.setUpdatedAt(LocalDateTime.now());
        return AddressDto.from(addressRepository.saveAndFlush(address));
    }

    private Address require(Long addressId, Long userId) {
        return addressRepository.findByIdAndUserIdAndIsActiveTrue(addressId, userId)
                .orElseThrow(() -> ApiException.notFound("ADDRESS_NOT_FOUND", "Address not found"));
    }

    private void clearDefault(Long userId, Long exceptId) {
        LocalDateTime now = LocalDateTime.now();
        for (Address other : addressRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId)) {
            if (!other.getId().equals(exceptId)) {
                other.setIsDefault(false);
                other.setUpdatedAt(now);
            }
        }
    }

    private static String countryOrDefault(String country) {
        return StringUtils.hasText(country) ? country.trim() : DEFAULT_COUNTRY;
    }
}
