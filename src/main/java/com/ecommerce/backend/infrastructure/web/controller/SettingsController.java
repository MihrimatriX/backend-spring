package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.PrivacySettingsDto;
import com.ecommerce.backend.application.dto.UpdatePrivacySettingsDto;
import com.ecommerce.backend.application.dto.UpdateUserSettingsDto;
import com.ecommerce.backend.application.dto.UserSettingsDto;
import com.ecommerce.backend.application.service.SettingsService;
import com.ecommerce.backend.infrastructure.security.CurrentUserService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * docs/API_CONTRACT.md §4.14 — her zaman çağıranın ayarları (yolda kullanıcı id'si yok).
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Kullanıcı ve gizlilik ayarları")
public class SettingsController {

    private final SettingsService settingsService;
    private final CurrentUserService currentUserService;

    @GetMapping("/user")
    @Operation(summary = "Kullanıcı ayarları (yoksa varsayılanlarla oluşturulur)")
    public ResponseEntity<BaseResponseDto<UserSettingsDto>> getUserSettings() {
        return ApiResponses.ok("User settings retrieved successfully",
                settingsService.getUserSettings(currentUserService.requireUserId()));
    }

    @PutMapping("/user")
    @Operation(summary = "Kullanıcı ayarlarını kısmi güncelle")
    public ResponseEntity<BaseResponseDto<UserSettingsDto>> updateUserSettings(
            @Valid @RequestBody UpdateUserSettingsDto request) {
        return ApiResponses.ok("User settings updated successfully",
                settingsService.updateUserSettings(currentUserService.requireUserId(), request));
    }

    @GetMapping("/privacy")
    @Operation(summary = "Gizlilik ayarları (yoksa varsayılanlarla oluşturulur)")
    public ResponseEntity<BaseResponseDto<PrivacySettingsDto>> getPrivacySettings() {
        return ApiResponses.ok("Privacy settings retrieved successfully",
                settingsService.getPrivacySettings(currentUserService.requireUserId()));
    }

    @PutMapping("/privacy")
    @Operation(summary = "Gizlilik ayarlarını kısmi güncelle")
    public ResponseEntity<BaseResponseDto<PrivacySettingsDto>> updatePrivacySettings(
            @Valid @RequestBody UpdatePrivacySettingsDto request) {
        return ApiResponses.ok("Privacy settings updated successfully",
                settingsService.updatePrivacySettings(currentUserService.requireUserId(), request));
    }

    @PostMapping("/reset")
    @Operation(summary = "Kullanıcı ve gizlilik ayarlarını varsayılana döndür")
    public ResponseEntity<BaseResponseDto<String>> resetToDefaults() {
        settingsService.resetToDefaults(currentUserService.requireUserId());
        return ApiResponses.ok("Settings reset to defaults successfully", "Settings reset to defaults successfully");
    }

    @GetMapping("/export")
    @Operation(summary = "Ayarları JSON metni olarak dışa aktar")
    public ResponseEntity<BaseResponseDto<String>> exportSettings() {
        return ApiResponses.ok("Settings exported successfully",
                settingsService.exportSettings(currentUserService.requireUserId()));
    }

    @PostMapping("/import")
    @Operation(summary = "Export metnini (JSON string) veya aynı nesneyi içe aktar; geçersizse 400 INVALID_IMPORT")
    public ResponseEntity<BaseResponseDto<String>> importSettings(@RequestBody JsonNode body) {
        settingsService.importSettings(currentUserService.requireUserId(), body);
        return ApiResponses.ok("Settings imported successfully", "Settings imported successfully");
    }
}
