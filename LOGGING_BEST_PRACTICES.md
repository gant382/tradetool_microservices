# CallCard Microservice - Logging Best Practices

## Overview

This guide provides best practices for using the comprehensive logging configuration in the CallCard microservice.

## 1. Code-Level Logging Practices

### 1.1 Using SLF4J Logger

**Correct:**
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public void processOrder(Order order) {
        logger.info("Processing order: {}", order.getId());
    }
}
```

**Incorrect (avoid):**
```java
import java.util.logging.Logger;

public class OrderService {
    private Logger logger = Logger.getLogger(OrderService.class.getName());  // Wrong logger

    public void processOrder(Order order) {
        System.out.println("Processing order: " + order.getId());  // Use logger
    }
}
```

### 1.2 Log Levels

**TRACE - Detailed Diagnostic Information**
```java
// When: Low-level debugging, parameter values
logger.trace("Parameter received: key={}, value={}, type={}", key, value, value.getClass());
```

**DEBUG - Low-Level Diagnostic Information**
```java
// When: Method entry/exit, significant control flow
logger.debug("Entering processOrder method");
logger.debug("Order validation completed: isValid={}", isValid);
```

**INFO - Informational Messages**
```java
// When: Application flow, important events
logger.info("Order {} received for customer {}", orderId, customerId);
logger.info("Payment processed successfully for order {}", orderId);
```

**WARN - Warning Messages**
```java
// When: Potentially harmful situations
logger.warn("Order {} exceeds daily limit of {}", orderId, dailyLimit);
logger.warn("Retry attempt {} for order {}", retryCount, orderId);
```

**ERROR - Error Messages**
```java
// When: Serious problems
logger.error("Failed to process order {}: {}", orderId, e.getMessage(), e);
logger.error("Database connection lost: {}", e.getMessage());
```

### 1.3 Guard Clauses for Performance

**Correct - Avoid expensive operations when not logging:**
```java
// For expensive toString() operations
if (logger.isDebugEnabled()) {
    logger.debug("Order details: {}", order.toString());
}

// Alternative using lambda (SLF4J 1.8+)
logger.debug("Order details: {}", () -> buildComplexDebugString());
```

**Incorrect - Always executes:**
```java
// This always builds the string, even if DEBUG is disabled
logger.debug("Order details: " + order.toString());
```

### 1.4 Exception Logging

**Correct:**
```java
try {
    processOrder(order);
} catch (OrderProcessingException e) {
    logger.error("Order processing failed for order {}", orderId, e);  // Include stack trace
    throw new ApplicationException("Failed to process order", e);
}
```

**Incorrect:**
```java
try {
    processOrder(order);
} catch (OrderProcessingException e) {
    logger.error("Error: " + e.getMessage());  // Lost stack trace
    throw e;
}
```

### 1.5 Structured Logging with Variables

**Correct:**
```java
String status = "COMPLETED";
long duration = 234;
String result = "SUCCESS";

logger.info("Order processing: status={}, duration={}ms, result={}",
    status, duration, result);
```

**Result in log:**
```
2025-12-22 14:35:22.123 [main] INFO com.saicon.games.callcard.service.OrderService - Order processing: status=COMPLETED, duration=234ms, result=SUCCESS
```

### 1.6 Context/Correlation IDs

**Correct - Using MDC:**
```java
import org.slf4j.MDC;

public class OrderService {
    public void processOrder(String orderId) {
        MDC.put("orderId", orderId);
        MDC.put("customerId", getCustomerId(orderId));

        try {
            logger.info("Starting order processing");
            // ... process order
            logger.info("Order processing completed");
        } finally {
            MDC.clear();  // Always clear MDC
        }
    }
}
```

**Log output:**
```
2025-12-22 14:35:22.123 [main] INFO com.saicon.games.callcard.service.OrderService [order123] - Starting order processing
2025-12-22 14:35:22.234 [main] INFO com.saicon.games.callcard.service.OrderService [order123] - Order processing completed
```

## 2. Performance Optimization

### 2.1 Avoid Logging in Hot Paths

**Hot Path: Called thousands of times per second**
```java
public class RequestProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RequestProcessor.class);

    public void processRequest(Request req) {
        // Avoid: Called for every request
        // logger.debug("Processing request: {}", req);  // DON'T

        // Better: Log only important events
        if (req.isCritical()) {
            logger.info("Critical request received: {}", req.getId());
        }
    }
}
```

### 2.2 Batch Operations Logging

**Incorrect - Logs every single operation:**
```java
public void importOrders(List<Order> orders) {
    for (Order order : orders) {
        logger.info("Processing order {}", order.getId());  // Too many logs
        processOrder(order);
    }
}
```

**Correct - Log summary:**
```java
public void importOrders(List<Order> orders) {
    logger.info("Starting import of {} orders", orders.size());
    int processed = 0;
    int failed = 0;

    for (Order order : orders) {
        try {
            processOrder(order);
            processed++;
        } catch (Exception e) {
            failed++;
            logger.warn("Failed to process order {}", order.getId());
        }
    }

    logger.info("Import completed: processed={}, failed={}", processed, failed);
}
```

### 2.3 Lazy Logging with Lambdas

**Java 8+ Syntax:**
```java
public void complexOperation(ComplexObject obj) {
    // Lazy evaluation: buildComplexDebugInfo() only called if DEBUG enabled
    logger.debug("Complex operation: {}", () -> buildComplexDebugInfo(obj));
}

