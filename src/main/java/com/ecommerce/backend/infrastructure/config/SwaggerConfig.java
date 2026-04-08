package com.ecommerce.backend.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("E-Commerce API")
                                                .version("v1")
                                                .description("Modern E-Commerce Backend API with Spring Boot")
                                                .contact(new Contact()
                                                                .name("AFU")
                                                                .email("afu@example.com"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8080")
                                                                .description("Development Server"),
                                                new Server()
                                                                .url("https://api.ecommerce.com")
                                                                .description("Production Server")));
        }
}
