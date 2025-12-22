package com.saicon.games.callcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * CallCard Microservice - Spring Boot Application
 *
 * Extracted from gameserver_v3 monolith as an independent microservice.
 * Provides call card management functionality via REST and SOAP endpoints.
 *
 * Architecture:
 * - callcard-ws-api: REST and SOAP service interfaces (resources and DTOs)
 * - callcard-components: Business logic components, DAOs, and utilities
 * - callcard-entity: JPA entities and domain models
 * - callcard-service: Service orchestration layer and main application
 */
@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = {
    "com.saicon.games.callcard.service",
    "com.saicon.games.callcard.components",
    "com.saicon.games.callcard.ws",
    "com.saicon.games.callcard.resources"
})
@EntityScan(basePackages = {
    "com.saicon.games.callcard.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.saicon.games.callcard.components"
})
public class CallCardApplication {

    /**
     * Main entry point for the CallCard microservice.
     * Initializes Spring Boot application context and starts the embedded server.
     *
     * Configuration is loaded from application.yml in src/main/resources/
     * Database configuration is via JNDI datasource or properties-based datasource
     * JPA/Hibernate configuration is auto-configured by Spring Boot
     *
     * @param args Command line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(CallCardApplication.class, args);
    }
}
