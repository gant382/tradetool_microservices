# CallCard Microservice - Monitoring and Health Check Guide

## Overview

The CallCard microservice includes comprehensive monitoring and health check endpoints for production-grade observability. The monitoring stack includes Spring Boot Actuator, Micrometer metrics, Prometheus integration, and custom health indicators.

## Architecture

### Components

1. **Health Indicators** - Custom Spring Boot health checks for service, database, and external services
2. **Metrics** - Micrometer-based metrics collection and Prometheus export
3. **Health Check Controller** - Custom REST endpoints for detailed health information
4. **Monitoring Aspect** - AOP-based automatic method instrumentation
5. **Monitoring Configuration** - Centralized metrics setup and management

## Health Check Endpoints

### Built-in Spring Boot Actuator Endpoints

All endpoints are prefixed with `/callcard/actuator/` in the application.

#### GET /callcard/actuator/health
Overall service health status. Returns JSON with component details.

```bash
curl http://localhost:8080/callcard/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "callCard": {
      "status": "UP",
      "details": {
        "service": "callcard-microservice",
        "version": "1.0.0",
        "status": "Running",
        "database": "Connected",
        "timestamp": 1703251200000
      }
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "Microsoft SQL Server",
        "version": "15.00.2000",
        "driver": "Microsoft SQL Server JDBC Driver",
        "driverVersion": "12.4.2",
        "url": "jdbc:sqlserver://...",
        "responseTime": "15ms"
      }
    },
    "externalServices": {
      "status": "UP",
      "details": {
        "externalServices": "Available",
        "talosCoreService": "Connected",
        "talosCoreUrl": "http://localhost:8080/Game_Server_WS/cxf/GAMEInternalService"
      }
    }
  }
}
```

#### GET /callcard/actuator/health/callcard
CallCard service-specific health check.

```bash
curl http://localhost:8080/callcard/actuator/health/callcard
```

#### GET /callcard/actuator/health/db
Database connectivity and metadata information.

```bash
curl http://localhost:8080/callcard/actuator/health/db
```

#### GET /callcard/actuator/health/externalServices
Status of external service dependencies (TALOS Core).

```bash
curl http://localhost:8080/callcard/actuator/health/externalServices
```

### Custom Health Check Endpoints

Custom endpoints provide additional health check capabilities.

#### GET /callcard/api/health/status
Simple health status (useful for monitoring alerts).

```bash
curl http://localhost:8080/callcard/api/health/status
```

Response (200 OK):
```json
{
  "status": "UP",
  "service": "callcard-microservice",
  "timestamp": 1703251200000
}
```

Response when unhealthy (503 Service Unavailable):
```json
{
  "status": "DOWN",
  "service": "callcard-microservice",
  "timestamp": 1703251200000
}
```

#### GET /callcard/api/health/detailed
Comprehensive health metrics with component details.

```bash
curl http://localhost:8080/callcard/api/health/detailed
```

#### GET /callcard/api/health/live
Kubernetes liveness probe endpoint.

```bash
curl http://localhost:8080/callcard/api/health/live
```

Response (200 OK):
```json
{
  "status": "alive",
  "service": "callcard-microservice"
}
```

#### GET /callcard/api/health/ready
Kubernetes readiness probe endpoint.

```bash
curl http://localhost:8080/callcard/api/health/ready
```

Response when ready (200 OK):
```json
{
  "status": "UP",
  "service": "callcard-microservice",
  "ready": true,
  "timestamp": 1703251200000
}
```

Response when not ready (503 Service Unavailable):
```json
{
  "status": "NOT_READY",
  "error": "Database connection failed",
  "ready": false,
  "timestamp": 1703251200000
}
```

#### GET /callcard/api/health/info
Service information and version details.

```bash
curl http://localhost:8080/callcard/api/health/info
```

Response:
```json
{
  "service": "callcard-microservice",
  "version": "1.0.0",
  "description": "CallCard Management Microservice",
  "buildTime": "2024-12-22"
}
```

## Metrics Endpoints

### GET /callcard/actuator/metrics
List all available metrics.

```bash
curl http://localhost:8080/callcard/actuator/metrics
```

Response:
```json
{
  "names": [
    "callcard.api.errors",
    "callcard.api.response.time",
    "callcard.cache.size",
    "callcard.created",
    "callcard.db.errors",
    "callcard.db.query.time",
    "callcard.external.service.time",
    "callcard.updated",
    ...
  ]
}
```

### GET /callcard/actuator/metrics/{metric-name}
Get details for a specific metric.

