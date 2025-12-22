package com.saicon.games.callcard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test configuration for CallCard microservice integration tests.
 * Provides minimal Spring context with essential beans for testing.
 */
@TestConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
    "com.saicon.games.callcard"
})
@Profile("test")
public class TestApplicationConfiguration {

    /**
     * Provides ObjectMapper for JSON serialization/deserialization in tests.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
