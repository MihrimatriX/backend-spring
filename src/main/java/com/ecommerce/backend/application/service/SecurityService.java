package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.ChangePasswordDto;
import com.ecommerce.backend.application.dto.LoginHistoryDto;
import com.ecommerce.backend.application.dto.SecurityDto;
import com.ecommerce.backend.application.dto.SecuritySettingsDto;
import com.ecommerce.backend.application.dto.UpdateEmailDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.LoginHistory;
import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.domain.entity.UserSettings;
import com.ecommerce.backend.infrastructure.repository.LoginHistoryRepository;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import com.ecommerce.backend.infrastructure.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Hesap güvenliği — docs/API_CONTRACT.md §4.13, §5.6. Giriş geçmişi {@code AuthService} tarafından yazılır;
 * güvenlik ayarları {@code user_settings} satırında saklanır.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SecurityService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("loginAt"), Sort.Order.desc("id"));
    private static final int RECENT_LOGIN_COUNT = 5;
    private static final String INVALID_PASSWORD = "INVALID_PASSWORD";

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final SettingsService settingsService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public SecurityDto getSecurityInfo(Long userId) {
        User user = requireUser(userId);
        List<LoginHistoryDto> recent = loginHistory(userId, 1, RECENT_LOGIN_COUNT);
        Optional<LoginHistory> lastLogin = loginHistoryRepository
                .findFirstByUserIdAndIsActiveTrueAndIsSuccessfulTrueOrderByLoginAtDescIdDesc(userId);
        return new SecurityDto(user.getId(), user.getEmail(), Boolean.TRUE.equals(user.getIsEmailVerified()),
                user.getUpdatedAt(), false,
                lastLogin.map(LoginHistory::getLoginAt).orElse(null),
                lastLogin.map(LoginHistory::getIpAddress).orElse(null),
                recent);
    }

    /** {@code pageNumber} 1 tabanlı, değerler çağıran tarafından kırpılmış olmalı. */
    @Transactional(readOnly = true)
    public List<LoginHistoryDto> loginHistory(Long userId, int pageNumber, int pageSize) {
        return loginHistoryRepository
                .findByUserIdAndIsActiveTrue(userId, PageRequest.of(pageNumber - 1, pageSize, NEWEST_FIRST))
                .stream().map(SecurityService::toDto).toList();
    }

    /** İlk okumada varsayılanlar: {@code true, false, true, false, 30}. */
    public SecuritySettingsDto getSecuritySettings(Long userId) {
        return toDto(settingsService.loadOrCreateUserSettings(userId));
    }

    /** Gönderilen (null olmayan) alanlar kaydedilir. */
    public SecuritySettingsDto updateSecuritySettings(Long userId, SecuritySettingsDto dto) {
        UserSettings s = settingsService.loadOrCreateUserSettings(userId);
        if (dto.getEmailNotifications() != null) {
            s.setEmailNotifications(dto.getEmailNotifications());
        }
        if (dto.getSmsNotifications() != null) {
            s.setSmsNotifications(dto.getSmsNotifications());
        }
        if (dto.getLoginAlerts() != null) {
            s.setLoginAlerts(dto.getLoginAlerts());
        }
        if (dto.getTwoFactorRequired() != null) {
            s.setTwoFactorRequired(dto.getTwoFactorRequired());
        }
        if (dto.getSessionTimeout() != null) {
            s.setSessionTimeout(dto.getSessionTimeout());
        }
        s.setUpdatedAt(LocalDateTime.now());
        return toDto(userSettingsRepository.save(s));
    }

    public void changePassword(Long userId, ChangePasswordDto dto) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw ApiException.badRequest(INVALID_PASSWORD, "Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Şifre değiştirildi: userId={}", userId);
    }

    /** E-posta küçük harfe çevrilir; doğrulama durumu sıfırlanır. */
    public void updateEmail(Long userId, UpdateEmailDto dto) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw ApiException.badRequest(INVALID_PASSWORD, "Current password is incorrect");
        }
        String email = AuthService.normalizeEmail(dto.getNewEmail());
        boolean taken = userRepository.findFirstByEmailIgnoreCase(email)
                .filter(other -> !other.getId().equals(userId))
                .isPresent();
        if (taken) {
            throw ApiException.badRequest("EMAIL_TAKEN", "Email address is already in use");
        }
        user.setEmail(email);
        user.setIsEmailVerified(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("E-posta güncellendi: userId={}", userId);
    }

    /**
     * Çağıran token hariç tüm token'ları geçersiz kılar (§5.6): iptal zamanı saniye hassasiyetinde (UTC)
     * yazılır; {@code JwtAuthenticationFilter} {@code iat <= tokensRevokedAt} ve {@code jti != revokeExceptJti}
     * olan token'ları reddeder.
     */
    public void logoutAllDevices(Long userId, String callerJti) {
        User user = requireUser(userId);
        user.setTokensRevokedAt(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        user.setRevokeExceptJti(callerJti);
        userRepository.save(user);
        log.info("Tüm cihazlardan çıkış: userId={}", userId);
    }

    /** Taslak: yalnızca şifreyi doğrular (2FA henüz uygulanmadı). */
    @Transactional(readOnly = true)
    public void verifyPasswordForTwoFactor(Long userId, String password) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw ApiException.badRequest(INVALID_PASSWORD, "Password is incorrect");
        }
    }

    private User requireUser(Long userId) {
        return userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));
    }

    private static SecuritySettingsDto toDto(UserSettings s) {
        return new SecuritySettingsDto(s.getEmailNotifications(), s.getSmsNotifications(), s.getLoginAlerts(),
                s.getTwoFactorRequired(), s.getSessionTimeout());
    }

    private static LoginHistoryDto toDto(LoginHistory h) {
        return new LoginHistoryDto(h.getId(), h.getLoginAt(), h.getIpAddress(), h.getUserAgent(), h.getLocation(),
                Boolean.TRUE.equals(h.getIsSuccessful()));
    }
}
