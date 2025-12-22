# Performance Tuning and Optimization Guide

**CallCard Microservice Performance Tuning Documentation**

Last Updated: December 22, 2024
Version: 1.0
Technology Stack: Spring Boot 2.7.x, Hibernate 5.6.x, SQL Server 2012+, HikariCP, Caffeine Cache

---

## Table of Contents

1. [JVM Tuning Parameters](#jvm-tuning-parameters)
2. [Database Connection Pool Optimization](#database-connection-pool-optimization)
3. [Hibernate Query Optimization](#hibernate-query-optimization)
4. [Caching Strategies](#caching-strategies)
5. [Thread Pool Configuration](#thread-pool-configuration)
6. [Memory Management](#memory-management)
7. [Performance Monitoring Tools](#performance-monitoring-tools)
8. [Load Testing Recommendations](#load-testing-recommendations)
9. [Bottleneck Identification](#bottleneck-identification)
10. [Profiling Instructions](#profiling-instructions)

---

## 1. JVM Tuning Parameters

### 1.1 Standard JVM Options

For **development** environments:

```bash
JAVA_OPTS="-Xms256m -Xmx512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:InitiatingHeapOccupancyPercent=35 \
  -XX:G1NewCollectionHsizePercent=20 \
  -XX:G1MaxNewGenTaskPercent=8 \
  -XX:-OmitStackTraceInFastThrow \
  -Dfile.encoding=UTF-8"
```

For **staging** environments:

```bash
JAVA_OPTS="-Xms1g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:InitiatingHeapOccupancyPercent=35 \
  -XX:G1NewCollectionHsizePercent=20 \
  -XX:G1MaxNewGenTaskPercent=8 \
  -XX:+ParallelRefProcEnabled \
  -XX:+AlwaysPreTouch \
  -XX:+UnlockDiagnosticVMOptions \
  -XX:G1SummarizeRSetStatsPeriod=86400 \
  -XX:-OmitStackTraceInFastThrow \
  -Dfile.encoding=UTF-8"
```

For **production** environments:

```bash
JAVA_OPTS="-Xms4g -Xmx8g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:InitiatingHeapOccupancyPercent=35 \
  -XX:G1NewCollectionHsizePercent=20 \
  -XX:G1MaxNewGenTaskPercent=8 \
  -XX:+ParallelRefProcEnabled \
  -XX:+AlwaysPreTouch \
  -XX:+UnlockDiagnosticVMOptions \
  -XX:G1SummarizeRSetStatsPeriod=86400 \
  -XX:+UseStringDeduplication \
  -XX:StringDeduplicationAgeThreshold=3 \
  -XX:+ExitOnOutOfMemoryError \
  -XX:OnOutOfMemoryError='kill -9 %p' \
  -XX:-OmitStackTraceInFastThrow \
  -Dfile.encoding=UTF-8 \
  -Djava.awt.headless=true \
  -Dspring.jmx.enabled=true \
  -Dcom.sun.management.jmxremote.port=9010 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false"
```

### 1.2 Garbage Collection Configuration

**G1GC Benefits:**
- Predictable pause times (good for REST APIs)
- Better performance on multi-core systems
- Reduces full GC pauses

**Monitor GC Performance:**

```bash
# Add to JAVA_OPTS for GC logging
-Xlog:gc*:file=logs/gc.log:time,uptime,level,tags:filecount=5,filesize=100m
```

**Key Parameters Explained:**

| Parameter | Value | Purpose |
|-----------|-------|---------|
| `-Xms` | 4g (prod) | Initial heap size (set equal to Xmx) |
| `-Xmx` | 8g (prod) | Maximum heap size (75% of available RAM) |
| `MaxGCPauseMillis` | 200ms | Target pause time for GC |
| `InitiatingHeapOccupancyPercent` | 35% | Trigger concurrent mark cycle |
| `G1NewCollectionHsizePercent` | 20% | Young generation size target |
| `ParallelRefProcEnabled` | true | Parallel reference processing |
| `AlwaysPreTouch` | true | Pre-allocate memory pages |

### 1.3 Startup Parameters

```bash
# Add to application startup script or Docker CMD
java -server \
  ${JAVA_OPTS} \
  -jar CallCard_Server_WS.war \
  --spring.profiles.active=production \
  --server.tomcat.threads.max=200 \
  --server.tomcat.threads.min-spare=20
```

---

## 2. Database Connection Pool Optimization

### 2.1 HikariCP Configuration (Current)

**File:** `CallCard_Server_WS/src/main/resources/application.yml`

Current configuration:

```yaml
spring:
  datasource:
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000         # 5 minutes
      connection-timeout: 20000    # 20 seconds
      max-lifetime: 1200000        # 20 minutes
```

### 2.2 Optimized Configuration by Environment

#### Development (Low Load)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
      auto-commit: true
      leak-detection-threshold: 60000  # 1 minute
```

#### Staging (Medium Load, 50-100 concurrent users)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
      auto-commit: true
      leak-detection-threshold: 30000  # 30 seconds
      connection-test-query: "SELECT 1"
```

#### Production (High Load, 200+ concurrent users)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 20
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
      auto-commit: true
      leak-detection-threshold: 15000  # 15 seconds
      connection-test-query: "SELECT 1"
      validation-timeout: 5000
      initialization-fail-timeout: 1
```

### 2.3 Connection Pool Sizing Formula

```
Pool Size = ((core_count * 2) + effective_spindle_count)
```

For typical scenarios:

- **4-core system:** `(4 * 2) + 1 = 9` connections
- **8-core system:** `(8 * 2) + 1 = 17` connections
- **16-core system:** `(16 * 2) + 1 = 33` connections

**Rule of thumb:** 2-3x the pool size to handle peak demand without starving threads.

### 2.4 Connection Leak Prevention

Add to application properties:

```yaml
spring:
  datasource:
    hikari:
      leak-detection-threshold: 60000  # Log warnings after 60 seconds
      max-lifetime: 1200000            # 20 minutes (SQL Server timeout)
      idle-timeout: 300000             # 5 minutes (close idle connections)
```

Monitor logs for connection leaks:

```bash
# Check logs for:
grep "Connection is dead" logs/application.log
grep "Leak detection" logs/application.log
```

### 2.5 SQL Server Specific Settings

```yaml
spring:
  datasource:
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    hikari:
      data-source-properties:
        encrypt: true
        trustServerCertificate: true
        hostNameInCertificate: "*.database.windows.net"
        loginTimeout: 20
        queryTimeout: 30
        tcpNoDelay: true
```

---

## 3. Hibernate Query Optimization

### 3.1 Current Configuration

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServer2012Dialect
        format_sql: true
        use_sql_comments: true
        generate_statistics: false
        jdbc.batch_size: 50
        order_inserts: true
        order_updates: true
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region.factory_class: org.hibernate.cache.ehcache.EhCacheRegionFactory
```

### 3.2 Enhanced Configuration for Performance

Add to `application-production.yml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    open-in-view: false  # Critical for performance
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServer2012Dialect
        format_sql: false          # Disable in production
        use_sql_comments: false    # Disable in production
        generate_statistics: true  # Enable for analysis
        jdbc.batch_size: 50
        jdbc.batch_versioned_data: true
        order_inserts: true
        order_updates: true

        # Fetch strategies
        default_batch_fetch_size: 16

        # Query cache configuration
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region.factory_class: org.hibernate.cache.ehcache.EhCacheRegionFactory
          region.prefix: "callcard"

          # EhCache regions
          region:
            factory_class: org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory
```

### 3.3 N+1 Query Prevention

**Problem:** Default lazy loading causes N+1 queries

**Solution 1: Fetch Joins in JPQL**

```java
@Repository
public interface CallCardRepository extends JpaRepository<CallCard, Long> {
    @Query("SELECT DISTINCT c FROM CallCard c " +
           "LEFT JOIN FETCH c.services " +
           "LEFT JOIN FETCH c.products " +
           "WHERE c.id = :id")
    Optional<CallCard> findByIdWithServices(@Param("id") Long id);
}
```

**Solution 2: Entity Graph Annotations**

```java
@Entity
@NamedEntityGraph(
    name = "CallCard.withServices",
    attributeNodes = {
        @NamedAttributeNode("services"),
        @NamedAttributeNode(value = "products", subgraph = "products")
    },
    subgraphs = {
        @NamedSubgraph(name = "products", attributeNodes = @NamedAttributeNode("category"))
    }
)
public class CallCard {
    @Id
    private Long id;

    @OneToMany(fetch = FetchType.LAZY)
    private Set<Service> services;

    @OneToMany(fetch = FetchType.LAZY)
    private Set<Product> products;
}

@Repository
public interface CallCardRepository extends JpaRepository<CallCard, Long> {
    @EntityGraph("CallCard.withServices")
    Optional<CallCard> findById(Long id);

    @EntityGraph(attributePaths = {"services", "products"})
    List<CallCard> findAllActive();
}
```

**Solution 3: Read-Only Queries**

```java
@Transactional(readOnly = true)
@Query(value = "SELECT * FROM CallCard WHERE status = 'ACTIVE'",
       nativeQuery = true)
List<CallCard> findAllActive();
```

### 3.4 Query Cache Configuration

**Enable for read-heavy queries:**

```java
@Repository
public interface CallCardRepository extends JpaRepository<CallCard, Long> {
    @Transactional(readOnly = true)
    @QueryHints({
        @QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"),
        @QueryHint(name = org.hibernate.annotations.QueryHints.CACHE_REGION, value = "callcard-lookup")
    })
    @Query("SELECT c FROM CallCard c WHERE c.externalId = :externalId")
    Optional<CallCard> findByExternalId(@Param("externalId") String externalId);
}
```

### 3.5 Pagination Performance

**Inefficient (loads all data then filters):**

```java
List<CallCard> all = repository.findAll();
return all.stream().skip(page * size).limit(size).collect(toList());
```

**Efficient (database-level pagination):**

```java
@Repository
public interface CallCardRepository extends JpaRepository<CallCard, Long> {
    Page<CallCard> findAllByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}

// Usage
Pageable pageable = PageRequest.of(0, 50, Sort.by("createdAt").descending());
Page<CallCard> page = repository.findAllByStatusOrderByCreatedAtDesc("ACTIVE", pageable);
```

### 3.6 Bulk Operations

**For inserting/updating large datasets:**

```java
@Service
public class CallCardBulkService {
    @Autowired
    private CallCardRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public void bulkInsert(List<CallCard> cards) {
        for (int i = 0; i < cards.size(); i++) {
            entityManager.persist(cards.get(i));

            // Flush and clear every 50 records
            if ((i + 1) % 50 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
    }

    @Transactional
    public int bulkUpdate(String status, List<Long> ids) {
        return entityManager.createQuery(
            "UPDATE CallCard c SET c.status = :status WHERE c.id IN :ids")
            .setParameter("status", status)
            .setParameter("ids", ids)
            .executeUpdate();
    }
}
```

### 3.7 SQL Monitoring

Enable Hibernate statistics logging:

```yaml
# application-production.yml
logging:
  level:
    org.hibernate.stat: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

Retrieve statistics programmatically:

```java
@Component
public class HibernateStatsLogger {
    @Autowired
    private SessionFactory sessionFactory;

    @Scheduled(fixedRate = 60000)  // Every 60 seconds
    public void logStatistics() {
        Statistics stats = sessionFactory.getStatistics();
        logger.info("Session count: {}", stats.getSessionOpenCount());
        logger.info("Query execution count: {}", stats.getQueryExecutionCount());
        logger.info("Cache hits: {}", stats.getCacheHitCount());
        logger.info("Cache misses: {}", stats.getCacheMissCount());
    }
}
```

---

## 4. Caching Strategies

### 4.1 Current Caffeine Cache Configuration

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m
```

### 4.2 Multi-Level Caching Strategy

#### Level 1: Application Cache (Caffeine)

Used for frequently accessed, small data:

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "callCards", "services", "products", "sessions"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .recordStats());
        return cacheManager;
    }
}
```

#### Level 2: Hibernate Second-Level Cache (EhCache)

Used for entity caching:

```java
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CallCard {
    // Entity body
}

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ServiceType {
    // Read-only configuration for master data
}
```

#### Level 3: Query Result Cache

For expensive queries:

```java
@Repository
public interface CallCardRepository extends JpaRepository<CallCard, Long> {
    @Transactional(readOnly = true)
    @QueryHints({
        @QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"),
        @QueryHint(name = org.hibernate.annotations.QueryHints.CACHE_REGION, value = "callcard-queries")
    })
    @Query("SELECT c FROM CallCard c WHERE c.status = 'ACTIVE' ORDER BY c.createdAt DESC")
    List<CallCard> findActiveCards();
}
```

### 4.3 EhCache Configuration File

Create `ehcache.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.ehcache.org/v3"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.ehcache.org/v3 http://www.ehcache.org/schema/ehcache-core-3.0.xsd">

    <persistence directory="logs/ehcache"/>

    <!-- Default cache configuration -->
    <cache-template name="default">
        <expiry>
            <ttl unit="minutes">5</ttl>
        </expiry>
        <resources>
            <heap unit="entries">10000</heap>
            <offheap unit="mb">100</offheap>
        </resources>
    </cache-template>

    <!-- CallCard entity cache -->
    <cache alias="com.saicon.games.callcard.entity.CallCard" uses-template="default">
        <expiry>
            <ttl unit="minutes">10</ttl>
        </expiry>
        <resources>
            <heap unit="entries">5000</heap>
            <offheap unit="mb">50</offheap>
        </resources>
    </cache>

    <!-- Service entity cache (read-only) -->
    <cache alias="com.saicon.games.callcard.entity.Service" uses-template="default">
        <expiry>
            <ttl unit="hours">1</ttl>
        </expiry>
        <resources>
            <heap unit="entries">1000</heap>
        </resources>
    </cache>

    <!-- Query result cache -->
    <cache alias="callcard-queries">
        <expiry>
            <ttl unit="minutes">5</ttl>
        </expiry>
        <resources>
            <heap unit="entries">5000</heap>
            <offheap unit="mb">50</offheap>
        </resources>
    </cache>

    <!-- Session cache -->
    <cache alias="callcard-session-cache">
        <expiry>
            <ttl unit="minutes">30</ttl>
        </expiry>
        <resources>
            <heap unit="entries">10000</heap>
        </resources>
    </cache>
</config>
```

### 4.4 Session Caching (CallCard-Specific)

```java
@Configuration
public class SessionCacheConfig {

    @Bean("sessionCache")
    public Cache<String, SessionInfo> sessionCache() {
        return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }
}

@Service
public class SessionValidationService {
    @Autowired
    @Qualifier("sessionCache")
    private Cache<String, SessionInfo> cache;

    public SessionInfo validateSession(String sessionId) {
        return cache.get(sessionId, key -> {
            // Call TALOS Core if cache miss
            return validateWithTalosCore(key);
        });
    }
}
```

### 4.5 Cache Invalidation Strategy

```java
@Service
public class CallCardService {
    @Autowired
    private CacheManager cacheManager;

    @Transactional
    @CacheEvict(value = "callCards", key = "#id")
    public void updateCallCard(Long id, CallCardDTO dto) {
        // Update logic
    }

    @Transactional
    @CacheEvict(value = "callCards", allEntries = true)
    public void bulkUpdate(List<CallCardDTO> updates) {
        // Bulk update logic
    }

    // Manual cache clear
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName ->
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear()
        );
    }
}
```

### 4.6 Cache Metrics Monitoring

```java
@Component
public class CacheMetricsCollector {
    @Autowired
    private CacheManager cacheManager;

    @Scheduled(fixedRate = 30000)
    public void reportCacheMetrics() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            CacheStatistics stats = getStats(cache);
            logger.info("Cache: {}, Hits: {}, Misses: {}, Size: {}",
                cacheName, stats.getHits(), stats.getMisses(), stats.getSize());
        });
    }
}
```

---

## 5. Thread Pool Configuration

### 5.1 Tomcat Thread Pool Settings

Add to `application.yml`:

```yaml
server:
  tomcat:
    threads:
      max: 200              # Production: adjust based on load testing
      min-spare: 20         # Pre-allocated threads
      name-prefix: "callcard-"
      queue-capacity: 100
    accept-count: 100       # Queue size when thread pool is full
    max-connections: 10000  # Maximum TCP connections
    connection-timeout: 60000  # 60 seconds
    keep-alive-timeout: 60000
    processor-cache: 200
```

### 5.2 Custom Thread Pool for Async Operations

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("async-callcard-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(
            new ThreadPoolTaskExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("scheduler-callcard-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }
}
```

### 5.3 Circuit Breaker Thread Pool (Resilience4j)

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        registerHealthIndicator: true
        slidingWindowSize: 10
        failureRateThreshold: 50
        slowCallRateThreshold: 50
        slowCallDurationThreshold: 2s
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true

    instances:
      talosCore:
        baseConfig: default

  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 1000
        retryExceptions:
          - java.net.ConnectException
          - java.net.SocketTimeoutException

    instances:
      talosCore:
        baseConfig: default

  timelimiter:
    configs:
      default:
        cancelRunningFuture: true
        timeoutDuration: 10s

    instances:
      talosCore:
        baseConfig: default

  threadpooldispatched:
    configs:
      default:
        coreThreadPoolSize: 10
        maxThreadPoolSize: 20
        queueCapacity: 100
```

---

## 6. Memory Management

### 6.1 Memory Leak Detection

**Configure leak detection in HikariCP:**

```yaml
spring:
  datasource:
    hikari:
      leak-detection-threshold: 60000
```

**Monitor for leaks:**

```java
@Component
public class MemoryLeakDetector {
    @Scheduled(fixedRate = 300000)  // Every 5 minutes
    public void detectPotentialLeaks() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();

        long usageThreshold = (long) (heap.getMax() * 0.85);
        if (heap.getUsed() > usageThreshold) {
            logger.warn("Heap memory usage high: {} / {} bytes",
                heap.getUsed(), heap.getMax());
        }
    }
}
```

### 6.2 Off-Heap Storage Configuration

**Use for large caches:**

```java
@Configuration
public class OffHeapCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("large-data");
        // Caffeine doesn't support off-heap natively
        // Use EhCache for off-heap requirements
        return manager;
    }
}
```

### 6.3 String Deduplication (Production)

Already configured in production JVM options:

```
-XX:+UseStringDeduplication
-XX:StringDeduplicationAgeThreshold=3
```

### 6.4 Memory Optimization Checklist

- [ ] Disable unused Spring Boot auto-configurations
- [ ] Use `spring.jpa.open-in-view: false` (critical!)
- [ ] Implement proper connection pooling
- [ ] Cache frequently accessed data
- [ ] Implement pagination for large result sets
- [ ] Use DTOs instead of entire entities in responses
- [ ] Enable compression in HTTP responses
- [ ] Monitor memory usage regularly
- [ ] Profile application before deployment
- [ ] Test under expected load

---

## 7. Performance Monitoring Tools

### 7.1 Spring Boot Actuator Endpoints

**Current Configuration:**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when_authorized
```

