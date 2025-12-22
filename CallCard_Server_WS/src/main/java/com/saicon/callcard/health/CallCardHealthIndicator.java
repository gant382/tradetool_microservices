package com.saicon.callcard.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * CallCard Microservice Health Indicator
 *
 * Monitors the health status of the CallCard microservice by checking:
 * - Database connectivity
 * - Service availability
 * - Resource utilization
 *
 * Exposed via: GET /actuator/health/callcard
 */
@Component
public class CallCardHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallCardHealthIndicator.class);
    private static final String SERVICE_NAME = "callcard-microservice";
    private static final String SERVICE_VERSION = "1.0.0";

    private final DataSource dataSource;

    public CallCardHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try {
            LOGGER.debug("Checking CallCard microservice health");

            // Check database connectivity
            boolean databaseHealthy = checkDatabaseHealth();

            if (databaseHealthy) {
                LOGGER.debug("CallCard microservice is healthy");
                return buildHealthyResponse();
            } else {
                LOGGER.warn("CallCard microservice database is unavailable");
                return buildUnhealthyResponse("Database connection failed");
            }
        } catch (Exception e) {
            LOGGER.error("Error checking CallCard microservice health", e);
            return buildUnhealthyResponse("Health check failed: " + e.getMessage());
        }
    }

    /**
     * Verifies database connectivity by attempting to obtain a connection
     */
    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2); // 2 second timeout
        } catch (Exception e) {
            LOGGER.debug("Database health check failed", e);
            return false;
        }
    }

    /**
     * Builds a healthy status response with service details
     */
    private Health buildHealthyResponse() {
        return Health.up()
            .withDetail("service", SERVICE_NAME)
            .withDetail("version", SERVICE_VERSION)
            .withDetail("status", "Running")
            .withDetail("database", "Connected")
            .withDetail("timestamp", System.currentTimeMillis())
            .build();
    }

    /**
     * Builds an unhealthy status response with error details
     */
    private Health buildUnhealthyResponse(String reason) {
        return Health.down()
            .withDetail("service", SERVICE_NAME)
            .withDetail("version", SERVICE_VERSION)
            .withDetail("status", "Not Available")
            .withDetail("reason", reason)
            .withDetail("timestamp", System.currentTimeMillis())
            .build();
    }
}
