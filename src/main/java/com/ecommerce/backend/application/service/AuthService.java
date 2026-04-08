package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import com.ecommerce.backend.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final MetricsService metricsService;

    public BaseResponseDto<AuthResponseDto> register(RegisterRequestDto registerRequest) {
        try {
            log.info("Registering new user with email: {}", registerRequest.getEmail());

            // Check if user already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return BaseResponseDto.error("Email is already taken");
            }

            // Create new user
            User user = new User();
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setPhoneNumber(registerRequest.getPhoneNumber());
            user.setAddress(registerRequest.getAddress());
            user.setCity(registerRequest.getCity());
            user.setPostalCode(registerRequest.getPostalCode());
            user.setIsEmailVerified(false);
            user.setIsActive(true);

            User savedUser = userRepository.save(user);

            // Record metrics
            metricsService.incrementUserRegistrationCounter();

            // Generate JWT token
            String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId());

            AuthResponseDto authResponse = new AuthResponseDto(
                    token, savedUser.getId(), savedUser.getEmail(),
                    savedUser.getFirstName(), savedUser.getLastName(),
                    savedUser.getIsEmailVerified());

            log.info("User registered successfully with ID: {}", savedUser.getId());
            return BaseResponseDto.success("User registered successfully", authResponse);
        } catch (Exception e) {
            log.error("Error during user registration", e);
            return BaseResponseDto.error("Registration failed: " + e.getMessage());
        }
    }

    public BaseResponseDto<AuthResponseDto> login(LoginRequestDto loginRequest) {
        try {
            log.info("Login attempt for email: {}", loginRequest.getEmail());

            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

            // Get user details
            User user = userRepository.findByEmailAndIsActiveTrue(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), user.getId());

            AuthResponseDto authResponse = new AuthResponseDto(
                    token, user.getId(), user.getEmail(),
                    user.getFirstName(), user.getLastName(),
                    user.getIsEmailVerified());

            log.info("User logged in successfully with ID: {}", user.getId());
            return BaseResponseDto.success("Login successful", authResponse);
        } catch (BadCredentialsException e) {
            log.error("Authentication failed for email: {}", loginRequest.getEmail());
            metricsService.incrementAuthenticationFailureCounter("bad_credentials");
            return BaseResponseDto.error("Invalid email or password");
        } catch (AuthenticationException e) {
            log.error("Authentication failed for email: {}", loginRequest.getEmail());
            metricsService.incrementAuthenticationFailureCounter("authentication_exception");
            return BaseResponseDto.error("Invalid email or password");
        } catch (Exception e) {
            log.error("Error during login", e);
            metricsService.incrementAuthenticationFailureCounter("general_exception");
            return BaseResponseDto.error("Login failed: " + e.getMessage());
        }
    }

    public BaseResponseDto<String> logout() {
        // JWT is stateless, so logout is handled on client side
        // In a more complex system, you might want to blacklist the token
        return BaseResponseDto.success("Logout successful", "User logged out successfully");
    }
}