**Enhanced Configuration:**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env,caches,datasource,scheduledtasks
      base-path: /actuator
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true
    shutdown:
      enabled: false  # Security
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true
    web:
      server:
        request:
          autotime:
            enabled: true
  health:
    diskspace:
      enabled: true
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

### 7.2 Key Monitoring Metrics

**HTTP Request Metrics:**

```
GET /actuator/metrics/http.server.requests
```

Returns:
- Request count
- Response time (mean, max, percentiles)
- Status code distribution
- Exception counts

**Database Metrics:**

```
GET /actuator/metrics/db.connection.active
GET /actuator/metrics/db.connection.max
GET /actuator/metrics/db.connection.pending
```

**Cache Metrics:**

```
GET /actuator/metrics/cache.gets
GET /actuator/metrics/cache.puts
GET /actuator/metrics/cache.removals
GET /actuator/metrics/cache.evictions
```

**JVM Metrics:**

```
GET /actuator/metrics/jvm.memory.usage
GET /actuator/metrics/jvm.threads.live
GET /actuator/metrics/jvm.gc.pause
```

### 7.3 Custom Application Metrics

```java
@Component
public class CallCardMetrics {
    private final MeterRegistry meterRegistry;
    private final AtomicInteger activeOperations;

    public CallCardMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.activeOperations = meterRegistry.gauge(
            "callcard.operations.active",
            new AtomicInteger(0)
        );
    }

    @Transactional
    public void processCallCard(Long cardId) {
        activeOperations.incrementAndGet();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // Business logic
        } finally {
            activeOperations.decrementAndGet();
            sample.stop(Timer.builder("callcard.process.duration")
                .description("Time to process call card")
                .register(meterRegistry));
        }
    }
}
```

