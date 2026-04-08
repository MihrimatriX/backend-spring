package com.ecommerce.backend.infrastructure.security;

import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public Long requireUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails ud)) {
            throw new IllegalStateException("User not authenticated");
        }
        String email = ud.getUsername();
        return userRepository.findByEmailAndIsActiveTrue(email)
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
