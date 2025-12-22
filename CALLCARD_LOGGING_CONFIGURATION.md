# CallCard Microservice - Comprehensive Logging Configuration

## Overview

This document provides complete logging configuration for the CallCard microservice, supporting development, testing, and production environments.

## Configuration Files

### Main Configuration: `logback.xml`
**Location:** `CallCard_Server_WS/src/main/resources/logback.xml`

This is the primary logging configuration file that should be updated with the comprehensive configuration below.

## Comprehensive Logging Configuration

### Appenders

#### 1. Console Appender
- **Purpose:** Real-time log output to console
- **Pattern:** Timestamp, Thread, Level, Logger, Transaction ID, Message
- **Encoding:** UTF-8

#### 2. File Appender - Application Logs
- **File:** `logs/callcard-microservice.log`
- **Rolling Policy:** Time-based (daily) + Size-based (100MB per file)
- **Compression:** GZIP
- **Retention:** 30 days or 3GB total
- **Pattern:** Standard pattern with transaction ID

#### 3. Error File Appender
- **File:** `logs/callcard-microservice-error.log`
- **Filter:** Only ERROR level and above
- **Rolling Policy:** Time-based (daily) + Size-based (100MB per file)
- **Compression:** GZIP
- **Retention:** 30 days or 2GB total
- **Pattern:** Detailed pattern including file name and line number

#### 4. Database Access Appender
- **File:** `logs/callcard-db-access.log`
- **Purpose:** Database queries, parameters, and statistics
- **Rolling Policy:** Time-based (daily) + Size-based (100MB per file)
- **Retention:** 15 days or 1GB total

#### 5. Service Integration Appender
- **File:** `logs/callcard-service-integration.log`
- **Purpose:** CXF SOAP, Jersey REST, and external service calls
- **Rolling Policy:** Time-based (daily) + Size-based (100MB per file)
- **Retention:** 15 days or 1GB total

#### 6. Performance Appender
- **File:** `logs/callcard-performance.log`
- **Purpose:** Resilience4j metrics, Hibernate statistics, circuit breaker events
- **Rolling Policy:** Time-based (daily) + Size-based (100MB per file)
- **Retention:** 10 days or 500MB total

### Async Appenders

All file appenders use async wrappers for performance optimization:

1. **ASYNC_FILE** - For application logs (queue size: 512)
2. **ASYNC_ERROR_FILE** - For error logs with caller data (queue size: 512)
3. **ASYNC_DB_FILE** - For database logs (queue size: 256)
4. **ASYNC_SERVICE_FILE** - For service integration logs (queue size: 256)
5. **ASYNC_PERF_FILE** - For performance logs (queue size: 512)

**Async Configuration:**
- Discarding Threshold: 0 (no event loss)
- Include Caller Data: false (except for errors)
- Non-blocking: Uses ring buffer for high-throughput scenarios

## Logger Configuration

### CallCard Application Loggers

| Logger | Level | Output | Purpose |
|--------|-------|--------|---------|
| `com.saicon.games.callcard` | DEBUG | ASYNC_FILE | Main application package |
| `com.saicon.callcard` | DEBUG | ASYNC_FILE | Legacy callcard package |
| `com.saicon.games.callcard.service` | INFO | SERVICE_FILE + CONSOLE | Service layer operations |
| `com.saicon.games.callcard.component` | DEBUG | FILE + CONSOLE | Component-level logic |
| `com.saicon.games.callcard.entity` | INFO | DB_ACCESS_FILE | JPA entity operations |
| `com.saicon.games.callcard.ws.response` | DEBUG | FILE | Response handling |
| `com.saicon.games.callcard.exception` | WARN | ERROR_FILE + CONSOLE | Exception handlers |
| `com.saicon.games.callcard.util` | DEBUG | FILE | Utility functions |

### Hibernate/JPA Loggers

| Logger | Level | Output | Purpose |
|--------|-------|--------|---------|
| `org.hibernate.SQL` | DEBUG | DB_ACCESS_FILE + CONSOLE | SQL statements |
| `org.hibernate.type.descriptor.sql` | TRACE | DB_ACCESS_FILE | Parameter binding |
| `org.hibernate.stat` | INFO | PERF_FILE | Query statistics |
| `org.hibernate.jpa` | INFO | Console | JPA bootstrapping |
| `com.zaxxer.hikari` | INFO | Console | Connection pool status |
| `com.zaxxer.hikari.HikariConfig` | DEBUG | Console | Pool configuration |

### Spring Framework Loggers