### 7.4 OpenTelemetry Integration (Optional)

Add dependency:

```xml
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-spring-boot-starter</artifactId>
    <version>1.32.0</version>
</dependency>
```

Configure:

```yaml
otel:
  exporter:
    otlp:
      endpoint: http://localhost:4317
  sdk:
    disabled: false
  service:
    name: callcard-microservice
```

### 7.5 Prometheus Metrics Export

Add dependency:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Access metrics:

```
http://localhost:8080/actuator/prometheus
```

---

## 8. Load Testing Recommendations

### 8.1 Load Testing Tools

**Apache JMeter:** Best for comprehensive load testing

```bash
# Install
brew install jmeter  # macOS
# or
choco install jmeter  # Windows

# Run tests
jmeter -n -t test-plan.jmx -l results.jtl -j jmeter.log -e -o report/
```

**Gatling:** Code-based load testing (Scala)

```scala
object CallCardSimulation extends Simulation {
    val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .contentTypeHeader("application/json")

    val scn = scenario("CallCard Load Test")
        .repeat(100) {
            exec(http("Get CallCard")
                .get("/callcard/api/cards/${cardId}")
            )
            .pause(1)
        }

    setUp(
        scn.inject(rampUsers(100).during(10.seconds))
    ).protocols(httpProtocol)
}
```

