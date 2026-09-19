package com.example.authapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point.
 *
 * {@code @SpringBootApplication} bundles three annotations:
 *   - {@code @Configuration}          : this class contributes bean definitions
 *   - {@code @EnableAutoConfiguration}: activate Spring Boot's auto-config
 *   - {@code @ComponentScan}          : scan the {@code com.example.authapp} package
 *
 * All controllers, services, repositories, security beans, and the exception
 * handler live under this base package and are picked up automatically.
 */
@SpringBootApplication
public class AuthAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthAppApplication.class, args);
    }
}
