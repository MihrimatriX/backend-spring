package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.AuthResponseDto;
import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.LoginRequestDto;
import com.ecommerce.backend.application.dto.RegisterRequestDto;
import com.ecommerce.backend.application.service.AuthService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Kayıt, giriş, çıkış")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Yeni kullanıcı kaydı (201)")
    public ResponseEntity<BaseResponseDto<AuthResponseDto>> register(@Valid @RequestBody RegisterRequestDto request) {
        return ApiResponses.created("User registered successfully", authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Giriş; hatalı bilgide 401 INVALID_CREDENTIALS")
    public ResponseEntity<BaseResponseDto<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request,
            HttpServletRequest http) {
        AuthResponseDto auth = authService.login(request, ApiResponses.clientIp(http),
                http.getHeader(HttpHeaders.USER_AGENT));
        return ApiResponses.ok("Login successful", auth);
    }

    @PostMapping("/logout")
    @Operation(summary = "Çıkış (JWT durumsuz; istemci token'ı siler)")
    public ResponseEntity<BaseResponseDto<String>> logout() {
        return ApiResponses.ok("User logged out successfully", "Logout successful");
    }
}
