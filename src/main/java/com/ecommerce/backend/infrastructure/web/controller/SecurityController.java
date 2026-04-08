package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.application.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security")
@Tag(name = "Security Management", description = "APIs for managing user security settings")
public class SecurityController {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private com.ecommerce.backend.infrastructure.security.CurrentUserService currentUserService;

    @GetMapping("/info")
    @Operation(summary = "Get security information", description = "Retrieve security information for the current user")
    public ResponseEntity<BaseResponseDto<SecurityDto>> getSecurityInfo() {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<SecurityDto> result = securityService.getSecurityInfo(currentUserId);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error retrieving security information: " + ex.getMessage()));
        }
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change user password")
    public ResponseEntity<BaseResponseDto<String>> changePassword(
            @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<String> result = securityService.changePassword(currentUserId, changePasswordDto);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error changing password: " + ex.getMessage()));
        }
    }

    @PostMapping("/update-email")
    @Operation(summary = "Update email", description = "Update user email address")
    public ResponseEntity<BaseResponseDto<String>> updateEmail(@Valid @RequestBody UpdateEmailDto updateEmailDto) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<String> result = securityService.updateEmail(currentUserId, updateEmailDto);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error updating email: " + ex.getMessage()));
        }
    }

    @GetMapping("/login-history")
    @Operation(summary = "Get login history", description = "Retrieve login history for the current user")
    public ResponseEntity<BaseResponseDto<List<LoginHistoryDto>>> getLoginHistory(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<List<LoginHistoryDto>> result = securityService.getLoginHistory(currentUserId, pageNumber,
                    pageSize);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error retrieving login history: " + ex.getMessage()));
        }
    }

    @GetMapping("/settings")
    @Operation(summary = "Get security settings", description = "Retrieve security settings for the current user")
    public ResponseEntity<BaseResponseDto<SecuritySettingsDto>> getSecuritySettings() {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<SecuritySettingsDto> result = securityService.getSecuritySettings(currentUserId);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error retrieving security settings: " + ex.getMessage()));
        }
    }

    @PutMapping("/settings")
    @Operation(summary = "Update security settings", description = "Update security settings for the current user")
    public ResponseEntity<BaseResponseDto<SecuritySettingsDto>> updateSecuritySettings(
            @Valid @RequestBody SecuritySettingsDto securitySettingsDto) {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<SecuritySettingsDto> result = securityService.updateSecuritySettings(currentUserId,
                    securitySettingsDto);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error updating security settings: " + ex.getMessage()));
        }
    }

    @PostMapping("/logout-all-devices")
    @Operation(summary = "Logout all devices", description = "Logout user from all devices")
    public ResponseEntity<BaseResponseDto<String>> logoutAllDevices() {
        try {
            Long currentUserId = getCurrentUserId();
            BaseResponseDto<String> result = securityService.logoutAllDevices(currentUserId);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.badRequest().body(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.error("Error logging out all devices: " + ex.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        return currentUserService.requireUserId();
    }
}
