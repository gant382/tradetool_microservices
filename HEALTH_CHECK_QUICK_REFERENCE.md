# CallCard Microservice - Health Check Quick Reference

## Quick Start

Service runs on port 8080 with context path `/callcard`.

### Health Endpoints

```bash
# Overall health (includes all components)
curl http://localhost:8080/callcard/actuator/health

# Simple status (200 = UP, 503 = DOWN)
curl http://localhost:8080/callcard/api/health/status

# Detailed health information
curl http://localhost:8080/callcard/api/health/detailed

# Kubernetes liveness probe
curl http://localhost:8080/callcard/api/health/live

# Kubernetes readiness probe
curl http://localhost:8080/callcard/api/health/ready

# Service information
curl http://localhost:8080/callcard/api/health/info
```

### Metrics Endpoints

```bash
# List all metrics
curl http://localhost:8080/callcard/actuator/metrics

# Prometheus format (scrape this endpoint)
curl http://localhost:8080/callcard/actuator/prometheus

# Specific metric details
curl http://localhost:8080/callcard/actuator/metrics/callcard.api.response.time
```

## Key Metrics

| Metric | Purpose |
|--------|---------|
| `callcard.created` | Callcards created (counter) |
| `callcard.updated` | Callcards updated (counter) |
| `callcard.deleted` | Callcards deleted (counter) |
| `callcard.api.response.time` | API latency (timer) |
| `callcard.db.query.time` | DB query time (timer) |
| `callcard.api.errors` | API errors (counter) |
| `callcard.db.errors` | DB errors (counter) |

## Component Health Status

### CallCard Health (`/actuator/health/callcard`)
- **Check**: Database connectivity
- **Status**: UP (healthy) or DOWN (database unavailable)

### Database Health (`/actuator/health/db`)
- **Check**: Connection pool, driver version, response time
- **Status**: UP (healthy) or DOWN (database unavailable)

### External Services (`/actuator/health/externalServices`)
- **Check**: TALOS Core service availability
- **Status**: UP, DEGRADED (cached sessions), or DOWN

## Usage in Code

### Record Custom Metric

```java
@Service
public class CallCardService {
    private final MonitoringConfiguration monitoring;

    public CallCard create(CallCard card) {
        card = repository.save(card);
        monitoring.recordCallcardCreated();  // Record metric
        return card;
    }
}
```

### Automatic Metrics with @Monitored

```java
@Monitored
public CallCard getCallCard(String id) {
    return repository.findById(id).orElse(null);
}
```

Automatically records execution time, error rates, success/failure.

## Health Check Response Examples

### Healthy Response

```json
{
  "status": "UP",
  "components": {
    "callCard": {
      "status": "UP",
      "details": {
        "service": "callcard-microservice",
        "version": "1.0.0",
        "database": "Connected"
      }
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "Microsoft SQL Server",
        "responseTime": "15ms"
      }
    }
  }
}
```

### Degraded Response (External Services Down)

```json
{
  "status": "DEGRADED",
  "components": {
    "externalServices": {
      "status": "DEGRADED",
      "details": {
        "talosCoreService": "Unavailable",
        "note": "Service will continue with cached session data"
      }
    }
  }
}
```

## Monitoring Integration

### Prometheus Scrape Config

```yaml
scrape_configs:
  - job_name: 'callcard'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/callcard/actuator/prometheus'
```

### Kubernetes Pod Config

```yaml
livenessProbe:
  httpGet:
    path: /api/health/live
    port: 8080
readinessProbe:
  httpGet:
    path: /api/health/ready
    port: 8080
```

## Troubleshooting

| Issue | Check |
|-------|-------|
| Service shows DOWN | `/actuator/health/db` - database connectivity |
| DEGRADED status | `/actuator/health/externalServices` - TALOS Core |
| No metrics | `/actuator/metrics` - verify endpoint responds |
| High latency | `/actuator/metrics/callcard.db.query.time` - check DB |

## Files Overview

| Location | Purpose |
|----------|---------|
| `health/CallCardHealthIndicator.java` | Main service health check |
| `health/DatabaseHealthIndicator.java` | Database connectivity |
| `health/ExternalServicesHealthIndicator.java` | External service status |
| `health/HealthCheckController.java` | Custom REST endpoints |
| `config/MonitoringConfiguration.java` | Metrics initialization |
| `monitoring/MonitoringAspect.java` | Auto method instrumentation |
| `monitoring/Monitored.java` | Custom annotation |
| `config/ApplicationYml` | Management endpoint config |

## HTTP Status Codes

| Endpoint | 200 OK | 503 Down |
|----------|--------|----------|
| `/api/health/status` | UP | Service unavailable |
| `/api/health/ready` | Ready | Not ready |
| `/api/health/live` | Alive | (Always 200 if process running) |
