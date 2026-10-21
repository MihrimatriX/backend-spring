package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateAddressDto {
    @NotBlank(message = "Address title is required")
    @Size(max = 100, message = "Address title cannot exceed 100 characters")
    private String title;

    @NotBlank(message = "Full address is required")
    @Size(max = 500, message = "Full address cannot exceed 500 characters")
    private String fullAddress;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @NotBlank(message = "District is required")
    @Size(max = 100, message = "District cannot exceed 100 characters")
    private String district;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    private Boolean isDefault;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;

    // Constructors
    public UpdateAddressDto() {}

    public UpdateAddressDto(String title, String fullAddress, String city, String district, 
                           String postalCode, String country, Boolean isDefault, String phoneNumber) {
        this.title = title;
        this.fullAddress = fullAddress;
        this.city = city;
        this.district = district;
        this.postalCode = postalCode;
        this.country = country;
        this.isDefault = isDefault;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
