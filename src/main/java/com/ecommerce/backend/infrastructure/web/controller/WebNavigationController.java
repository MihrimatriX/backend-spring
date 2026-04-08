package com.ecommerce.backend.infrastructure.web.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Public entry points for browsers (root URL, short Swagger link).
 */
@Controller
public class WebNavigationController {

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> root() {
        return Map.of(
                "service", "ecommerce-backend",
                "swaggerUi", "/swagger-ui.html",
                "openApi", "/v3/api-docs");
    }

    @GetMapping("/swagger")
    public String swaggerRedirect() {
        return "redirect:/swagger-ui.html";
    }
}
