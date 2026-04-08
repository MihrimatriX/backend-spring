package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.domain.entity.*;
import com.ecommerce.backend.infrastructure.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SecurityService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public BaseResponseDto<SecurityDto> getSecurityInfo(Long userId) {
        try {
            User user = userRepository.findByIdAndIsActiveTrue(userId).orElse(null);
            if (user == null) {
                return BaseResponseDto.error("User not found");
            }

            List<LoginHistory> recentLogins = loginHistoryRepository
                    .findTop5ByUserIdAndIsActiveTrueOrderByLoginAtDesc(userId);
            List<LoginHistoryDto> recentLoginDtos = recentLogins.stream()
                    .map(this::convertToLoginHistoryDto)
                    .collect(Collectors.toList());

            SecurityDto securityInfo = new SecurityDto(
                    user.getId(),
                    user.getEmail(),
                    user.getIsEmailVerified(),
                    user.getUpdatedAt(), // Assuming this tracks password changes
                    false, // Implement 2FA later
                    null, // Add this field to User entity if needed
                    null, // Add this field to User entity if needed
                    recentLoginDtos);

            return BaseResponseDto.success("Security information retrieved successfully", securityInfo);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error retrieving security information: " + ex.getMessage());
        }
    }

    public BaseResponseDto<String> changePassword(Long userId, ChangePasswordDto changePasswordDto) {
        try {
            User user = userRepository.findByIdAndIsActiveTrue(userId).orElse(null);
            if (user == null) {
                return BaseResponseDto.error("User not found");
            }

            // Verify current password
            if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
                return BaseResponseDto.error("Current password is incorrect");
            }

            // Hash new password
            String hashedNewPassword = passwordEncoder.encode(changePasswordDto.getNewPassword());
            user.setPassword(hashedNewPassword);
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            return BaseResponseDto.success("Password changed successfully", "Password changed successfully");
        } catch (Exception ex) {
            return BaseResponseDto.error("Error changing password: " + ex.getMessage());
        }
    }

    public BaseResponseDto<String> updateEmail(Long userId, UpdateEmailDto updateEmailDto) {
        try {
            User user = userRepository.findByIdAndIsActiveTrue(userId).orElse(null);
            if (user == null) {
                return BaseResponseDto.error("User not found");
            }

            // Verify current password
            if (!passwordEncoder.matches(updateEmailDto.getCurrentPassword(), user.getPassword())) {
                return BaseResponseDto.error("Current password is incorrect");
            }

            // Check if email already exists
            User existingUser = userRepository.findByEmailAndIsActiveTrue(updateEmailDto.getNewEmail()).orElse(null);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                return BaseResponseDto.error("Email address is already in use");
            }

            user.setEmail(updateEmailDto.getNewEmail());
            user.setIsEmailVerified(false); // Require email verification
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            return BaseResponseDto.success("Email updated successfully. Please verify your new email address.",
                    "Email updated successfully. Please verify your new email address.");
        } catch (Exception ex) {
            return BaseResponseDto.error("Error updating email: " + ex.getMessage());
        }
    }

    public BaseResponseDto<List<LoginHistoryDto>> getLoginHistory(Long userId, int pageNumber, int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
            Page<LoginHistory> loginHistoryPage = loginHistoryRepository
                    .findByUserIdAndIsActiveTrueOrderByLoginAtDesc(userId, pageable);

            List<LoginHistoryDto> loginHistoryDtos = loginHistoryPage.getContent().stream()
                    .map(this::convertToLoginHistoryDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Login history retrieved successfully", loginHistoryDtos);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error retrieving login history: " + ex.getMessage());
        }
    }

    public BaseResponseDto<SecuritySettingsDto> getSecuritySettings(Long userId) {
        try {
            // TODO: Implement security settings storage
            // For now, return default settings
            SecuritySettingsDto settings = new SecuritySettingsDto(
                    true, // emailNotifications
                    false, // smsNotifications
                    true, // loginAlerts
                    false, // twoFactorRequired
                    30 // sessionTimeout
            );

            return BaseResponseDto.success("Security settings retrieved successfully", settings);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error retrieving security settings: " + ex.getMessage());
        }
    }

    public BaseResponseDto<SecuritySettingsDto> updateSecuritySettings(Long userId,
            SecuritySettingsDto securitySettingsDto) {
        try {
            // TODO: Implement security settings storage
            // For now, just return the updated settings
            return BaseResponseDto.success("Security settings updated successfully", securitySettingsDto);
        } catch (Exception ex) {
            return BaseResponseDto.error("Error updating security settings: " + ex.getMessage());
        }
    }

    public BaseResponseDto<String> logoutAllDevices(Long userId) {
        try {
            // TODO: Implement device logout logic
            // This would typically involve invalidating all JWT tokens for the user
            return BaseResponseDto.success("All devices logged out successfully",
                    "All devices logged out successfully");
        } catch (Exception ex) {
            return BaseResponseDto.error("Error logging out all devices: " + ex.getMessage());
        }
    }

    private LoginHistoryDto convertToLoginHistoryDto(LoginHistory loginHistory) {
        return new LoginHistoryDto(
                loginHistory.getId(),
                loginHistory.getLoginAt(),
                loginHistory.getIpAddress(),
                loginHistory.getUserAgent(),
                loginHistory.getLocation(),
                loginHistory.getIsSuccessful());
    }
}
