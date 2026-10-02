package com.ecommerce.backend.infrastructure.data;

import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sözleşmedeki demo hesaplarını (docs/API_CONTRACT.md §2.1) her profilde idempotent olarak oluşturur;
 * e-posta zaten varsa dokunmaz. .NET karşılığı: {@code DataSeeder.EnsureStandardAccountsAsync}.
 */
@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class StandardAccountsInitializer implements ApplicationRunner {

    private record Account(String email, String password, String firstName, String lastName, boolean verified) {
    }

    private static final List<Account> ACCOUNTS = List.of(
            new Account("admin@example.com", "admin123", "Admin", "User", true),
            new Account("manager@shop.demo", "Manager123!", "Panel", "Yönetici", true),
            new Account("user1@example.com", "user123", "Ahmet", "Yılmaz", true),
            new Account("user2@example.com", "user123", "Ayşe", "Demir", true),
            new Account("user3@example.com", "user123", "Mehmet", "Kaya", false),
            new Account("user4@example.com", "user123", "Fatma", "Özkan", true),
            new Account("support@shop.demo", "Support123!", "Destek", "Uzman", true),
            new Account("demo.buyer@shop.local", "Buyer123!", "Demo", "Alıcı", true),
            new Account("staff@shop.demo", "Staff123!", "Mağaza", "Personel", true));

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int created = 0;
        for (Account a : ACCOUNTS) {
            if (userRepository.existsByEmailIgnoreCase(a.email())) {
                continue;
            }
            User u = new User();
            u.setEmail(a.email());
            u.setPassword(passwordEncoder.encode(a.password()));
            u.setFirstName(a.firstName());
            u.setLastName(a.lastName());
            u.setIsEmailVerified(a.verified());
            u.setIsActive(true);
            userRepository.save(u);
            created++;
        }
        if (created > 0) {
            log.info("{} standart demo hesabı oluşturuldu", created);
        }
    }
}
