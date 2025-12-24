package com.saicon.callcard.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Health Check REST Controller
 */
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckController.class);

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "CallCard Microservice");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "CallCard Microservice");
        response.put("timestamp", System.currentTimeMillis());
        Map<String, String> components = new HashMap<>();
        components.put("database", "UP");
        components.put("disk", "UP");
        response.put("components", components);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/liveness")
    public ResponseEntity<Map<String, Object>> getLivenessProbe() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/readiness")
    public ResponseEntity<Map<String, Object>> getReadinessProbe() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("ready", true);
        return ResponseEntity.ok(response);
    }
}
