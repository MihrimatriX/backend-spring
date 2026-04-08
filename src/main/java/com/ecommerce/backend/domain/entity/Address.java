package com.ecommerce.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull(message = "Address title is required")
    @Size(max = 100, message = "Address title cannot exceed 100 characters")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @NotNull(message = "Full address is required")
    @Size(max = 500, message = "Full address cannot exceed 500 characters")
    @Column(name = "full_address", nullable = false, length = 500)
    private String fullAddress;

    @NotNull(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @NotNull(message = "District is required")
    @Size(max = 100, message = "District cannot exceed 100 characters")
    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @NotNull(message = "Postal code is required")
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    @Column(name = "country", length = 100)
    private String country = "Turkey";

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    // Constructors
    public Address() {
    }

    public Address(Long userId, String title, String fullAddress, String city,
            String district, String postalCode, String country,
            Boolean isDefault, String phoneNumber) {
        this.userId = userId;
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
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