**K6:** Modern load testing tool

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 50,
    duration: '5m',
    thresholds: {
        'http_req_duration': ['p(95)<500'],
        'http_req_failed': ['<0.1'],
    },
};

export default function() {
    let res = http.get('http://localhost:8080/callcard/api/health');
    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 200ms': (r) => r.timings.duration < 200,
    });
    sleep(1);
}
```

### 8.2 Load Testing Scenarios

**Scenario 1: Steady State Load**

```
- Ramp up: 50 users over 2 minutes
- Hold: 100 concurrent users for 10 minutes
- Ramp down: 50 users over 2 minutes
```

**Scenario 2: Spike Test**

```
- Baseline: 20 users
- Spike: 200 users suddenly
- Monitor recovery time and errors
```

**Scenario 3: Endurance Test**

```
- Constant: 50 concurrent users
- Duration: 2+ hours
- Monitor for memory leaks and performance degradation
```

### 8.3 JMeter Test Plan Template

Create `callcard-load-test.jmx`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="CallCard Load Test">
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments"/>
      <stringProp name="TestPlan.user_defined_variables"></stringProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.tearDown_on_shutdown">true</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="CallCard Users">
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">1</stringProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">50</stringProp>
        <stringProp name="ThreadGroup.ramp_time">60</stringProp>
        <elementProp name="ThreadGroup.duration_group" elementType="Arguments"/>
        <stringProp name="ThreadGroup.duration"></stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
        <boolProp name="ThreadGroup.same_user_objects">true</boolProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.schedulers_duration">600</stringProp>
        <stringProp name="ThreadGroup.schedulers_delay"></stringProp>
      </ThreadGroup>
      <hashTree>
        <ConfigTestElement guiclass="HttpConfigGui" testclass="ConfigTestElement" testname="HTTP Request Defaults">
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments"/>
          <stringProp name="HTTPSampler.domain">localhost</stringProp>
          <stringProp name="HTTPSampler.port">8080</stringProp>
          <stringProp name="HTTPSampler.protocol">http</stringProp>
          <stringProp name="HTTPSampler.path">/callcard</stringProp>
        </ConfigTestElement>
        <hashTree/>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="Get CallCard">
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments"/>
          <stringProp name="HTTPSampler.path">/api/callcards/1</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
        </HTTPSamplerProxy>
        <hashTree/>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### 8.4 Performance Acceptance Criteria

| Metric | Development | Staging | Production |
|--------|-------------|---------|------------|
| Response Time (p50) | < 200ms | < 150ms | < 100ms |
| Response Time (p95) | < 500ms | < 300ms | < 200ms |
| Response Time (p99) | < 1000ms | < 500ms | < 500ms |
| Error Rate | < 1% | < 0.5% | < 0.1% |
| Throughput | > 100 req/s | > 500 req/s | > 1000 req/s |
| CPU Usage | < 80% | < 70% | < 60% |
| Memory Usage | < 75% | < 70% | < 65% |

---

## 9. Bottleneck Identification

### 9.1 Common Performance Bottlenecks

**Database Related:**

```
Symptom: High database CPU, slow queries
Detection:
  1. Enable SQL logging
  2. Run SQL Profiler on SQL Server
  3. Check query execution plans
  4. Look for missing indexes
