package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<BaseResponseDto<AuthResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto registerRequest) {
        log.info("Registration request for email: {}", registerRequest.getEmail());
        BaseResponseDto<AuthResponseDto> response = authService.register(registerRequest);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<BaseResponseDto<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        log.info("Login request for email: {}", loginRequest.getEmail());
        BaseResponseDto<AuthResponseDto> response = authService.login(loginRequest);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user (JWT is stateless)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    public ResponseEntity<BaseResponseDto<String>> logout() {
        log.info("Logout request");
        BaseResponseDto<String> response = authService.logout();
        return ResponseEntity.ok(response);
    }
}
