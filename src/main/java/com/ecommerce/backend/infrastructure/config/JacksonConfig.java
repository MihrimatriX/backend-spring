package com.ecommerce.backend.infrastructure.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * JSON kuralları (docs/API_CONTRACT.md §1): tarih UTC + "Z", istek alan adları harf duyarsız.
 * Entity'lerdeki {@link LocalDateTime} değerleri UTC kabul edilir (JVM saat dilimi UTC).
 */
@Configuration
public class JacksonConfig {

    static final DateTimeFormatter UTC_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer contractJsonCustomizer() {
        return builder -> builder
                .featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .serializerByType(LocalDateTime.class, new UtcLocalDateTimeSerializer())
                .deserializerByType(LocalDateTime.class, new UtcLocalDateTimeDeserializer());
    }

    static final class UtcLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(UTC_FORMAT.format(value));
        }
    }

    /** "Z" veya ofsetli değerleri UTC'ye çevirir; bölgesiz değerleri UTC kabul eder. */
    static final class UtcLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getValueAsString();
            if (text == null || text.isBlank()) {
                return null;
            }
            String value = text.trim();
            try {
                return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
            } catch (DateTimeParseException ignored) {
                // bölge bilgisi yok
            }
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException e) {
                return (LocalDateTime) ctxt.handleWeirdStringValue(LocalDateTime.class, value,
                        "Geçersiz tarih: ISO-8601 bekleniyor");
            }
        }
    }
}
