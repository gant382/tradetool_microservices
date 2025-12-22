package com.saicon.callcard.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Health Check REST Controller
 *
 * Provides detailed health check endpoints for the CallCard microservice.
 * Includes custom aggregations and business-level health metrics.
 *
 * Endpoints:
 * - GET /callcard/api/health/status - Overall service status
 * - GET /callcard/api/health/detailed - Detailed health metrics
 * - GET /callcard/api/health/liveness - Kubernetes liveness probe
 * - GET /callcard/api/health/readiness - Kubernetes readiness probe
 */
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckController.class);

    private final HealthEndpoint healthEndpoint;

    public HealthCheckController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    /**
     * Simple health status endpoint
     * Returns 200 OK if service is healthy, 503 if unhealthy
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        try {
            var health = healthEndpoint.health();
            var status = health.getStatus().toString();
            var response = new HashMap<String, Object>();
            response.put("status", status);
            response.put("service", "callcard-microservice");
            response.put("timestamp", System.currentTimeMillis());

            if ("UP".equals(status)) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            LOGGER.error("Error retrieving health status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                ));
        }
    }

    /**
     * Detailed health metrics endpoint
     * Returns comprehensive health information for monitoring and debugging
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedHealth() {
        try {
            var health = healthEndpoint.health();
            var response = new HashMap<String, Object>();
            response.put("status", health.getStatus().toString());
            response.put("components", health.getComponents());
            response.put("details", health.getDetails());
            response.put("timestamp", System.currentTimeMillis());

            if ("UP".equals(health.getStatus().toString())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            LOGGER.error("Error retrieving detailed health", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                ));
        }
    }

    /**
     * Kubernetes liveness probe endpoint
     * Indicates if the service process is running
     * Returns 200 OK if running, 500 if not
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> getLivenessProbe() {
        LOGGER.debug("Liveness probe called");
        return ResponseEntity.ok(Map.of(
            "status", "alive",
            "service", "callcard-microservice"
        ));
    }

    /**
     * Kubernetes readiness probe endpoint
     * Indicates if the service is ready to accept traffic
     * Returns 200 OK if ready, 503 if not ready
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> getReadinessProbe() {
        LOGGER.debug("Readiness probe called");
        try {
            var health = healthEndpoint.health();
            var response = new HashMap<String, Object>();
            response.put("status", health.getStatus().toString());
            response.put("service", "callcard-microservice");
            response.put("ready", "UP".equals(health.getStatus().toString()));
            response.put("timestamp", System.currentTimeMillis());

            if ("UP".equals(health.getStatus().toString())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            LOGGER.error("Error checking readiness", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "status", "NOT_READY",
                    "error", e.getMessage(),
                    "ready", false,
                    "timestamp", System.currentTimeMillis()
                ));
        }
    }

    /**
     * Service version and info endpoint
     * Returns basic service information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getServiceInfo() {
        LOGGER.debug("Service info requested");
        return ResponseEntity.ok(Map.of(
            "service", "callcard-microservice",
            "version", "1.0.0",
            "description", "CallCard Management Microservice",
            "buildTime", "2024-12-22"
        ));
    }
}
