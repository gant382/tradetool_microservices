package com.saicon.callcard.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Database Health Indicator
 *
 * Monitors the health of the SQL Server database connection with detailed metadata.
 * Checks:
 * - Connection pool availability
 * - Database version and name
 * - Driver information
 * - Response time
 *
 * Exposed via: GET /actuator/health/db
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try {
            LOGGER.debug("Checking database health");

            long startTime = System.currentTimeMillis();
            try (Connection connection = dataSource.getConnection()) {
                long responseTime = System.currentTimeMillis() - startTime;

                if (!connection.isValid(2)) {
                    return buildUnhealthyResponse("Connection validation failed");
                }

                DatabaseMetaData metaData = connection.getMetaData();
                return buildHealthyResponse(metaData, responseTime);
            }
        } catch (Exception e) {
            LOGGER.error("Database health check failed", e);
            return buildUnhealthyResponse(e.getMessage());
        }
    }

    /**
     * Builds a healthy response with database metadata
     */
    private Health buildHealthyResponse(DatabaseMetaData metaData, long responseTime) {
        try {
            return Health.up()
                .withDetail("database", metaData.getDatabaseProductName())
                .withDetail("version", metaData.getDatabaseProductVersion())
                .withDetail("driver", metaData.getDriverName())
                .withDetail("driverVersion", metaData.getDriverVersion())
                .withDetail("url", metaData.getURL())
                .withDetail("responseTime", responseTime + "ms")
                .withDetail("timestamp", System.currentTimeMillis())
                .build();
        } catch (Exception e) {
            LOGGER.error("Error extracting database metadata", e);
            return Health.up()
                .withDetail("database", "Connected")
                .withDetail("responseTime", responseTime + "ms")
                .build();
        }
    }

    /**
     * Builds an unhealthy response with error details
     */
    private Health buildUnhealthyResponse(String reason) {
        return Health.down()
            .withDetail("database", "SQL Server")
            .withDetail("status", "Unavailable")
            .withDetail("reason", reason)
            .withDetail("timestamp", System.currentTimeMillis())
            .build();
    }
}
