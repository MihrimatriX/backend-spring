package com.ecommerce.backend.infrastructure.config;

import com.ecommerce.backend.infrastructure.security.JsonAccessDeniedHandler;
import com.ecommerce.backend.infrastructure.security.JsonAuthenticationEntryPoint;
import com.ecommerce.backend.infrastructure.security.JwtAuthenticationFilter;
import com.ecommerce.backend.infrastructure.security.RateLimitingFilter;
import com.ecommerce.backend.infrastructure.security.RequestLoggingFilter;
import com.ecommerce.backend.infrastructure.security.SecurityHeadersFilter;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Yetki kuralları docs/API_CONTRACT.md §4'teki "Yetki" sütunlarının birebir karşılığıdır.
 * Kural sırası önemlidir: yönetici kuralları anonim GET kurallarından önce gelir.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final RateLimitingFilter rateLimitingFilter;
    private final SecurityHeadersFilter securityHeadersFilter;
    private final RequestLoggingFilter requestLoggingFilter;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    private final JsonAccessDeniedHandler jsonAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // Güvenlik başlıkları SecurityHeadersFilter'da; Spring yalnızca cache başlıklarını ekler.
                .headers(headers -> headers
                        .contentTypeOptions(c -> c.disable())
                        .xssProtection(x -> x.disable())
                        .frameOptions(f -> f.disable())
                        .httpStrictTransportSecurity(h -> h.disable()))
                .authorizeHttpRequests(auth -> auth
                        // Altyapı
                        .requestMatchers("/error", "/health", "/actuator/**", "/h2-console/**").permitAll()
                        .requestMatchers("/swagger", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/test/**", "/api/health", "/api/health/**", "/api/metrics",
                                "/api/metrics/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // Yönetici
                        .requestMatchers(HttpMethod.GET, "/api/order/admin").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/order/*/status").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/review/admin").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/notification").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/notification/admin/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/helpsupport/articles").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/helpsupport/tickets/admin").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/product", "/api/category", "/api/subcategory",
                                "/api/campaign").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/product/**", "/api/category/**", "/api/subcategory/**",
                                "/api/campaign/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/product/**", "/api/category/**",
                                "/api/subcategory/**", "/api/campaign/**").hasRole(ADMIN)
                        // Anonim okuma
                        .requestMatchers(HttpMethod.GET, "/api/product", "/api/product/**", "/api/category",
                                "/api/category/**", "/api/subcategory", "/api/subcategory/**", "/api/campaign",
                                "/api/campaign/**", "/api/review/**", "/api/helpsupport/articles",
                                "/api/helpsupport/articles/**", "/api/helpsupport/faqs").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/helpsupport/contact").permitAll()
                        // Geri kalan tüm API uçları oturum ister
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonAuthenticationEntryPoint)
                        .accessDeniedHandler(jsonAccessDeniedHandler))
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(securityHeadersFilter, RequestLoggingFilter.class)
                .addFilterAfter(rateLimitingFilter, SecurityHeadersFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RateLimitingFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Filtreler yalnızca güvenlik zincirinde çalışsın; Spring Boot'un ayrıca servlet filtresi
    // olarak kaydetmesini engelle.
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter f) {
        return disabled(f);
    }

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitFilterRegistration(RateLimitingFilter f) {
        return disabled(f);
    }

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> headersFilterRegistration(SecurityHeadersFilter f) {
        return disabled(f);
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> loggingFilterRegistration(RequestLoggingFilter f) {
        return disabled(f);
    }

    private static <T extends Filter> FilterRegistrationBean<T> disabled(T filter) {
        FilterRegistrationBean<T> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
