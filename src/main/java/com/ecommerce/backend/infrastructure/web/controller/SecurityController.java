package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.ChangePasswordDto;
import com.ecommerce.backend.application.dto.DisableTwoFactorDto;
import com.ecommerce.backend.application.dto.EnableTwoFactorDto;
import com.ecommerce.backend.application.dto.LoginHistoryDto;
import com.ecommerce.backend.application.dto.SecurityDto;
import com.ecommerce.backend.application.dto.SecuritySettingsDto;
import com.ecommerce.backend.application.dto.UpdateEmailDto;
import com.ecommerce.backend.application.service.SecurityService;
import com.ecommerce.backend.infrastructure.security.AuthenticatedUser;
import com.ecommerce.backend.infrastructure.security.CurrentUserService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * docs/API_CONTRACT.md §4.13 — oturum sahibinin hesap güvenliği.
 */
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@Tag(name = "Security", description = "Şifre, e-posta, giriş geçmişi, oturum iptali")
public class SecurityController {

    private final SecurityService securityService;
    private final CurrentUserService currentUserService;

    @GetMapping("/info")
    @Operation(summary = "Güvenlik özeti (son başarılı giriş, son 5 giriş)")
    public ResponseEntity<BaseResponseDto<SecurityDto>> getSecurityInfo() {
        return ApiResponses.ok("Security information retrieved successfully",
                securityService.getSecurityInfo(currentUserService.requireUserId()));
    }

    @GetMapping("/login-history")
    @Operation(summary = "Giriş geçmişi (en yeni önce)")
    public ResponseEntity<BaseResponseDto<List<LoginHistoryDto>>> getLoginHistory(
            @RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize) {
        return ApiResponses.ok("Login history retrieved successfully", securityService.loginHistory(
                currentUserService.requireUserId(), ApiResponses.page(pageNumber), ApiResponses.size(pageSize, 10)));
    }

    @GetMapping("/settings")
    @Operation(summary = "Güvenlik ayarları")
    public ResponseEntity<BaseResponseDto<SecuritySettingsDto>> getSecuritySettings() {
        return ApiResponses.ok("Security settings retrieved successfully",
                securityService.getSecuritySettings(currentUserService.requireUserId()));
    }

    @PutMapping("/settings")
    @Operation(summary = "Güvenlik ayarlarını kaydet")
    public ResponseEntity<BaseResponseDto<SecuritySettingsDto>> updateSecuritySettings(
            @Valid @RequestBody SecuritySettingsDto request) {
        return ApiResponses.ok("Security settings updated successfully",
                securityService.updateSecuritySettings(currentUserService.requireUserId(), request));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Şifre değiştir; mevcut şifre hatalıysa 400 INVALID_PASSWORD")
    public ResponseEntity<BaseResponseDto<String>> changePassword(@Valid @RequestBody ChangePasswordDto request) {
        securityService.changePassword(currentUserService.requireUserId(), request);
        return ApiResponses.ok("Password changed successfully", "Password changed successfully");
    }

    @PostMapping("/update-email")
    @Operation(summary = "E-posta değiştir; kullanımdaysa 400 EMAIL_TAKEN")
    public ResponseEntity<BaseResponseDto<String>> updateEmail(@Valid @RequestBody UpdateEmailDto request) {
        securityService.updateEmail(currentUserService.requireUserId(), request);
        return ApiResponses.ok("Email updated successfully. Please verify your new email address.",
                "Email updated successfully");
    }

    @PostMapping("/logout-all-devices")
    @Operation(summary = "Çağıran token hariç tüm oturumları kapat")
    public ResponseEntity<BaseResponseDto<String>> logoutAllDevices() {
        AuthenticatedUser caller = currentUserService.require();
        securityService.logoutAllDevices(caller.id(), caller.jti());
        return ApiResponses.ok("All devices logged out successfully", "All devices logged out successfully");
    }

    @PostMapping("/enable-2fa")
    @Operation(summary = "Taslak: şifreyi doğrular (2FA henüz uygulanmadı)")
    public ResponseEntity<BaseResponseDto<String>> enableTwoFactor(@Valid @RequestBody EnableTwoFactorDto request) {
        securityService.verifyPasswordForTwoFactor(currentUserService.requireUserId(), request.getPassword());
        return ApiResponses.ok("Two-factor authentication enabled successfully",
                "Two-factor authentication enabled successfully");
    }

    @PostMapping("/disable-2fa")
    @Operation(summary = "Taslak: şifreyi doğrular (2FA henüz uygulanmadı)")
    public ResponseEntity<BaseResponseDto<String>> disableTwoFactor(@Valid @RequestBody DisableTwoFactorDto request) {
        securityService.verifyPasswordForTwoFactor(currentUserService.requireUserId(), request.getPassword());
        return ApiResponses.ok("Two-factor authentication disabled successfully",
                "Two-factor authentication disabled successfully");
    }
}