```bash
curl http://localhost:8080/callcard/actuator/metrics/callcard.api.response.time
```

Response:
```json
{
  "name": "callcard.api.response.time",
  "description": "API endpoint response time",
  "baseUnit": "milliseconds",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1234
    },
    {
      "statistic": "TOTAL",
      "value": 4567890
    },
    {
      "statistic": "MAX",
      "value": 5000
    }
  ]
}
```

### GET /callcard/actuator/prometheus
Prometheus-format metrics export.

```bash
curl http://localhost:8080/callcard/actuator/prometheus
```

Response:
```
# HELP callcard_api_response_time API endpoint response time
# TYPE callcard_api_response_time summary
callcard_api_response_time_count 1234
callcard_api_response_time_sum 4567890
callcard_api_response_time_max 5000

# HELP callcard_api_errors Total API errors
# TYPE callcard_api_errors counter
callcard_api_errors_total 5
```

## Available Metrics

### Business Metrics (Counters)

| Metric | Description | Type |
|--------|-------------|------|
| `callcard.created` | Total number of callcards created | Counter |
| `callcard.updated` | Total number of callcards updated | Counter |
| `callcard.deleted` | Total number of callcards deleted | Counter |
| `callcard.viewed` | Total number of callcard views | Counter |

### Technical Metrics

| Metric | Description | Type |
|--------|-------------|------|
| `callcard.api.response.time` | API endpoint response time | Timer |
| `callcard.db.query.time` | Database query execution time | Timer |
| `callcard.external.service.time` | External service call duration | Timer |
| `callcard.api.errors` | Total API errors | Counter |
| `callcard.db.errors` | Total database errors | Counter |
| `callcard.active.sessions` | Number of active sessions | Gauge |
| `callcard.cache.size` | Current cache size | Gauge |

### Standard Spring Boot Metrics

| Metric | Description |
|--------|-------------|
| `http.server.requests` | HTTP request metrics |
| `jvm.memory.used` | JVM memory usage |
| `jvm.threads.live` | Number of live threads |
| `process.uptime` | Process uptime |
| `system.cpu.usage` | System CPU usage |

## Using Metrics in Code

### Recording Custom Metrics

Inject `MonitoringConfiguration` into your service:

```java
@Service
public class CallCardService {

    private final MonitoringConfiguration monitoring;

    public CallCardService(MonitoringConfiguration monitoring) {
        this.monitoring = monitoring;
    }

    public CallCard createCallCard(CallCard callCard) {
        // Create the callcard
        callCard = repository.save(callCard);

        // Record the metric
        monitoring.recordCallcardCreated();

        return callCard;
    }
}
```

### Using the @Monitored Annotation

Annotate methods for automatic metric collection:

```java
@Service
public class CallCardService {

    @Monitored(description = "Fetch callcard by ID")
    public CallCard getCallCardById(String id) {
        return repository.findById(id).orElse(null);
    }

    @Monitored
    public List<CallCard> getAllCallCards() {
        return repository.findAll();
    }
}
```

The `@Monitored` annotation automatically records:
- Method execution time
- Success/failure rates
- Error types and counts

### Recording Timed Operations

For complex operations spanning multiple steps:

```java
public void complexOperation() {
    Timer.Sample sample = monitoring.recordApiResponseTime();

    try {
        // Perform operation
        doSomething();
    } finally {
        monitoring.stopRecordingApiResponseTime(sample);
    }
}
```

## Health Indicator Details

### CallCardHealthIndicator
**Location**: `com.saicon.callcard.health.CallCardHealthIndicator`

Monitors:
- Database connectivity
- Service availability
- Returns UP or DOWN status

**Check Method**: Database connection validation (2-second timeout)

### DatabaseHealthIndicator
**Location**: `com.saicon.callcard.health.DatabaseHealthIndicator`

Monitors:
- Connection pool availability
- Database version and name
- Driver information
- Response time

**Check Method**: SQL Server metadata queries

### ExternalServicesHealthIndicator
**Location**: `com.saicon.callcard.health.ExternalServicesHealthIndicator`

Monitors:
- TALOS Core session validation service
- Response times
- Service availability

**Status Values**:
- `UP` - All services available
- `DEGRADED` - Service available but TALOS Core unavailable (graceful degradation with cached sessions)
- `DOWN` - Critical failure

## Configuration

### application.yml Settings

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,loggers
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      show-components: always
      probes:
        enabled: true
  health:
    circuitbreaker:
      enabled: true
    db:
      enabled: true
    defaults:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
    tags:
      application: callcard-microservice
