package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.domain.entity.*;
import com.ecommerce.backend.infrastructure.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public BaseResponseDto<List<AddressDto>> getUserAddresses(Long userId) {
        try {
            List<Address> addresses = addressRepository
                    .findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(userId);
            List<AddressDto> addressDtos = addresses.stream()
                    .map(this::convertToAddressDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Addresses retrieved successfully", addressDtos);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error retrieving addresses: " + ex.getMessage());
        }
    }

    public BaseResponseDto<AddressDto> getAddressById(Long addressId, Long userId) {
        try {
            Address address = addressRepository.findByIdAndUserIdAndIsActiveTrue(addressId, userId).orElse(null);
            if (address == null) {
                return BaseResponseDto.error("Address not found");
            }

            AddressDto addressDto = convertToAddressDto(address);
            return BaseResponseDto.success("Address retrieved successfully", addressDto);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error retrieving address: " + ex.getMessage());
        }
    }

    public BaseResponseDto<AddressDto> createAddress(Long userId, CreateAddressDto createAddressDto) {
        try {
            // If this is set as default, remove default from other addresses
            if (createAddressDto.getIsDefault()) {
                List<Address> existingDefaultAddresses = addressRepository
                        .findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId);
                for (Address existingAddress : existingDefaultAddresses) {
                    existingAddress.setIsDefault(false);
                    existingAddress.setUpdatedAt(LocalDateTime.now());
                }
            }

            Address address = new Address();
            address.setUserId(userId);
            address.setTitle(createAddressDto.getTitle());
            address.setFullAddress(createAddressDto.getFullAddress());
            address.setCity(createAddressDto.getCity());
            address.setDistrict(createAddressDto.getDistrict());
            address.setPostalCode(createAddressDto.getPostalCode());
            address.setCountry(createAddressDto.getCountry());
            address.setIsDefault(createAddressDto.getIsDefault());
            address.setPhoneNumber(createAddressDto.getPhoneNumber());
            address.setIsActive(true);
            address.setCreatedAt(LocalDateTime.now());
            address.setUpdatedAt(LocalDateTime.now());

            Address savedAddress = addressRepository.save(address);
            AddressDto addressDto = convertToAddressDto(savedAddress);

            return BaseResponseDto.success("Address created successfully", addressDto);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error creating address: " + ex.getMessage());
        }
    }

    public BaseResponseDto<AddressDto> updateAddress(Long addressId, Long userId, UpdateAddressDto updateAddressDto) {
        try {
            Address address = addressRepository.findByIdAndUserIdAndIsActiveTrue(addressId, userId).orElse(null);
            if (address == null) {
                return BaseResponseDto.error("Address not found");
            }

            // If this is set as default, remove default from other addresses
            if (updateAddressDto.getIsDefault() && !address.getIsDefault()) {
                List<Address> existingDefaultAddresses = addressRepository
                        .findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId);
                for (Address existingAddress : existingDefaultAddresses) {
                    if (!existingAddress.getId().equals(addressId)) {
                        existingAddress.setIsDefault(false);
                        existingAddress.setUpdatedAt(LocalDateTime.now());
                    }
                }
            }

            address.setTitle(updateAddressDto.getTitle());
            address.setFullAddress(updateAddressDto.getFullAddress());
            address.setCity(updateAddressDto.getCity());
            address.setDistrict(updateAddressDto.getDistrict());
            address.setPostalCode(updateAddressDto.getPostalCode());
            address.setCountry(updateAddressDto.getCountry());
            address.setIsDefault(updateAddressDto.getIsDefault());
            address.setPhoneNumber(updateAddressDto.getPhoneNumber());
            address.setUpdatedAt(LocalDateTime.now());

            Address savedAddress = addressRepository.save(address);
            AddressDto addressDto = convertToAddressDto(savedAddress);

            return BaseResponseDto.success("Address updated successfully", addressDto);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error updating address: " + ex.getMessage());
        }
    }

    public BaseResponseDto<String> deleteAddress(Long addressId, Long userId) {
        try {
            Address address = addressRepository.findByIdAndUserIdAndIsActiveTrue(addressId, userId).orElse(null);
            if (address == null) {
                return BaseResponseDto.error("Address not found");
            }

            address.setIsActive(false);
            address.setUpdatedAt(LocalDateTime.now());
            addressRepository.save(address);

            return BaseResponseDto.success("Address deleted successfully", "Address deleted successfully");
        } catch (Exception ex) {
            return BaseResponseDto.error("Error deleting address: " + ex.getMessage());
        }
    }

    public BaseResponseDto<AddressDto> setDefaultAddress(Long addressId, Long userId) {
        try {
            Address address = addressRepository.findByIdAndUserIdAndIsActiveTrue(addressId, userId).orElse(null);
            if (address == null) {
                return BaseResponseDto.error("Address not found");
            }

            // Remove default from other addresses
            List<Address> existingDefaultAddresses = addressRepository
                    .findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId);
            for (Address existingAddress : existingDefaultAddresses) {
                if (!existingAddress.getId().equals(addressId)) {
                    existingAddress.setIsDefault(false);
                    existingAddress.setUpdatedAt(LocalDateTime.now());
                }
            }

            address.setIsDefault(true);
            address.setUpdatedAt(LocalDateTime.now());
            Address savedAddress = addressRepository.save(address);
            AddressDto addressDto = convertToAddressDto(savedAddress);

            return BaseResponseDto.success("Default address set successfully", addressDto);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error setting default address: " + ex.getMessage());
        }
    }

    private AddressDto convertToAddressDto(Address address) {
        return new AddressDto(
                address.getId(),
                address.getUserId(),
                address.getTitle(),
                address.getFullAddress(),
                address.getCity(),
                address.getDistrict(),
                address.getPostalCode(),
                address.getCountry(),
                address.getIsDefault(),
                address.getPhoneNumber(),
                address.getCreatedAt(),
                address.getUpdatedAt());
    }
}
