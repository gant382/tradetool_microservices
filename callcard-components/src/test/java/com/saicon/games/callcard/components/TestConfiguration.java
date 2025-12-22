package com.saicon.games.callcard.components;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration for CallCard components module.
 * Provides test beans and configurations that can be reused across all component tests.
 */
@TestConfiguration
public class TestConfiguration {

    /**
     * Provides a test instance of this configuration class for Spring context.
     * This allows Spring to recognize and include test-specific beans during test execution.
     */
    @Bean
    public TestConfiguration testConfiguration() {
        return new TestConfiguration();
    }
}
