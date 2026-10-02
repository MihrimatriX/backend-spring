package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.PrivacySettingsDto;
import com.ecommerce.backend.application.dto.UpdatePrivacySettingsDto;
import com.ecommerce.backend.application.dto.UpdateUserSettingsDto;
import com.ecommerce.backend.application.dto.UserSettingsDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.PrivacySettings;
import com.ecommerce.backend.domain.entity.UserSettings;
import com.ecommerce.backend.infrastructure.repository.PrivacySettingsRepository;
import com.ecommerce.backend.infrastructure.repository.UserSettingsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Kullanıcı ve gizlilik ayarları (her zaman çağıranın) — docs/API_CONTRACT.md §4.14.
 * Satır yoksa ilk erişimde varsayılanlarla oluşturulur; güncellemeler kısmidir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SettingsService {

    private static final String INVALID_IMPORT = "INVALID_IMPORT";
    private static final String INVALID_IMPORT_MESSAGE = "Invalid settings data";

    private final UserSettingsRepository userSettingsRepository;
    private final PrivacySettingsRepository privacySettingsRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public UserSettingsDto getUserSettings(Long userId) {
        return toDto(loadOrCreateUserSettings(userId));
    }

    public UserSettingsDto updateUserSettings(Long userId, UpdateUserSettingsDto dto) {
        UserSettings s = loadOrCreateUserSettings(userId);
        apply(s, dto);
        return toDto(userSettingsRepository.save(s));
    }

    public PrivacySettingsDto getPrivacySettings(Long userId) {
        return toDto(loadOrCreatePrivacySettings(userId));
    }

    public PrivacySettingsDto updatePrivacySettings(Long userId, UpdatePrivacySettingsDto dto) {
        PrivacySettings s = loadOrCreatePrivacySettings(userId);
        apply(s, dto);
        return toDto(privacySettingsRepository.save(s));
    }

    /** Kullanıcı ve gizlilik tercihlerini varsayılana döndürür (güvenlik ayarları korunur). */
    public void resetToDefaults(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        UserSettings us = loadOrCreateUserSettings(userId);
        us.applyPreferenceDefaults();
        us.setUpdatedAt(now);
        userSettingsRepository.save(us);
        PrivacySettings ps = loadOrCreatePrivacySettings(userId);
        ps.applyDefaults();
        ps.setUpdatedAt(now);
        privacySettingsRepository.save(ps);
    }

    /** {@code {"userSettings":{…},"privacySettings":{…},"exportDate":"…Z"}} JSON metni. */
    public String exportSettings(Long userId) {
        Map<String, Object> export = new LinkedHashMap<>();
        export.put("userSettings", getUserSettings(userId));
        export.put("privacySettings", getPrivacySettings(userId));
        export.put("exportDate", LocalDateTime.now());
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(export);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Ayarlar JSON'a çevrilemedi", e);
        }
    }

    /**
     * Export çıktısını uygular. Gövde, export metnini taşıyan bir JSON string'i veya doğrudan aynı nesne
     * olabilir; okunamayan/geçersiz veri 400 {@code INVALID_IMPORT}.
     */
    public void importSettings(Long userId, JsonNode body) {
        JsonNode root = body;
        if (root != null && root.isTextual()) {
            try {
                root = objectMapper.readTree(root.asText());
            } catch (JsonProcessingException e) {
                throw invalidImport();
            }
        }
        if (root == null || !root.isObject()) {
            throw invalidImport();
        }
        JsonNode userNode = field(root, "userSettings");
        JsonNode privacyNode = field(root, "privacySettings");
        if (userNode == null && privacyNode == null) {
            throw invalidImport();
        }
        UpdateUserSettingsDto userDto = read(userNode, UpdateUserSettingsDto.class);
        UpdatePrivacySettingsDto privacyDto = read(privacyNode, UpdatePrivacySettingsDto.class);
        if (userDto != null) {
            updateUserSettings(userId, userDto);
        }
        if (privacyDto != null) {
            updatePrivacySettings(userId, privacyDto);
        }
        log.info("Ayarlar içe aktarıldı: userId={}", userId);
    }

    /** Güvenlik ayarları da bu satırda tutulur ({@code SecurityService}). */
    public UserSettings loadOrCreateUserSettings(Long userId) {
        return userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> userSettingsRepository.save(UserSettings.defaultsFor(userId)));
    }

    private PrivacySettings loadOrCreatePrivacySettings(Long userId) {
        return privacySettingsRepository.findByUserId(userId)
                .orElseGet(() -> privacySettingsRepository.save(PrivacySettings.defaultsFor(userId)));
    }

    private <T> T read(JsonNode node, Class<T> type) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            throw invalidImport();
        }
        T value;
        try {
            value = objectMapper.treeToValue(node, type);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw invalidImport();
        }
        if (!validator.validate(value).isEmpty()) {
            throw invalidImport();
        }
        return value;
    }

    /** Üst düzey anahtarlar harf duyarsız okunur ({@code UserSettings} da kabul edilir). */
    private static JsonNode field(JsonNode root, String name) {
        for (Iterator<Map.Entry<String, JsonNode>> it = root.fields(); it.hasNext();) {
            Map.Entry<String, JsonNode> e = it.next();
            if (e.getKey().equalsIgnoreCase(name)) {
                return e.getValue();
            }
        }
        return null;
    }

    private static ApiException invalidImport() {
        return ApiException.badRequest(INVALID_IMPORT, INVALID_IMPORT_MESSAGE);
    }

    private static void apply(UserSettings s, UpdateUserSettingsDto dto) {
        setText(dto.getLanguage(), s::setLanguage);
        setText(dto.getTimezone(), s::setTimezone);
        setText(dto.getCurrency(), s::setCurrency);
        set(dto.getEmailNotifications(), s::setEmailNotifications);
        set(dto.getSmsNotifications(), s::setSmsNotifications);
        set(dto.getPushNotifications(), s::setPushNotifications);
        set(dto.getMarketingEmails(), s::setMarketingEmails);
        set(dto.getOrderUpdates(), s::setOrderUpdates);
        set(dto.getPriceAlerts(), s::setPriceAlerts);
        set(dto.getStockNotifications(), s::setStockNotifications);
        setText(dto.getTheme(), s::setTheme);
        set(dto.getItemsPerPage(), s::setItemsPerPage);
        set(dto.getAutoSaveCart(), s::setAutoSaveCart);
        set(dto.getShowProductRecommendations(), s::setShowProductRecommendations);
        set(dto.getEnableLocationServices(), s::setEnableLocationServices);
        s.setUpdatedAt(LocalDateTime.now());
    }

    private static void apply(PrivacySettings s, UpdatePrivacySettingsDto dto) {
        set(dto.getProfileVisibility(), s::setProfileVisibility);
        set(dto.getShowEmail(), s::setShowEmail);
        set(dto.getShowPhone(), s::setShowPhone);
        set(dto.getAllowDataCollection(), s::setAllowDataCollection);
        set(dto.getAllowAnalytics(), s::setAllowAnalytics);
        set(dto.getAllowCookies(), s::setAllowCookies);
        set(dto.getAllowMarketing(), s::setAllowMarketing);
        set(dto.getDataSharing(), s::setDataSharing);
        s.setUpdatedAt(LocalDateTime.now());
    }

    private static <T> void set(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private static void setText(String value, Consumer<String> setter) {
        if (value != null && !value.isBlank()) {
            setter.accept(value.trim());
        }
    }

    private static UserSettingsDto toDto(UserSettings s) {
        return new UserSettingsDto(s.getUserId(), s.getLanguage(), s.getTimezone(), s.getCurrency(),
                s.getEmailNotifications(), s.getSmsNotifications(), s.getPushNotifications(), s.getMarketingEmails(),
                s.getOrderUpdates(), s.getPriceAlerts(), s.getStockNotifications(), s.getTheme(),
                s.getItemsPerPage(), s.getAutoSaveCart(), s.getShowProductRecommendations(),
                s.getEnableLocationServices(), s.getCreatedAt(), s.getUpdatedAt());
    }

    private static PrivacySettingsDto toDto(PrivacySettings s) {
        return new PrivacySettingsDto(s.getUserId(), s.getProfileVisibility(), s.getShowEmail(), s.getShowPhone(),
                s.getAllowDataCollection(), s.getAllowAnalytics(), s.getAllowCookies(), s.getAllowMarketing(),
                s.getDataSharing(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