```

**Memory Related:**

```
Symptom: Frequent GC pauses, OOM errors
Detection:
  1. Monitor GC logs
  2. Analyze heap dumps
  3. Check for memory leaks
  4. Review cache configurations
```

**Network Related:**

```
Symptom: High latency, timeout errors
Detection:
  1. Monitor network latency
  2. Check connection pool exhaustion
  3. Review timeout settings
  4. Analyze packet loss
```

**CPU Related:**

```
Symptom: CPU pegged at 100%, slow responses
Detection:
  1. Thread dump analysis
  2. CPU profiling
  3. Lock contention analysis
  4. Dead loop detection
```

### 9.2 SQL Server Query Analyzer

```sql
-- Find slow queries
SELECT TOP 20
    qs.execution_count,
    qs.total_elapsed_time / 1000000 as total_elapsed_sec,
    qs.total_elapsed_time / qs.execution_count / 1000 as avg_elapsed_ms,
    SUBSTRING(st.text, (qs.statement_start_offset / 2) + 1,
        ((qs.statement_end_offset - qs.statement_start_offset) / 2) + 1) as sql_text
FROM sys.dm_exec_query_stats qs
CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle) st
ORDER BY qs.total_elapsed_time DESC;

-- Find missing indexes
SELECT
    CONVERT(decimal(18,2), user_seeks * avg_total_user_cost *
        (avg_user_impact * 0.01)) AS improvement_measure,
    d.equality_columns,
    d.inequality_columns,
    d.included_columns,
    migs.user_seeks,
    migs.avg_user_impact
