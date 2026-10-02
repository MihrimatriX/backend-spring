package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.AuthResponseDto;
import com.ecommerce.backend.application.dto.LoginRequestDto;
import com.ecommerce.backend.application.dto.RegisterRequestDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.LoginHistory;
import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.infrastructure.config.AuthProperties;
import com.ecommerce.backend.infrastructure.repository.LoginHistoryRepository;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import com.ecommerce.backend.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

/**
 * Kayıt / giriş — docs/API_CONTRACT.md §2, §4.1.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthProperties authProperties;
    private final MetricsService metricsService;

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("EMAIL_TAKEN", "Email is already taken");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setPostalCode(request.getPostalCode());
        user.setIsEmailVerified(false);
        user.setIsActive(true);
        User saved = userRepository.save(user);

        metricsService.incrementUserRegistrationCounter();
        log.info("Yeni kullanıcı kaydı: id={}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Başarısız denemeler de (kullanıcı varsa) giriş geçmişine yazılır; {@code noRollbackFor} sayesinde
     * hata fırlatılsa da kayıt kalıcı olur.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponseDto login(LoginRequestDto request, String ipAddress, String userAgent) {
        String email = normalizeEmail(request.getEmail());
        Optional<User> found = userRepository.findFirstByEmailIgnoreCase(email);
        if (found.isEmpty() || !Boolean.TRUE.equals(found.get().getIsActive())
                || !passwordEncoder.matches(request.getPassword(), found.get().getPassword())) {
            found.ifPresent(u -> recordLogin(u.getId(), ipAddress, userAgent, false,
                    Boolean.TRUE.equals(u.getIsActive()) ? "Invalid password" : "Inactive account"));
            metricsService.incrementAuthenticationFailureCounter("bad_credentials");
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "Invalid email or password");
        }

        User user = found.get();
        recordLogin(user.getId(), ipAddress, userAgent, true, null);
        return toResponse(user);
    }

    private void recordLogin(Long userId, String ip, String userAgent, boolean success, String failureReason) {
        LoginHistory entry = new LoginHistory(userId, LocalDateTime.now(), truncate(ip, 45), truncate(userAgent, 500),
                null, success, failureReason);
        loginHistoryRepository.save(entry);
    }

    private AuthResponseDto toResponse(User user) {
        String role = authProperties.roleFor(user.getEmail());
        String token = jwtService.generateToken(user, role);
        return AuthResponseDto.bearer(token, user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getIsEmailVerified(), role);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
