package com.ecommerce.backend.application.dto;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code POST /api/security/change-password}. Şifreler eşleşmezse 400 {@code VALIDATION_ERROR} ve
 * {@code errors.confirmPassword} (.NET {@code [Compare]} karşılığı).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ChangePasswordDto.PasswordsMatch
public class ChangePasswordDto {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "New password must be between 6 and 100 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = PasswordsMatchValidator.class)
    public @interface PasswordsMatch {
        String message() default "New password and confirm password do not match";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    /** Hata {@code confirmPassword} alanına yazılır. */
    public static class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, ChangePasswordDto> {
        @Override
        public boolean isValid(ChangePasswordDto dto, ConstraintValidatorContext context) {
            if (dto == null || dto.newPassword == null || dto.confirmPassword == null
                    || dto.confirmPassword.isEmpty() || dto.newPassword.equals(dto.confirmPassword)) {
                return true;
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
            return false;
        }
    }
}