FROM sys.dm_db_missing_index_groups g
INNER JOIN sys.dm_db_missing_index_group_stats migs
    ON g.index_group_id = migs.group_id
INNER JOIN sys.dm_db_missing_index_details d
    ON g.index_handle = d.index_handle
WHERE database_id = DB_ID()
ORDER BY improvement_measure DESC;

-- Find expensive operations
SELECT
    TOP 10
    DB_NAME(s.database_id) as database_name,
    OBJECT_NAME(p.object_id) as table_name,
    ps.user_updates,
    ps.user_seeks,
    ps.user_scans,
    ps.user_lookups
FROM sys.dm_db_partition_stats ps
INNER JOIN sys.partitions p ON ps.partition_id = p.partition_id
INNER JOIN sys.dm_db_index_usage_stats s ON p.object_id = s.object_id
ORDER BY ps.user_updates DESC;
```

### 9.3 JVM Profiling with JFR

**Enable JFR in production:**

```bash
JAVA_OPTS="${JAVA_OPTS} \
  -XX:+FlightRecorder \
  -XX:StartFlightRecording=delay=30s,duration=120s,filename=application.jfr,name=ApplicationProfile"
```

**Analyze JFR file:**

```bash
# Using JDK Mission Control
jmc application.jfr

# Or use command line
jcmd <pid> JFR.dump filename=output.jfr
```

### 9.4 Spring Boot Diagnostics Endpoint

```bash
# Check configurations
curl http://localhost:8080/actuator/env

