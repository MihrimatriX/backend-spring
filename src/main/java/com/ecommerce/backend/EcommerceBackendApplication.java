package com.ecommerce.backend;

import com.ecommerce.backend.infrastructure.config.EcommerceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(EcommerceProperties.class)
@ComponentScan(basePackages = {
        "com.ecommerce.backend.infrastructure.web.controller",
        "com.ecommerce.backend.infrastructure.config",
        "com.ecommerce.backend.infrastructure.security",
        "com.ecommerce.backend.infrastructure.data",
        "com.ecommerce.backend.infrastructure.messaging",
        "com.ecommerce.backend.application.service",
        "com.ecommerce.backend.application.support"
})
@EntityScan(basePackages = "com.ecommerce.backend.domain.entity")
public class EcommerceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceBackendApplication.class, args);
    }
}
