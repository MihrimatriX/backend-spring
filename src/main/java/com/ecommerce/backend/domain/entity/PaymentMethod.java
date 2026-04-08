package com.ecommerce.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "payment_methods")
public class PaymentMethod extends BaseEntity {

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "Payment method type is required")
    @Size(max = 50, message = "Payment method type cannot exceed 50 characters")
    @Column(name = "type", nullable = false, length = 50)
    private String type; // CreditCard, DebitCard, PayPal, BankTransfer

    @NotBlank(message = "Card holder name is required")
    @Size(max = 100, message = "Card holder name cannot exceed 100 characters")
    @Column(name = "card_holder_name", nullable = false, length = 100)
    private String cardHolderName;

    @NotBlank(message = "Card number is required")
    @Size(max = 20, message = "Card number cannot exceed 20 characters")
    @Column(name = "card_number", nullable = false, length = 20)
    private String cardNumber; // Masked: **** **** **** 1234

    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;

    @NotNull(message = "Expiry year is required")
    @Min(value = 2024, message = "Expiry year must be between 2024 and 2050")
    @Max(value = 2050, message = "Expiry year must be between 2024 and 2050")
    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;

    @Size(max = 10, message = "CVV cannot exceed 10 characters")
    @Column(name = "cvv", length = 10)
    private String cvv; // Encrypted

    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Size(max = 50, message = "Account number cannot exceed 50 characters")
    @Column(name = "account_number", length = 50)
    private String accountNumber; // Masked

    @Size(max = 100, message = "Account holder name cannot exceed 100 characters")
    @Column(name = "account_holder_name", length = 100)
    private String accountHolderName;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    // Constructors
    public PaymentMethod() {
    }

    public PaymentMethod(Long userId, String type, String cardHolderName, String cardNumber,
            Integer expiryMonth, Integer expiryYear, String cvv, String bankName,
            String accountNumber, String accountHolderName, Boolean isDefault) {
        this.userId = userId;
        this.type = type;
        this.cardHolderName = cardHolderName;
        this.cardNumber = cardNumber;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Integer getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(Integer expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public Integer getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(Integer expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