# Check caches
curl http://localhost:8080/actuator/caches

# Check data sources
curl http://localhost:8080/actuator/datasource

# Check scheduled tasks
curl http://localhost:8080/actuator/scheduledtasks

# Health check
curl http://localhost:8080/actuator/health
```

### 9.5 Thread Dump Analysis

```bash
# Generate thread dump
jcmd <pid> Thread.print > thread_dump.txt

# Look for patterns:
# - "waiting to lock" - lock contention
# - "blocked" - deadlocks
# - Many threads in same state - potential bottleneck
```

---

## 10. Profiling Instructions

### 10.1 Local Development Profiling

**Using IntelliJ IDEA Profiler:**

1. Run > Edit Configurations
2. Enable "Run with profiler"
3. Choose CPU or Memory profiling
4. Select profiler: Async Profiler or IntelliJ Profiler
5. Start application
6. Trigger load
7. Stop and analyze results

**Key metrics to check:**
- Hot methods (consuming most CPU)
- Memory allocations
- Thread activity
- GC pauses

### 10.2 Production Profiling with Async Profiler

Install Async Profiler:

```bash
# Download
cd /opt
wget https://github.com/jvm-profiling-tools/async-profiler/releases/download/v2.9/async-profiler-2.9-linux-x64.tar.gz
tar xzf async-profiler-2.9-linux-x64.tar.gz
```

Profile running application:

```bash
# CPU profiling
./profiler.sh -d 30 -f cpu.html -e cpu -cstack dwarf <pid>

# Memory profiling
./profiler.sh -d 30 -f memory.html -e alloc <pid>

# Lock contention
./profiler.sh -d 30 -f locks.html -e lock <pid>

# Wall-clock time
./profiler.sh -d 30 -f wall.html -e wall <pid>
```

### 10.3 Flame Graph Visualization

Generate from profile:

```bash
# Export to flamegraph format
jcmd <pid> JFR.dump filename=profile.jfr

# Convert using FlameGraph tools
java -cp jcmd-tools.jar convert jfr --from profile.jfr --output folded.txt
./flamegraph.pl folded.txt > flamegraph.svg

# View in browser
open flamegraph.svg
```

### 10.4 Heap Dump Analysis

**Generate heap dump:**

```bash
# When OutOfMemory occurs
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/heapdump"

