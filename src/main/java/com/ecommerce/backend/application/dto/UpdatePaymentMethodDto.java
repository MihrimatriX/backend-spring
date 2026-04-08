package com.ecommerce.backend.application.dto;

import jakarta.validation.constraints.*;

public class UpdatePaymentMethodDto {
    @NotBlank(message = "Payment method type is required")
    @Size(max = 50, message = "Payment method type cannot exceed 50 characters")
    private String type;

    @NotBlank(message = "Card holder name is required")
    @Size(max = 100, message = "Card holder name cannot exceed 100 characters")
    private String cardHolderName;

    @NotBlank(message = "Card number is required")
    @Size(max = 20, message = "Card number cannot exceed 20 characters")
    private String cardNumber;

    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    private Integer expiryMonth;

    @NotNull(message = "Expiry year is required")
    @Min(value = 2024, message = "Expiry year must be between 2024 and 2050")
    @Max(value = 2050, message = "Expiry year must be between 2024 and 2050")
    private Integer expiryYear;

    @Size(max = 10, message = "CVV cannot exceed 10 characters")
    private String cvv;

    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;

    @Size(max = 50, message = "Account number cannot exceed 50 characters")
    private String accountNumber;

    @Size(max = 100, message = "Account holder name cannot exceed 100 characters")
    private String accountHolderName;

    private Boolean isDefault;

    // Constructors
    public UpdatePaymentMethodDto() {}

    public UpdatePaymentMethodDto(String type, String cardHolderName, String cardNumber, 
                                 Integer expiryMonth, Integer expiryYear, String cvv, 
                                 String bankName, String accountNumber, String accountHolderName, 
                                 Boolean isDefault) {
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
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public Integer getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(Integer expiryMonth) { this.expiryMonth = expiryMonth; }

    public Integer getExpiryYear() { return expiryYear; }
    public void setExpiryYear(Integer expiryYear) { this.expiryYear = expiryYear; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