private String buildComplexDebugInfo(ComplexObject obj) {
    // Expensive operation only happens when needed
    return obj.getDeepStructure().toString();
}
```

## 3. Troubleshooting Patterns

### 3.1 Request-Response Logging

**Correct Pattern:**
```java
@Component
public class CallCardInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(CallCardInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        String txnId = UUID.randomUUID().toString();
        MDC.put("transactionId", txnId);
        MDC.put("method", req.getMethod());
        MDC.put("path", req.getRequestURI());

        long startTime = System.currentTimeMillis();
        req.setAttribute("startTime", startTime);

        logger.info("Incoming request");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res,
                               Object handler, Exception ex) {
        long duration = System.currentTimeMillis() - (long) req.getAttribute("startTime");

        if (ex != null) {
            logger.error("Request failed: {}", ex.getMessage(), ex);
        } else {
            logger.info("Request completed: status={}, duration={}ms", res.getStatus(), duration);
        }

        MDC.clear();
    }
}
```

### 3.2 Database Query Debugging

**Enable Query Logging:**
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql: TRACE
```

**Analyze from logs:**
```bash
# Find slow queries
grep "took.*ms" logs/callcard-performance.log | sort -t= -k2 -rn | head -10

# Find specific table queries
grep "SELECT.*FROM orders" logs/callcard-db-access.log

# Find N+1 query problems
grep "SELECT" logs/callcard-db-access.log | sort | uniq -c | sort -rn
```

### 3.3 Service Integration Debugging

**Pattern for external service calls:**
```java
public class ExternalServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(ExternalServiceClient.class);

    public Response callExternalService(Request request) {
        MDC.put("service", "ExternalService");
        long startTime = System.nanoTime();

        try {
            logger.info("Calling external service with request id: {}", request.getId());
            Response response = httpClient.post(endpoint, request);

            long duration = (System.nanoTime() - startTime) / 1_000_000;
            logger.info("Service call successful: duration={}ms, status={}",
                duration, response.getStatus());

            return response;
        } catch (TimeoutException e) {
            logger.warn("Service call timeout: {}ms",
                (System.nanoTime() - startTime) / 1_000_000);
            throw e;
        } catch (Exception e) {
            logger.error("Service call failed: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove("service");
        }
    }
}
```

## 4. Environment-Specific Practices

### 4.1 Development Environment

**Recommended Setup:**
```yaml
logging:
  level:
    root: DEBUG
    com.saicon.games.callcard: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

**Good for:**
- Debugging issues locally
- Understanding code flow
- Testing new features

**Monitoring:**
```bash
# Follow logs in real-time
tail -f logs/callcard-microservice.log

# Search for specific patterns
grep "ERROR" logs/callcard-microservice.log
```

### 4.2 Test Environment

**Recommended Setup:**
```yaml
logging:
  level:
    root: INFO
    com.saicon.games.callcard: DEBUG
    org.hibernate.SQL: DEBUG
```

**Good for:**
- CI/CD pipelines
- Integration testing
- Performance testing

**Monitoring:**
```bash
# Check test results
grep -c "Test.*completed" logs/callcard-microservice.log

# Monitor performance metrics
grep "duration=" logs/callcard-microservice.log
```

### 4.3 Production Environment

**Recommended Setup:**
```yaml
logging:
  level:
    root: WARN
    com.saicon.games.callcard: INFO
    org.springframework: ERROR
```

**Good for:**
- Minimal overhead
- Important events only
- Regulatory compliance

**Monitoring:**
```bash
# Alert on errors
watch -n 60 'grep -c ERROR logs/callcard-microservice-error.log'

# Monitor service health
grep "health.*OK" logs/callcard-microservice.log

# Track circuit breaker state
grep "circuitbreaker" logs/callcard-performance.log
```

## 5. Security Considerations

### 5.1 Sensitive Data

**Incorrect - Logs sensitive data:**
```java
logger.info("Customer credit card: {}", customer.getCreditCard());  // BAD
logger.debug("Password hash: {}", user.getPassword());  // BAD
```

**Correct - Mask sensitive data:**
```java
logger.info("Customer account: {}", maskCreditCard(customer.getCreditCard()));

private String maskCreditCard(String ccNumber) {
    return "**** **** **** " + ccNumber.substring(ccNumber.length() - 4);
}
```

### 5.2 PII (Personally Identifiable Information)

**Avoid logging:**
- Full names (OK: initials or customer ID)
- Email addresses (OK: masked or hashed)
- Phone numbers (OK: last 4 digits)
- SSN/National ID (NEVER log)
- Addresses (NEVER log complete, OK: city only)

### 5.3 API Keys and Secrets

**Never log:**
```java
// WRONG
logger.debug("API Key: {}", apiKey);
logger.info("Database password: {}", password);