# Or manually
jcmd <pid> GC.heap_dump filename=heap.bin
```

**Analyze with Eclipse MAT or JProfiler:**

1. Load heap dump
2. Analyze object retention
3. Find memory leaks
4. Check object counts

### 10.5 Continuous Monitoring with JMX

**Configure JMX Remote Access:**

```bash
JAVA_OPTS="${JAVA_OPTS} \
  -Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=9010 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  -Djava.rmi.server.hostname=<your-host>"
```

**Connect with JConsole or JVisualVM:**

```bash
# JConsole
jconsole localhost:9010

# JVisualVM
jvisualvm
# Add JMX connection: localhost:9010
```

**Key JMX Metrics:**

- Memory usage (heap, non-heap)
- Garbage collection statistics
- Thread count and state
- Runtime properties
- Operating system metrics

### 10.6 Performance Baseline Establishment

**Steps to establish baseline:**

1. **Measure current performance:**
   ```bash
   # Run load test
   jmeter -n -t baseline-test.jmx -l baseline-results.jtl

   # Analyze results
   ./analyze-jmeter-results.sh baseline-results.jtl
   ```

2. **Document metrics:**
   - Response times (mean, p50, p95, p99)
   - Throughput (requests/second)
   - Error rates
   - Resource utilization (CPU, memory)

3. **Establish thresholds:**
   - Performance regression tolerance: 5%
   - Error rate threshold: < 0.1%
   - Resource utilization ceiling: 70%

4. **Regular baseline testing:**
   - After each release
   - With environment changes
   - When performance issues arise

---

## Performance Tuning Checklist

### Immediate Actions (High Impact)

- [ ] Enable compression in HTTP responses
- [ ] Configure proper connection pool size (20-50 connections)
- [ ] Disable `spring.jpa.open-in-view`
- [ ] Configure Hibernate batch operations (batch_size: 50)
- [ ] Enable query and second-level caching
- [ ] Set up proper GC logging
- [ ] Configure thread pools for application (200 max)
- [ ] Implement N+1 query prevention with Entity Graphs
- [ ] Set up actuator endpoints for monitoring

### Medium-Term Actions (3-6 months)

- [ ] Establish performance baseline with load testing
- [ ] Implement custom metrics and monitoring
- [ ] Optimize database indexes based on usage patterns
- [ ] Review and optimize hotspot queries
- [ ] Implement distributed tracing (OpenTelemetry)
- [ ] Set up alerting for performance degradation
- [ ] Document runbook for common performance issues
- [ ] Conduct capacity planning analysis

### Long-Term Actions (6-12 months)

- [ ] Migrate to newer versions of frameworks (Spring Boot 3.x, Hibernate 6.x)
- [ ] Evaluate caching solutions (Redis for distributed cache)
- [ ] Implement database replication for read scaling
- [ ] Consider microservices decomposition if needed
- [ ] Evaluate message queue for async processing
- [ ] Plan for containerization and orchestration
- [ ] Establish SLOs and SLIs for performance
- [ ] Implement automated performance regression testing

---

## References and Resources

### Tools
- [Apache JMeter](https://jmeter.apache.org/)
- [Gatling](https://gatling.io/)
- [K6](https://k6.io/)
- [Async Profiler](https://github.com/jvm-profiling-tools/async-profiler)
- [JProfiler](https://www.ej-technologies.com/products/jprofiler/overview.html)
- [YourKit Java Profiler](https://www.yourkit.com/java/profiler/)

### Documentation
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Hibernate Performance Tuning](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#performance)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)
- [G1GC Tuning](https://docs.oracle.com/en/java/javase/11/tools/tools-and-command-references.html)
- [JFR Events](https://docs.oracle.com/javacomponents/jmc-5-4/jmc-user-guide/GUID-39A692DF-E1CF-4D1D-B907-F1D6212E20F5.htm)

### Best Practices
- [12 Factor App](https://12factor.net/)
- [Google SRE Book](https://sre.google/books/)
- [The Art of Capacity Planning](https://www.oreilly.com/library/view/the-art-of/9780596518585/)

---

## Support and Issues

For performance issues or questions:

1. Check the [Bottleneck Identification](#9-bottleneck-identification) section
2. Run load tests to confirm the issue
3. Collect profiling data using [Profiling Instructions](#10-profiling-instructions)
4. Review relevant metrics in [Performance Monitoring Tools](#7-performance-monitoring-tools)
5. Document findings and share with the team

---

**Document Version:** 1.0
**Last Updated:** December 22, 2024
**Next Review Date:** June 22, 2025
