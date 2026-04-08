package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.UserSettingsDto;
import com.ecommerce.backend.application.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponseDto<UserSettingsDto>> getUserSettings(@PathVariable Long userId) {
        log.info("Getting user settings for user ID: {}", userId);
        BaseResponseDto<UserSettingsDto> response = settingsService.getUserSettings(userId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponseDto<UserSettingsDto>> updateUserSettings(
            @PathVariable Long userId,
            @RequestBody UserSettingsDto settingsDto) {
        log.info("Updating user settings for user ID: {}", userId);
        BaseResponseDto<UserSettingsDto> response = settingsService.updateUserSettings(userId, settingsDto);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/privacy/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponseDto<Object>> getPrivacySettings(@PathVariable Long userId) {
        log.info("Getting privacy settings for user ID: {}", userId);
        BaseResponseDto<Object> response = settingsService.getPrivacySettings(userId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/privacy/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BaseResponseDto<Object>> updatePrivacySettings(
            @PathVariable Long userId,
            @RequestBody Object privacySettings) {
        log.info("Updating privacy settings for user ID: {}", userId);
        BaseResponseDto<Object> response = settingsService.updatePrivacySettings(userId, privacySettings);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }
}