| Logger | Level | Purpose |
|--------|-------|---------|
| `org.springframework.core` | INFO | Core container |
| `org.springframework.beans` | INFO | Bean creation and wiring |
| `org.springframework.web` | INFO | Web request handling |
| `org.springframework.web.servlet.mvc` | INFO | MVC controller invocation |
| `org.springframework.transaction` | DEBUG | Transaction management |
| `org.springframework.data` | INFO | Spring Data operations |
| `org.springframework.boot` | INFO | Boot startup and configuration |
| `org.springframework.boot.actuate` | INFO | Health checks and metrics |

### CXF/SOAP Web Services

| Logger | Level | Output | Purpose |
|--------|-------|--------|---------|
| `org.apache.cxf` | INFO | SERVICE_FILE + CONSOLE | Main CXF framework |
| `org.apache.cxf.services` | INFO | SERVICE_FILE | Service endpoint operations |
| `org.apache.cxf.phase.PhaseInterceptorChain` | INFO | Console | Message processing phases |
| `org.apache.cxf.binding.soap` | INFO | Console | SOAP binding |
| `org.apache.cxf.transport` | INFO | Console | HTTP/network transport |
| `org.apache.cxf.attachment` | INFO | Console | SOAP attachment handling |

### Jersey/REST Web Services

| Logger | Level | Output | Purpose |
|--------|-------|--------|---------|
| `org.glassfish.jersey` | INFO | SERVICE_FILE + CONSOLE | Main Jersey framework |
| `org.glassfish.jersey.server` | INFO | Console | Server-side components |
| `org.glassfish.jersey.server.spring` | INFO | Console | Spring integration |
| `org.glassfish.jersey.media` | INFO | Console | Media type handling |

### Resilience4j Circuit Breaker

| Logger | Level | Output | Purpose |
|--------|-------|--------|---------|
| `io.github.resilience4j.circuitbreaker` | INFO | PERF_FILE + CONSOLE | Circuit breaker state changes |
| `io.github.resilience4j.retry` | INFO | PERF_FILE | Retry attempts |
| `io.github.resilience4j.ratelimiter` | INFO | PERF_FILE | Rate limiting events |

### Third Party Libraries

| Logger | Level | Purpose |
|--------|-------|---------|
| `com.fasterxml.jackson` | INFO | JSON serialization/deserialization |
| `org.hibernate.validator` | INFO | Bean validation |

## Profile-Specific Configuration

### Development Profile (`dev`)

**Activation:** Default profile when not specified

**Logger Overrides:**
```
com.saicon.games.callcard: DEBUG
org.springframework: DEBUG
org.springframework.web: DEBUG
```

**Characteristics:**
- Verbose logging for all application code
- Detailed Spring Framework logging
- All appenders active
- Useful for debugging during development

### Test Profile (`test`)

**Activation:** `spring.profiles.active=test`

**Logger Overrides:**
```
com.saicon.games.callcard: DEBUG
org.springframework: INFO
org.hibernate.SQL: DEBUG
```

**Characteristics:**
- Moderate logging level
- SQL statements visible for testing
- Reduced noise from Spring Framework
- Good balance for test execution

### Production Profile (`prod`)

**Activation:** `spring.profiles.active=prod`

**Logger Overrides:**
```
com.saicon.games.callcard: INFO
org.springframework: WARN
org.apache.cxf: WARN
org.glassfish.jersey: WARN
org.hibernate.SQL: WARN
```

**Characteristics:**
- Minimal logging overhead
- Only significant events logged
- Reduced I/O and disk usage
- Performance optimized

## Log Levels and Semantics

| Level | Usage |
|-------|-------|
| TRACE | Detailed diagnostic information (parameter values, variable states) |
| DEBUG | Low-level diagnostic information (method entry/exit, data flow) |
| INFO | Informational messages about application flow (startup, shutdown, important events) |
| WARN | Warning messages about potentially harmful situations |
| ERROR | Error messages about serious problems |
| FATAL | Fatal errors that cause application termination |

## Pattern Details

### Standard Pattern
```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{transactionId}] - %msg%n
```

Components:
- `%d{yyyy-MM-dd HH:mm:ss.SSS}` - Timestamp with milliseconds
- `[%thread]` - Thread name
- `%-5level` - Log level (left-aligned, 5 chars minimum)
- `%logger{36}` - Logger name (abbreviated to 36 chars)
- `[%X{transactionId}]` - Transaction ID from MDC
- `%msg%n` - Message and newline

### Detailed Pattern (Errors)
```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{transactionId}] [%file:%line] - %msg%n
```

Additional components:
- `[%file:%line]` - Source file name and line number

## Rolling Policy

