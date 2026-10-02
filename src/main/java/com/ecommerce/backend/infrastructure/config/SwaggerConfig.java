package com.ecommerce.backend.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI: {@code /swagger-ui.html} (veya {@code /swagger}). Sunucu adresi istekten alınır;
 * "Authorize" ile {@code Bearer <token>} girilebilir.
 */
@Configuration
public class SwaggerConfig {

    private static final String BEARER = "Bearer";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce API")
                        .version("v1.0.0")
                        .description("E-ticaret REST API (Spring Boot). Sözleşme backend-dotnet ile birebir aynıdır: "
                                + "docs/API_CONTRACT.md")
                        .contact(new Contact().name("AFU").email("afu@example.com"))
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("POST /api/auth/login cevabındaki token")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