```

### Custom Configuration

Enable/disable specific health indicators by setting:

```yaml
management:
  health:
    callCard:
      enabled: true
    db:
      enabled: true
    externalServices:
      enabled: true
```

## Prometheus Integration

### Scrape Configuration

For Prometheus monitoring, add to `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'callcard-microservice'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/callcard/actuator/prometheus'
    scrape_interval: 15s
```

### Sample Prometheus Queries

```promql
# API response time P99
histogram_quantile(0.99, callcard_api_response_time)

# Error rate
rate(callcard_api_errors_total[5m])

# Database query performance
histogram_quantile(0.95, callcard_db_query_time)

# Business metric: callcards created
rate(callcard_created_total[1h])
```

## Kubernetes Integration

### Liveness Probe

```yaml
livenessProbe:
  httpGet:
    path: /api/health/live
    port: 8080
    scheme: HTTP
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

### Readiness Probe

```yaml
readinessProbe:
  httpGet:
    path: /api/health/ready
    port: 8080
    scheme: HTTP
  initialDelaySeconds: 20
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 2
```

## Monitoring Best Practices

### 1. Health Check Frequency
- **Liveness probe**: Every 10 seconds (loose check)
- **Readiness probe**: Every 5 seconds (strict check)
- **Monitoring scrape**: Every 15-30 seconds

### 2. Alert Rules

Create Prometheus alert rules:

```yaml
groups:
  - name: callcard_microservice
    rules:
      - alert: CallCardServiceDown
        expr: callcard_service_status == 0
        for: 2m
        annotations:
          summary: "CallCard service is down"

      - alert: DatabaseError
        expr: rate(callcard_db_errors_total[5m]) > 0.01
        annotations:
          summary: "Database errors detected"

      - alert: SlowApiResponse
        expr: histogram_quantile(0.95, callcard_api_response_time) > 5000
        annotations:
          summary: "API response time exceeding threshold"
```

### 3. Metrics Retention
- Configure Prometheus retention policy for cost optimization
- Default: 15 days of metrics

### 4. Dashboard Creation
Use Grafana to visualize metrics from Prometheus:
- Request rate and latency
- Error rates
- Database performance
- External service health
- Business metrics (callcards created/updated/deleted)

## Troubleshooting

### Health Check Returns DOWN

1. **Check database connectivity**
   ```bash
   curl http://localhost:8080/callcard/actuator/health/db
   ```

2. **Check external services**
   ```bash
   curl http://localhost:8080/callcard/actuator/health/externalServices
   ```

3. **Review application logs**
   ```
   grep -E "ERROR|health|database" logs/application.log
   ```

### No Metrics Appearing

1. **Verify Actuator is enabled**
   ```bash
   curl http://localhost:8080/callcard/actuator
   ```

2. **Check Prometheus endpoint**
   ```bash
   curl http://localhost:8080/callcard/actuator/prometheus
   ```

3. **Verify @Monitored annotations are applied**
   - Methods must be in Spring-managed beans
   - Aspect must be enabled (check Spring logs)

### High Response Times

1. **Check database metrics**
   ```bash
   curl 'http://localhost:8080/callcard/actuator/metrics/callcard.db.query.time'
   ```

2. **Analyze slow queries in SQL Server**
   - Check query execution plans
   - Review indexes on frequently queried tables

3. **Check external service latency**
   ```bash
   curl 'http://localhost:8080/callcard/actuator/metrics/callcard.external.service.time'
   ```

## Files Created

1. **Health Indicators**
   - `CallCardHealthIndicator.java` - Main service health
   - `DatabaseHealthIndicator.java` - Database connectivity
   - `ExternalServicesHealthIndicator.java` - External service status

2. **Custom Endpoints**
   - `HealthCheckController.java` - REST endpoints for health checks

3. **Monitoring**
   - `MonitoringConfiguration.java` - Metrics setup
   - `MonitoringAspect.java` - AOP-based instrumentation
   - `Monitored.java` - Custom annotation

4. **Configuration**
   - `application.yml` - Updated with management endpoints

5. **Dependencies**
   - `pom.xml` - Added Micrometer Prometheus registry

## Additional Resources

- [Spring Boot Actuator Documentation](https://spring.io/guides/gs/actuator-service/)
- [Micrometer Documentation](https://micrometer.io/)
- [Prometheus Documentation](https://prometheus.io/)
- [Kubernetes Probes Documentation](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