### Time-Based Rolling
- **Trigger:** Daily at midnight
- **Format:** `filename.yyyy-MM-dd.N.log.gz`
- **N:** Sequential index when multiple files created on same day

### Size-Based Rolling
- **Trigger:** When file reaches max size
- **Combines with:** Time-based policy for dual triggering

### Example Filename Evolution
```
callcard-microservice.log (current)
callcard-microservice.2025-12-22.1.log.gz (day 1, first rollover)
callcard-microservice.2025-12-22.2.log.gz (day 1, second rollover)
callcard-microservice.2025-12-23.1.log.gz (day 2, first rollover)
```

## Properties Configuration

### Environment-Specific Properties

In `application.yml`:

```yaml
logging:
  level:
    root: INFO
    com.saicon.games.callcard: DEBUG
    org.hibernate.SQL: DEBUG
    org.apache.cxf: INFO
    org.glassfish.jersey: INFO
```

### System Properties

Can be overridden at runtime:

```bash
java -DLOG_PATH=/var/log/callcard \
     -DLOG_FILE=callcard-service \
     -Dspring.profiles.active=prod \
     -jar CallCard_Server_WS.war
```

### Environment Variables

Maven filtering supports:

```bash
export LOG_PATH=/var/log/callcard
mvn clean install
```

## Performance Optimization

### Async Appenders

Benefits:
- Non-blocking logging (main threads don't wait for I/O)
- Ring buffer for lock-free operation
- Automatic thread pool for async writes

Configuration:
- Queue Size: 256-512 events per appender
- Discarding: 0 (never drop events)
- Caller Data: true only for error logs (expensive operation)

### Best Practices

1. **Use appropriate log levels**
   - DEBUG: Development and troubleshooting only
   - INFO: Significant application events
   - WARN: Potential issues
   - ERROR: Application failures

2. **Avoid logging in hot paths**
   - Expensive operations in frequently-called code
   - Consider guard clauses: `if (logger.isDebugEnabled())`

3. **Use MDC for correlation**
   - Add transaction ID to MDC for distributed tracing
   - Example: `MDC.put("transactionId", txnId)`

4. **Monitor log file growth**
   - Configure appropriate retention policies
   - Monitor disk space usage
   - Archive old logs regularly

## Troubleshooting

### Logs Not Appearing

**Check:**
1. Log level configuration is not WARN/ERROR when expecting DEBUG
2. Logger name matches configured logger (check package names)
3. Async appender queue not overflowing
4. File permissions allow writing to log directory

### Performance Issues

**Investigate:**
1. Log level too verbose (too many DEBUG statements)
2. Async appender queue too small (increase queueSize)
3. Disk I/O bottleneck (check hardware performance)
4. File rotation happening too frequently

### Disk Space Problems

**Solutions:**
1. Reduce retention period (maxHistory)
2. Reduce maximum total size (totalSizeCap)
3. Reduce maximum file size (maxFileSize)
4. Archive logs to external storage
5. Compress older log files

## Implementation Steps

### 1. Update logback.xml

Replace the current configuration with the comprehensive version provided in this document.

### 2. Create Profile-Specific Files (Optional)

Create separate logback files for each environment:
- `logback-dev.xml`
- `logback-test.xml`
- `logback-prod.xml`

### 3. Update application.yml

Ensure logging configuration matches logback.xml levels.

### 4. Verify Installation

```bash
# Check logs are created
ls -la logs/

# Monitor log output
tail -f logs/callcard-microservice.log

# Check for errors
grep ERROR logs/callcard-microservice-error.log
```

## Related Documentation

- Logback Official Documentation: http://logback.qos.ch/
- SLF4J (Simple Logging Facade): http://www.slf4j.org/
- Spring Boot Logging: https://spring.io/guides/gs/logging/

## Configuration File Content

Below is the complete logback.xml configuration to be placed in `CallCard_Server_WS/src/main/resources/`:

```xml
[Full XML configuration provided in the actual logback.xml file]
```

## Next Steps

1. Back up current logback.xml
2. Apply new comprehensive configuration
3. Test in development environment
4. Monitor log output and file growth
5. Adjust retention policies based on disk usage
6. Deploy to test environment with test profile
7. Deploy to production with prod profile

## Support

For issues with logging configuration:
1. Review this documentation
2. Check Logback documentation
3. Enable DEBUG logging for logback internals:
   - Add: `<logger name="ch.qos.logback" level="DEBUG"/>`
4. Examine startup logs for configuration issues

---

**Document Version:** 1.0
**Last Updated:** 2025-12-22
**Applicable To:** CallCard Microservice v1.0+
