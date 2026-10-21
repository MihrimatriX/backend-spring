package com.ecommerce.backend.infrastructure.web.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Tarayıcı giriş noktaları (.NET {@code MapGet("/")}, {@code MapGet("/health")} karşılıkları) ve kısa Swagger
 * bağlantısı. Metin cevaplar {@code Accept} başlığından bağımsız olarak {@code text/plain} döner.
 */
@Controller
public class WebNavigationController {

    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<String> root() {
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("E-Commerce API is running!");
    }

    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<String> health() {
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("OK");
    }

    @GetMapping("/swagger")
    public String swaggerRedirect() {
        return "redirect:/swagger-ui.html";
    }
}
