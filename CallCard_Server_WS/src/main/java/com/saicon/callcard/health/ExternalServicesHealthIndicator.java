package com.saicon.callcard.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.net.URL;
import java.net.URLConnection;

/**
 * External Services Health Indicator
 *
 * Monitors the health of external services required by CallCard microservice:
 * - TALOS Core session validation service
 * - Response times and availability status
 *
 * Exposed via: GET /actuator/health/external
 */
@Component
public class ExternalServicesHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalServicesHealthIndicator.class);
    private static final int CONNECTION_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;

    @Value("${callcard.talos-core.session-validation-url:http://localhost:8080/Game_Server_WS/cxf/GAMEInternalService}")
    private String talosCoreUrl;

    @Override
    public Health health() {
        try {
            LOGGER.debug("Checking external services health");

            boolean talosCoreHealthy = checkServiceAvailability(talosCoreUrl, "TALOS Core");

            if (talosCoreHealthy) {
                return buildHealthyResponse();
            } else {
                return buildDegradedResponse("TALOS Core service unavailable");
            }
        } catch (Exception e) {
            LOGGER.error("Error checking external services health", e);
            return buildDegradedResponse(e.getMessage());
        }
    }

    /**
     * Checks if an external service is available by attempting a connection
     */
    private boolean checkServiceAvailability(String serviceUrl, String serviceName) {
        try {
            LOGGER.debug("Checking {} at {}", serviceName, serviceUrl);

            long startTime = System.currentTimeMillis();
            URL url = new URL(serviceUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.getInputStream();

            long responseTime = System.currentTimeMillis() - startTime;
            LOGGER.debug("{} is available ({}ms)", serviceName, responseTime);
            return true;
        } catch (Exception e) {
            LOGGER.warn("{} is unavailable: {}", serviceName, e.getMessage());
            return false;
        }
    }

    /**
     * Builds a healthy response indicating all external services are available
     */
    private Health buildHealthyResponse() {
        return Health.up()
            .withDetail("externalServices", "Available")
            .withDetail("talosCoreService", "Connected")
            .withDetail("talosCoreUrl", talosCoreUrl)
            .withDetail("timestamp", System.currentTimeMillis())
            .build();
    }

    /**
     * Builds a degraded response when external services are unavailable
     * Service continues to operate but may have limited functionality
     */
    private Health buildDegradedResponse(String reason) {
        return Health.unknown()
            .withDetail("externalServices", "Partially Available")
            .withDetail("talosCoreService", "Unavailable")
            .withDetail("talosCoreUrl", talosCoreUrl)
            .withDetail("reason", reason)
            .withDetail("note", "Service will continue with cached session data")
            .withDetail("timestamp", System.currentTimeMillis())
            .build();
    }
}
