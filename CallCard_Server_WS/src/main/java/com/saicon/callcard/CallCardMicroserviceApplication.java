package com.saicon.callcard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * CallCard Microservice - Spring Boot Application
 *
 * Extracted from gameserver_v3 Platform_Core/ERP subsystem.
 * Provides CallCard management functionality as a standalone microservice.
 *
 * Features:
 * - SOAP web services (Apache CXF) at /cxf/*
 * - REST endpoints (Jersey) at /rest/*
 * - JPA/Hibernate with SQL Server
 * - Multi-tenant query filtering
 * - Session-based authentication via IGameInternalService
 *
 * Service Endpoints:
 * - SOAP: http://localhost:8080/cxf/CallCardService?wsdl
 * - REST: http://localhost:8080/rest/callcards
 * - Health: http://localhost:8080/actuator/health
 *
 * Configuration:
 * - Database: application.properties (spring.datasource.*)
 * - Game Service: game.internal.service.url
 * - Server port: server.port (default: 8080)
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.saicon.callcard",
    "com.saicon.games.callcard"
})
@EntityScan(basePackages = {
    "com.saicon.games.entities",
    "com.saicon.games.salesorder.entities",
    "com.saicon.games.invoice.entities",
    "com.saicon.user.entities",
    "com.saicon.application.entities",
    "com.saicon.addressbook.entities",
    "com.saicon.generic.entities"
})
@EnableJpaRepositories(basePackages = {
    "com.saicon.games.entities"
})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CallCardMicroserviceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallCardMicroserviceApplication.class);

    public static void main(String[] args) {
        LOGGER.info("==========================================================");
        LOGGER.info("Starting CallCard Microservice");
        LOGGER.info("==========================================================");

        SpringApplication.run(CallCardMicroserviceApplication.class, args);

        LOGGER.info("==========================================================");
        LOGGER.info("CallCard Microservice started successfully");
        LOGGER.info("==========================================================");
    }
}
