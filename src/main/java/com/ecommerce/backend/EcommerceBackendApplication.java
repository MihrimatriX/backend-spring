package com.ecommerce.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class EcommerceBackendApplication {

    public static void main(String[] args) {
        // Sözleşme: tüm zamanlar UTC saklanır ve "Z" sonekiyle yazılır (docs/API_CONTRACT.md §1).
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(EcommerceBackendApplication.class, args);
    }
}