// RIGHT - Log metadata only
logger.info("Authenticating with API key id: {}", keyId);
logger.info("Using database connection: {}", databaseUrl);
```

## 6. Operational Monitoring

### 6.1 Key Metrics to Track

```bash
# Error rate
ERROR_COUNT=$(grep -c "ERROR" logs/callcard-microservice-error.log)
TOTAL_LOG=$(grep -c "." logs/callcard-microservice.log)
echo "Error rate: $(echo "scale=2; $ERROR_COUNT * 100 / $TOTAL_LOG" | bc)%"

# Average response time
grep "duration=" logs/callcard-microservice.log | \
awk -F'=' '{sum+=$NF; count++} END {print "Avg duration: " sum/count "ms"}'

# Circuit breaker trips
grep "circuitbreaker.*OPEN" logs/callcard-performance.log | wc -l
```

### 6.2 Alerting

**Set up alerts for:**
1. Error rate exceeds threshold (e.g., >1% of requests)
2. Response time exceeds threshold (e.g., >5 seconds)
3. Circuit breaker opens
4. Database connection issues
5. Service timeouts

### 6.3 Log Aggregation

**Send to centralized system:**
```bash
# Logstash configuration
input {
  file {
    path => "/var/log/callcard/*.log"
    start_position => "beginning"
  }
}

filter {
  grok {
    match => { "message" => "%{TIMESTAMP_ISO8601:timestamp}.*%{LOGLEVEL:level}.*%{DATA:logger}.*%{DATA:message}" }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "callcard-%{+YYYY.MM.dd}"
  }
}
```

## 7. Common Mistakes to Avoid

### 7.1 String Concatenation

**WRONG:**
```java
logger.info("Processing order " + orderId + " for customer " + customerId);  // String concatenation
```

**RIGHT:**
```java
logger.info("Processing order {} for customer {}", orderId, customerId);  // Parameterized
```

### 7.2 Logging Without Context

**WRONG:**
```java
logger.error("Error occurred");  // No information about what error
```

**RIGHT:**
```java
logger.error("Order validation failed: {}", e.getMessage(), e);  // Clear context
```

### 7.3 Not Using Correct Log Level

**WRONG:**
```java
logger.info("Retry attempt 3/5");  // Not worth INFO
logger.warn("Order received");  // Not a warning situation
```

**RIGHT:**
```java
logger.debug("Retry attempt 3/5");  // DEBUG for diagnostic info
logger.info("Order received");  // INFO for important events
```

### 7.4 Logging Too Frequently

**WRONG:**
```java
for (Item item : largeList) {
    logger.info("Processing item: {}", item);  // Millions of logs
}
```

**RIGHT:**
```java
logger.info("Processing {} items", largeList.size());
// ... process
logger.info("Processed items successfully");
```

### 7.5 Not Clearing MDC

**WRONG:**
```java
MDC.put("userId", userId);
logger.info("Processing user");
// MDC still contains userId for next requests!
```

**RIGHT:**
```java
MDC.put("userId", userId);
try {
    logger.info("Processing user");
} finally {
    MDC.remove("userId");  // Always clean up
}
```

## 8. Testing Logging Code

### 8.1 Unit Testing

```java
@Test
public void testLogging() {
    Logger logger = LoggerFactory.getLogger(OrderService.class);

    // Use ListAppender to capture logs
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();

    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    ch.qos.logback.classic.Logger logbackLogger =
        loggerContext.getLogger(OrderService.class);
    logbackLogger.addAppender(listAppender);

    // Test code
    orderService.processOrder(order);

    // Verify logging
    List<ILoggingEvent> logList = listAppender.list;
    assertEquals(1, logList.size());
    assertEquals("Processing order 123", logList.get(0).getMessage());
}
```

### 8.2 Integration Testing

```java
@SpringBootTest
public class CallCardLoggingIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Test
    public void testErrorLogging() throws Exception {
        // Should create error log
        assertThrows(OrderException.class, () ->
            orderService.processOrder(invalidOrder)
        );

        // Verify error was logged
        String errorLog = Files.readString(Path.of("logs/callcard-microservice-error.log"));
        assertThat(errorLog).contains("OrderException");
    }
}
```

## Summary

Key takeaways:
1. Use SLF4J with parameterized logging
2. Choose appropriate log levels
3. Use guard clauses for expensive operations
4. Implement correlation IDs for tracing
5. Avoid logging sensitive data
6. Monitor logs in production
7. Keep logs organized with multiple appenders
8. Use async appenders for performance
9. Test logging code
10. Follow environment-specific configurations

---

**Document Version:** 1.0
**Last Updated:** 2025-12-22
**Audience:** Developers, DevOps, Operations
