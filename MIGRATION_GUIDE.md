# Migration Guide: gameserver_v3 Monolith to CallCard Microservice

**Version:** 1.0.0
**Date:** December 2025
**Status:** Production-Ready Migration Plan
**Target:** Gradual decomposition of CallCard services from gameserver_v3 monolith

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Architecture Comparison](#architecture-comparison)
3. [What Was Extracted](#what-was-extracted)
4. [What Remains in Monolith](#what-remains-in-monolith)
5. [Database Sharing Strategy](#database-sharing-strategy)
6. [API Compatibility Layer](#api-compatibility-layer)
7. [Gradual Migration Steps](#gradual-migration-steps)
8. [Rollback Procedures](#rollback-procedures)
9. [Testing Migration](#testing-migration)
10. [Production Cutover Plan](#production-cutover-plan)
11. [Post-Migration Validation](#post-migration-validation)

---

## Executive Summary

This guide documents the migration from the monolithic **gameserver_v3** architecture to the microservice-based **CallCard** architecture. The migration employs a strangler pattern to minimize risk and downtime, allowing for gradual decommissioning of monolith components while maintaining backward compatibility.

**Key Objectives:**
- Maintain 100% API compatibility during transition
- Achieve zero-downtime migration
- Enable independent scaling of CallCard services
- Reduce operational complexity and deployment coupling
- Establish patterns for future service extractions

**Timeline Phases:**
- **Phase 1: Dual-Run (Weeks 1-4)** - Both systems operational
- **Phase 2: Traffic Shifting (Weeks 5-8)** - Gradual cutover
- **Phase 3: Validation (Weeks 9-10)** - Full migration validation
- **Phase 4: Monolith Cleanup (Weeks 11-12)** - Archive legacy code

---

## Architecture Comparison

### Monolith Architecture (gameserver_v3)

```
gameserver_v3 (Single Deployment)
├── Platform_Commons (Foundation)
│   ├── Commons (com.saicon.games.commons)
│   └── build-support
│
├── Network_Client (Client Layer)
│   ├── Game_NetClientInterface
│   └── MessageProcessor
│
├── Platform_Core (Business Logic)
│   ├── Game_Server_WS (Main WAR - Port 8080)
│   ├── ERP/ (4-layer modules)
│   ├── userMessaging/
│   ├── lottery/
│   └── WS_API_* (30+ API definitions)
│
├── Game_Specific (Game Services)
│   ├── DataImport_Server_WS
│   ├── DataExport_Server_WS
│   ├── Quiz_Server_WS
│   ├── Amilon_Server_WS
│   └── SpecialProcessingEvent_Server_WS
│
└── Frontend_Commons
    └── IctQatarCommons
```

**Technology Stack:**
- Java 1.6+ (legacy compatibility)
- Spring Framework 3.0.x (XML configuration)
- Apache CXF 2.7.x (SOAP)
- Hibernate 4.x
- MSSQL Server 2008+
- Single database connection
- Hazelcast distributed state
- Quartz scheduling

**Deployment Model:**
- Single WAR deployment per service
- JNDI datasource configuration
- Shared EhCache instances
- Coupled release cycles
- Monolithic database schema

---

### Microservice Architecture (CallCard)

```
tradetool_middleware (Parent POM - Spring Boot 2.7.x)
├── callcard-entity (JAR)
│   └── JPA entities (com.saicon.games.callcard.entity)
│
├── callcard-ws-api (JAR)
│   ├── SOAP interfaces (@WebService)
│   ├── REST interfaces (@Path)
│   ├── DTOs (@XmlRootElement)
│   └── Annotations (@FastInfoset, @GZIP)
│
├── callcard-components (JAR)
│   ├── Business DAOs
│   ├── Converters
│   ├── Query managers
│   └── External integrations
│
├── callcard-service (JAR)
│   ├── Service orchestration
│   ├── Transaction boundaries
│   ├── Cache management
│   └── Event publishing
│
└── CallCard_Server_WS (WAR/JAR - Spring Boot)
    ├── Spring Boot starter (Port configurable: 8081-8090)
    ├── Actuator endpoints (/actuator/*)
    ├── CXF SOAP servlet (/cxf/*)
    ├── Jersey REST servlet (/rest/*)
    ├── Health checks (/health)
    └── Metrics (/metrics)
```

**Technology Stack:**
- Java 1.8+ (modern JVM)
- Spring Boot 2.7.x (auto-configuration)
- Spring Data JPA (repository abstraction)
- Apache CXF 3.5.x (SOAP, backward compatible)
- Jersey 2.39.x (REST/JAX-RS)
- Hibernate 5.6.x (modern ORM)
- Caffeine caching (local)
- Resilience4j (circuit breakers)
- springdoc-openapi (API documentation)
- MSSQL Server (shared database)

**Deployment Model:**
- Independent microservice (Spring Boot)
- Embedded application server (Tomcat)
- Self-contained configuration
- Actuator-based health checks
- Independent scaling and deployment
- Shared database (during migration)
- Event-driven communication

---

## What Was Extracted

### Source Code Extraction

**callcard-entity Module (JAR)**
```
callcard-entity/
├── pom.xml (Spring Boot dependency management)
└── src/main/java/com/saicon/games/callcard/entity/
    ├── CallCard.java (JPA @Entity)
    ├── CallCardDetail.java
    ├── CallCardType.java
    ├── CallCardStatus.java
    └── *.java (supporting entities)

Extracted from gameserver_v3:
├── Platform_Core/ERP/erp-entity/
├── Shared entity definitions
└── Extended with new properties as needed
```

**callcard-ws-api Module (JAR)**
```
callcard-ws-api/
├── pom.xml
└── src/main/java/com/saicon/games/callcard/ws/
    ├── ICallCardService.java (@WebService - SOAP interface)
    ├── ICallCardResource.java (@Path - REST interface)
    ├── CallCardDTO.java
    ├── CallCardRequestDTO.java
    ├── CallCardResponseDTO.java
    └── annotations/ (@FastInfoset, @GZIP)

Extracted from gameserver_v3:
├── WS_API_CallCard/
└── Legacy SOAP interface definitions
```

**callcard-components Module (JAR)**
```
callcard-components/
├── pom.xml
└── src/main/java/com/saicon/games/callcard/components/
    ├── CallCardDAO.java (JPA repository equivalent)
    ├── CallCardComponent.java (business logic)
    ├── CallCardConverterUtil.java (DTO conversion)
    ├── CallCardValidator.java (input validation)
    ├── external/
    │   ├── IAddressbookManagement.java (external service interface)
    │   ├── ISalesOrderManagement.java
    │   ├── IUserMetadataComponent.java
    │   └── IUserSessionManagement.java
    └── cache/
        ├── CallCardCacheManager.java (Caffeine)
        └── CacheInvalidationListener.java

Extracted from gameserver_v3:
├── Platform_Core/ERP/erp-components/
├── Game_Server_WS application layer
└── Custom caching implementations
```

**callcard-service Module (JAR)**
```
callcard-service/
├── pom.xml
└── src/main/java/com/saicon/games/callcard/service/
    ├── ICallCardService.java (service interface)
    ├── CallCardService.java (@Service, transactional)
    ├── CallCardWorkflow.java (complex operations)
    ├── CallCardEventPublisher.java (domain events)
    └── CallCardAuditService.java (audit trail)

Extracted from gameserver_v3:
├── Custom service layer
├── Transaction boundaries
└── Business workflow logic
```

**CallCard_Server_WS Module (WAR/JAR)**
```
CallCard_Server_WS/
├── pom.xml (Spring Boot plugin)
├── src/main/java/com/saicon/games/callcard/
│   ├── CallCardApplication.java (@SpringBootApplication)
│   ├── config/
│   │   ├── DataSourceConfig.java
│   │   ├── JpaConfig.java
│   │   ├── CxfConfig.java (SOAP endpoint registration)
│   │   ├── JerseyConfig.java (REST servlet configuration)
│   │   ├── CacheConfig.java (Caffeine/EhCache)
│   │   ├── SecurityConfig.java (JWT/auth)
│   │   └── ResilienceConfig.java (circuit breakers)
│   ├── controller/ (REST endpoints if applicable)
│   └── exception/ (global exception handling)
├── src/main/resources/
│   ├── application.yml (Spring Boot configuration)
│   ├── application-dev.yml
│   ├── application-staging.yml
│   ├── application-production.yml
│   ├── logback-spring.xml
│   └── META-INF/
│       └── persistence.xml (JPA configuration)
└── src/main/webapp/
    └── WEB-INF/
        ├── web.xml (servlet mapping)
        └── cxf-services.xml (legacy: CXF endpoint registration)

Extracted from gameserver_v3:
├── Game_Server_WS web application
├── Spring XML configuration (converted to Java @Configuration)
└── CXF servlet configuration
```

### Data Extraction

**JPA Entities Copied:**
- All CallCard-related entity classes
- Relationship definitions (@OneToMany, @ManyToOne)
- Validation annotations (@NotNull, @Size)
- Query annotations (@NamedQuery, @SqlResultSetMapping)

**Constraints & Indexes:**
```sql
-- Migrated database constraints
ALTER TABLE CallCard ADD CONSTRAINT pk_callcard PRIMARY KEY (callcard_id);
ALTER TABLE CallCard ADD CONSTRAINT fk_callcard_type FOREIGN KEY (type_id) REFERENCES CallCardType(type_id);
ALTER TABLE CallCard ADD CONSTRAINT fk_callcard_status FOREIGN KEY (status_id) REFERENCES CallCardStatus(status_id);

-- Migrated indexes
CREATE INDEX idx_callcard_user ON CallCard(user_id);
CREATE INDEX idx_callcard_date ON CallCard(created_date);
CREATE INDEX idx_callcard_type_status ON CallCard(type_id, status_id);
```

---

## What Remains in Monolith

### Core Platform Components

**NOT Extracted:**
```
gameserver_v3/ (Remains)
├── Platform_Commons/ (foundation)
├── Network_Client/ (client generation)
├── Platform_Core/
│   ├── Game_Server_WS (main platform)
│   ├── Game_Server_Core (game logic)
│   ├── Resource_Server_WS (resources)
│   ├── Platform_Jobs (scheduling)
│   ├── ERP/ (accounting, still core)
│   ├── userMessaging/ (still core)
│   ├── lottery/ (still core)
│   └── WS_API_* (most APIs remain)
│
├── Game_Specific/
│   ├── DataImport_Server_WS
│   ├── DataExport_Server_WS
│   ├── Quiz_Server_WS
│   ├── Amilon_Server_WS
│   └── SpecialProcessingEvent_Server_WS
│
└── Frontend_Commons/
```

**Rationale:**
- CallCard is first extraction (lowest risk)
- Core ERP, messaging, and gaming services remain tightly coupled
- Shared infrastructure (Hazelcast, Quartz) not yet decomposed
- Client generation still needs monolith coordination

### Legacy Infrastructure Retained

**Not Migrated:**
- Legacy Spring 3.0.x XML configuration patterns
- Hazelcast distributed cache (if in use)
- Quartz job scheduling framework
- JNDI datasource lookups (legacy)
- SVN-based deployment scripts
- Legacy IconParentPom dependencies

---

## Database Sharing Strategy

### Unified Schema Approach (Phase 1-3)

Both monolith and microservice use **same MSSQL database** during migration:

```
MSSQL Server (Single Instance)
└── gameserver_v3 (database)
    ├── [dbo].[CallCard*] tables (shared)
    │   ├── CallCard (primary)
    │   ├── CallCardDetail (details)
    │   ├── CallCardType (reference)
    │   └── CallCardStatus (reference)
    │
    ├── [dbo].[ERP_*] tables (monolith only)
    ├── [dbo].[Users_*] tables (monolith only)
    ├── [dbo].[Lottery_*] tables (monolith only)
    └── [dbo].[*] (30+ other tables)
```

### Connection Configuration

**gameserver_v3 (JNDI):**
```xml
<!-- src/main/webapp/WEB-INF/serverbeans.xml -->
<bean class="org.springframework.jndi.JndiObjectFactoryBean">
    <property name="jndiName" value="java:comp/env/jdbc/gameserver_v3"/>
</bean>
```

**CallCard_Server_WS (Spring Boot):**
```yaml
# src/main/resources/application-production.yml
spring:
  datasource:
    url: jdbc:sqlserver://db.saicon.local:1433;database=gameserver_v3
    username: callcard_svc
    password: ${DB_PASSWORD_ENCRYPTED}
    driverClassName: com.microsoft.sqlserver.jdbc.SQLServerDriver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
  jpa:
    hibernate:
      ddl-auto: validate  # No schema generation in production
    properties:
      hibernate.dialect: org.hibernate.dialect.SQLServer2012Dialect
```

### Data Access Control

**User Access:** Create dedicated database user for CallCard service
```sql
-- Create CallCard service user
CREATE LOGIN callcard_svc WITH PASSWORD = 'SecurePassword123!';
CREATE USER callcard_svc FOR LOGIN callcard_svc;

-- Grant minimal required permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA::[dbo] TO callcard_svc;
GRANT EXECUTE ON SCHEMA::[dbo] TO callcard_svc;

-- Restrict to CallCard tables only (best practice)
GRANT SELECT, INSERT, UPDATE, DELETE ON [dbo].[CallCard] TO callcard_svc;
GRANT SELECT, INSERT, UPDATE, DELETE ON [dbo].[CallCardDetail] TO callcard_svc;
GRANT SELECT ON [dbo].[CallCardType] TO callcard_svc;
GRANT SELECT ON [dbo].[CallCardStatus] TO callcard_svc;
GRANT SELECT ON [dbo].[Users] TO callcard_svc;  -- For audit trail
```

### Schema Compatibility

**Entity-to-Table Mapping Validation:**
```java
// CallCard entity must match database schema exactly
@Entity
@Table(name = "CallCard")
@NamedQuery(name = "CallCard.findByStatus",
    query = "SELECT c FROM CallCard c WHERE c.status = :status")
public class CallCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "callcard_id")
    private Long id;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    // ... all columns must exist in database
}
```

**Pre-Migration Validation Script:**
```sql
-- Verify all required tables and columns exist
SELECT OBJECT_NAME(id) as TableName, name as ColumnName
FROM sysobjects o
INNER JOIN syscolumns c ON o.id = c.id
WHERE OBJECT_NAME(id) IN ('CallCard', 'CallCardDetail', 'CallCardType', 'CallCardStatus')
ORDER BY OBJECT_NAME(id), colid;
```

### Transaction Consistency

**During Shared Phase:**
- Both systems operate against same tables
- Monolith retains transaction isolation (@Transactional)
- Microservice uses Spring Data JPA transactions
- Implement optimistic locking (version columns) to prevent conflicts

```java
@Entity
@Table(name = "CallCard")
public class CallCard {
    @Version
    @Column(name = "version")
    private Long version;  // Prevents concurrent modification conflicts

    @Column(name = "last_modified_by")
    private String modifiedBy;  // Track which system modified

    @Column(name = "last_modified_date")
    private LocalDateTime modifiedDate;
}
```

### Database Monitoring During Migration

**Monitor Locks:**
```sql
-- Identify long-running queries and locks
SELECT * FROM sys.dm_exec_requests WHERE session_id > 50;
SELECT * FROM sys.dm_tran_locks;

-- Check connection count per application
SELECT DB_NAME(dbid) as Database,
       COUNT(*) as ConnectionCount
FROM sys.sysprocesses
WHERE dbid > 0
GROUP BY dbid
ORDER BY ConnectionCount DESC;
```

---

## API Compatibility Layer

### Strangler Pattern Implementation

Create an **API Gateway** to route requests to either monolith or microservice:

```
Client Applications
    ↓
API Gateway (Nginx/Spring Cloud Gateway)
    ├─→ /cxf/CallCard* → CallCard Microservice (NEW)
    ├─→ /cxf/GameService* → gameserver_v3 Monolith (OLD)
    ├─→ /cxf/ERP* → gameserver_v3 Monolith (OLD)
    └─→ /* → gameserver_v3 (default)
```

### Nginx Configuration (Example)

```nginx
# /etc/nginx/conf.d/callcard-routing.conf
upstream callcard_microservice {
    server 192.168.1.100:8081 max_fails=3 fail_timeout=30s;
    server 192.168.1.101:8081 backup;
}

upstream gameserver_monolith {
    server 192.168.1.50:8080 max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    server_name api.saicon.local;

    # Route CallCard requests to microservice
    location ~ ^/cxf/CallCard {
        proxy_pass http://callcard_microservice;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Service-Type "CallCard";  # Track origin
        proxy_connect_timeout 10s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }

    # Route everything else to monolith
    location / {
        proxy_pass http://gameserver_monolith;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### SOAP Compatibility

Both SOAP interfaces must be 100% compatible:

**Monolith Service (Legacy):**
```java
// gameserver_v3/Game_Server_WS/WS_API_CallCard/ICallCardService.java
@WebService(targetNamespace = "http://ws.callcard.saicon.com/")
@FastInfoset
@GZIP
public interface ICallCardService {
    @WebMethod(operationName = "createCallCard")
    CallCardDTO createCallCard(CreateCallCardRequest request);

    @WebMethod(operationName = "getCallCard")
    CallCardDTO getCallCard(GetCallCardRequest request);
}
```

**Microservice Service (New):**
```java
// tradetool_middleware/callcard-ws-api/ICallCardService.java
@WebService(targetNamespace = "http://ws.callcard.saicon.com/")  // SAME namespace
@FastInfoset
@GZIP
public interface ICallCardService {
    @WebMethod(operationName = "createCallCard")
    CallCardDTO createCallCard(CreateCallCardRequest request);

    @WebMethod(operationName = "getCallCard")
    CallCardDTO getCallCard(GetCallCardRequest request);

    // NEW operations can be added here without breaking existing clients
}
```

### REST Compatibility

Similarly for REST endpoints:

```java
// microservice/callcard-ws-api
@Path("/callcard")
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface ICallCardResource {

    @GET
    @Path("/{id}")
    CallCardDTO getCallCard(@PathParam("id") Long id);

    @POST
    @Path("/")
    CallCardDTO createCallCard(CallCardDTO dto);

    @PUT
    @Path("/{id}")
    CallCardDTO updateCallCard(@PathParam("id") Long id, CallCardDTO dto);
}
```

### Version Management

**Maintain backward compatibility with versioning:**
```java
@WebService(
    targetNamespace = "http://ws.callcard.saicon.com/v2/",
    name = "ICallCardServiceV2"
)
public interface ICallCardServiceV2 {
    // New operations with enhanced capabilities
    @WebMethod(operationName = "createCallCardV2")
    CallCardDTOV2 createCallCard(CreateCallCardRequestV2 request);
}

// Register V2 endpoint alongside V1 in Spring Boot config
```

---

## Gradual Migration Steps

### Phase 1: Dual-Run (Weeks 1-4)

**Objective:** Both systems operational, zero traffic to microservice

**Week 1: Setup and Validation**

1. Deploy CallCard microservice to staging environment
   ```bash
   cd tradetool_middleware
   mvn clean package -Ppmi-staging-v3-1
   # Deploy CallCard_Server_WS/target/CallCard_Server_WS.war to Tomcat
   ```

2. Verify service health
   ```bash
   curl -X GET http://callcard-staging:8081/actuator/health
   curl -X GET http://callcard-staging:8081/cxf/CallCard?wsdl
   ```

3. Configure data source for shared database
   ```yaml
   spring:
     datasource:
       url: jdbc:sqlserver://db-staging:1433;database=gameserver_v3
       username: callcard_svc_staging
   ```

4. Run entity validation tests
   ```bash
   mvn test -Dtest=CallCardEntityTest
   mvn test -Dtest=DataAccessTest
   ```

5. Execute smoke tests against microservice
   ```bash
   # Test basic CRUD operations
   curl -X POST http://callcard-staging:8081/cxf/CallCard \
     -H "Content-Type: application/json" \
     -d @test_payloads/create_callcard.json
   ```

**Week 2: Load Testing and Monitoring**

1. Set up monitoring stack
   ```bash
   # Configure Prometheus scraping
   - job_name: 'callcard-microservice'
     metrics_path: '/actuator/prometheus'
     static_configs:
       - targets: ['callcard-staging:8081']
   ```

2. Run load tests (no production traffic)
   ```bash
   jmeter -n -t test_plans/callcard_load_test.jmx \
     -l results.jtl -e -o ./report \
     -Jhost=callcard-staging -Jport=8081 -Jthreads=100
   ```

3. Validate response times
   ```
   Target: < 500ms for 95th percentile
   Acceptable: < 1000ms for 99th percentile
   ```

4. Check database connection pool health
   ```sql
   SELECT * FROM sys.dm_exec_connections
   WHERE session_id > 50 AND database_id = DB_ID('gameserver_v3');
   ```

**Week 3: Failover Testing**

1. Test service restart resilience
   ```bash
   # Kill microservice, verify health check detects failure
   kill -9 <callcard-pid>
   curl -X GET http://callcard-staging:8081/actuator/health  # Should fail

   # Restart and verify recovery
   systemctl restart callcard-service
   # Monitor startup time: target < 30 seconds
   ```

2. Test database failover (if applicable)
   ```bash
   # Simulate database connection loss
   # Verify service handles gracefully with circuit breaker
   # Monitor error rates and recovery
   ```

3. Test cache invalidation
   ```java
   // Verify cache consistency between monolith and microservice
   // Test scenario: Update via monolith, read from microservice
   ```

**Week 4: Monitoring and Alerting Setup**

1. Configure alerting rules
   ```yaml
   # Prometheus alerting rules
   - alert: CallCardServiceDown
     expr: up{job="callcard-microservice"} == 0
     for: 5m
     action: page

   - alert: CallCardHighErrorRate
     expr: rate(http_requests_total{service="callcard", status=~"5.."}[5m]) > 0.05
     for: 5m
     action: page
   ```

2. Set up logging aggregation
   ```bash
   # ELK Stack or Splunk configuration
   # Tail application logs from CallCard microservice
   # Index into centralized logging
   ```

3. Create runbooks
   - Microservice restart procedure
   - Database connection troubleshooting
   - Cache invalidation procedures
   - Circuit breaker reset procedures

---

### Phase 2: Traffic Shifting (Weeks 5-8)

**Objective:** Gradually shift traffic from monolith to microservice

**Week 5: Canary Deployment (5% Traffic)**

1. Update API Gateway routing rules
   ```nginx
   # Configure 95% → monolith, 5% → microservice
   upstream callcard_microservice {
       weight=5;
   }
   upstream gameserver_monolith {
       weight=95;
   }
   ```

2. Monitor microservice metrics
   ```bash
   # Dashboard: Request count, error rate, response time
   # Alert threshold: error rate > 1%
   ```

3. Compare responses between systems
   ```bash
   # Sample requests from both systems
   # Verify identical JSON/XML structure
   # Log response time differences
   ```

4. Weekly review meeting
   - Production metrics analysis
   - Error logs review
   - Performance comparison

**Week 6: Gradual Shift (25% Traffic)**

1. Update routing to 25% microservice, 75% monolith
   ```nginx
   upstream callcard_microservice { weight=25; }
   upstream gameserver_monolith { weight=75; }
   ```

2. Monitor for anomalies
   ```bash
   # Daily metrics review
   # Alert on:
   # - Error rate increase > 1%
   # - Response time increase > 20%
   # - Database connection spike
   ```

3. Collect performance baseline
   ```sql
   -- Track query performance differences
   SELECT AVG(duration), MAX(duration), COUNT(*)
   FROM query_logs
   WHERE service = 'callcard'
   GROUP BY operation, database;
   ```

4. Validate data consistency
   ```java
   // Automated comparison test
   // Read same record from both microservice and monolith
   // Assert identical content
   ```

**Week 7: Majority Shift (75% Traffic)**

1. Update routing to 75% microservice, 25% monolith
   ```nginx
   upstream callcard_microservice { weight=75; }
   upstream gameserver_monolith { weight=25; }
   ```

2. Implement parallel request logging
   ```java
   // Log requests/responses from both systems for comparison
   // Automated diff detection for anomalies
   ```

3. Enhanced monitoring
   - Set up synthetic transactions
   - Increase alerting sensitivity
   - Prepare incident response team

4. Prepare rollback procedures
   - Document exact configuration state
   - Prepare rollback decision criteria
   - Test rollback in staging

**Week 8: Full Cutover (100% Traffic)**

1. Update routing to 100% microservice
   ```nginx
   upstream callcard_microservice { weight=100; }
   ```

2. Monitor continuously for 72 hours
   ```bash
   # Watch error rates, response times, database load
   # Alert on any degradation
   # Have rollback team on standby
   ```

3. Disable monolith CallCard services
   ```bash
   # Comment out CallCard endpoints in monolith
   # Keep code in place (not deleted)
   ```

4. Document cutover completion
   - Record timestamp of 100% traffic shift
   - Capture performance metrics
   - Update architecture documentation

---

### Phase 3: Validation (Weeks 9-10)

**Objective:** Ensure migration success and stability

**Week 9: Stability Monitoring**

1. Run production load tests (if safe)
   ```bash
   # Production-like traffic patterns
   # Monitor under realistic conditions
   ```

2. Verify all features working
   ```bash
   # Create comprehensive feature checklist
   # Test: CRUD operations, workflows, integrations
   ```

3. Validate integration points
   ```bash
   # Test external service calls:
   # - User metadata lookups
   # - Sales order management
   # - Address book queries
   # - App settings retrieval
   ```

4. Database performance analysis
   ```sql
   -- Compare index usage before/after migration
   SELECT * FROM sys.dm_db_index_usage_stats
   WHERE database_id = DB_ID('gameserver_v3')
   AND object_name IN ('CallCard', 'CallCardDetail', ...)
   ORDER BY user_seeks + user_scans DESC;
   ```

**Week 10: Post-Migration Validation**

1. Security audit
   ```bash
   # Verify: Authentication working correctly
   # Verify: Authorization enforced
   # Verify: API keys properly configured
   # Verify: HTTPS enabled
   ```

2. Performance benchmarking
   ```bash
   # Compare response times: microservice vs. monolith
   # CPU/Memory usage per request
   # Database query efficiency
   # Create baseline report
   ```

3. Data integrity validation
   ```java
   // Run comprehensive data validation
   // Count records: microservice vs. monolith view
   // Hash entire result sets
   // Alert on mismatches
   ```

4. Customer acceptance testing
   - Deploy to user acceptance environment
   - Have stakeholders validate functionality
   - Document sign-off

---

### Phase 4: Monolith Cleanup (Weeks 11-12)

**Objective:** Archive legacy code, reduce maintenance burden

**Week 11: Archive Legacy Code**

1. Create SVN branch for legacy code
   ```bash
   svn copy \
     https://172.16.0.21/svn/gameserver_v3/trunk \
     https://172.16.0.21/svn/gameserver_v3/branches/legacy-callcard-backup \
     -m "Backup: CallCard extracted to microservice"
   ```

2. Remove CallCard code from monolith
   ```bash
   # Remove: Game_Specific/WS_API_CallCard/
   # Remove: Game_Specific/CallCard_Server_WS/
   # Remove: CallCard-related code from Platform_Core
   # Remove: CallCard entity references from shared entities
   ```

3. Update monolith build
   ```xml
   <!-- Remove from pom.xml module list -->
   <!-- Remove CallCard dependencies -->
   <!-- Rebuild: mvn clean install -->
   ```

4. Update documentation
   ```markdown
   # Architecture Documentation
   - CallCard service: EXTRACTED to tradetool_middleware (microservice)
   - Location: C:\Users\dimit\tradetool_middleware
   - Deployment: Spring Boot on port 8081
   - Database: gameserver_v3 (shared, phase 4)
   ```

**Week 12: Final Decommissioning**

1. Verify no remaining dependencies
   ```bash
   grep -r "CallCard" gameserver_v3/
   grep -r "callcard" gameserver_v3/
   # Should return only comments/documentation references
   ```

2. Update deployment procedures
   ```bash
   # Remove CallCard deployment steps from gameserver_v3 docs
   # Update: Only run if CallCard not deployed as microservice
   ```

3. Archive/delete unused infrastructure
   ```bash
   # Decommission: Any CallCard-only Tomcat instances (if existed)
   # Remove: CallCard configuration files from monolith
   # Clean up: Old database views/procedures
   ```

4. Final documentation
   - Create migration summary
   - Document lessons learned
   - Update runbooks for production

---

## Rollback Procedures

### Immediate Rollback (< 5 Minutes)

**Scenario:** Microservice experiencing critical failures

**Action:** Revert traffic routing to monolith
```nginx
# /etc/nginx/conf.d/callcard-routing.conf
upstream callcard_active {
    server 192.168.1.50:8080;  # Monolith (was backup)
}

server {
    location ~ ^/cxf/CallCard {
        proxy_pass http://callcard_active;
    }
}

# Reload Nginx
systemctl reload nginx
```

**Verification:**
```bash
# Confirm traffic routing
curl -X GET http://api.saicon.local/cxf/CallCard?wsdl

# Monitor monolith load
top -p <monolith-pid>
htop -u gameserver

# Check error logs
tail -f /var/log/tomcat/gameserver_v3.log | grep -i error
```

### Partial Rollback (Data Restoration)

**Scenario:** Data corruption detected in microservice

**Action 1: Enable read-only mode on microservice**
```yaml
# application-production.yml
callcard:
  readOnly: true  # Block all writes, allow reads only
```

**Action 2: Restore from monolith database view**
```bash
# If database has point-in-time recovery enabled
RESTORE DATABASE gameserver_v3
FROM DISK = '/backup/gameserver_v3_latest.bak'
WITH REPLACE, NORECOVERY;
```

**Action 3: Manual data sync**
```sql
-- If corruption limited to subset of records
TRUNCATE TABLE CallCard_Corrupted;

-- Copy from backup table
INSERT INTO CallCard_Corrupted
SELECT * FROM CallCard_Backup
WHERE last_modified_date > @corruption_start_time;
```

### Full System Rollback (Hours)

**Scenario:** Unrecoverable issues, need to restore complete state

**Prerequisites:**
- Database backups maintained
- Git tags marking stable commits
- Configuration snapshots

**Rollback Steps:**

1. **Stop microservice**
   ```bash
   systemctl stop callcard-service
   ```

2. **Restore database snapshot**
   ```bash
   # Restore to pre-migration state
   RESTORE DATABASE gameserver_v3
   FROM DISK = '/backup/pre-migration/gameserver_v3_full.bak'
   WITH REPLACE;
   ```

3. **Restore monolith code**
   ```bash
   cd /opt/gameserver_v3
   svn revert -R .
   svn update  # Restore to known-good revision
   mvn clean install
   ```

4. **Restart monolith services**
   ```bash
   systemctl stop tomcat
   systemctl start tomcat
   sleep 30  # Wait for startup
   ```

5. **Reroute traffic**
   ```nginx
   # Update API Gateway to 100% monolith
   upstream callcard_active {
       server 192.168.1.50:8080;  # Monolith only
   }
   systemctl reload nginx
   ```

6. **Verify functionality**
   ```bash
   # Run smoke tests against monolith
   curl -X GET http://api.saicon.local/cxf/CallCard?wsdl
   curl -X POST http://api.saicon.local/cxf/CallCard \
     -H "Content-Type: application/json" \
     -d @test_payloads/sanity_check.json
   ```

7. **Post-incident review**
   - Document root cause
   - Update runbooks
   - Improve monitoring/alerting
   - Consider version pinning for dependencies

### Rollback Decision Criteria

**Automatic Rollback Triggers:**
```yaml
rollback:
  triggers:
    - errorRatePercent: 5          # > 5% errors
      duration: 5m                 # for 5+ minutes
      action: immediate_rollback

    - responseTimeP99Ms: 5000      # 99th percentile > 5 seconds
      duration: 10m                # for 10+ minutes
      action: immediate_rollback

    - databaseConnections: 95      # > 95% pool used
      duration: 15m                # for 15+ minutes
      action: circuit_breaker_engaged

    - uncaughtExceptions: 100      # 100+ exceptions
      timeWindow: 1h               # in 1 hour
      action: page_on_call
```

---

## Testing Migration

### Unit Testing

**CallCard Entity Tests**
```java
@RunWith(SpringRunner.class)
@DataJpaTest
public class CallCardEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CallCardRepository callCardRepo;

    @Test
    public void testCallCardPersistence() {
        // Create entity
        CallCard cc = new CallCard();
        cc.setNumber("CC-2025-001");
        cc.setStatus(CallCardStatus.ACTIVE);

        // Persist
        entityManager.persistAndFlush(cc);

        // Retrieve
        CallCard retrieved = callCardRepo.findByNumber("CC-2025-001");

        // Assert
        assertNotNull(retrieved);
        assertEquals("CC-2025-001", retrieved.getNumber());
    }

    @Test
    public void testCallCardValidation() {
        CallCard cc = new CallCard();
        cc.setNumber(null);  // Invalid

        Set<ConstraintViolation<CallCard>> violations =
            validator.validate(cc);

        assertTrue(violations.size() > 0);
    }
}
```

### Integration Testing

**SOAP Service Integration Test**
```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CallCardSoapServiceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebApplicationContext context;

    private Client client;
    private ICallCardService callCardService;

    @Before
    public void setup() throws Exception {
        // Create CXF client
        String address = "http://localhost:" + port + "/cxf/CallCard";
        ClientFactoryBean factory = new ClientFactoryBean();
        factory.setAddress(address);
        factory.setServiceClass(ICallCardService.class);
        client = factory.create();
        callCardService = (ICallCardService) client;
    }

    @Test
    public void testCreateCallCardViaSoap() {
        CreateCallCardRequest req = new CreateCallCardRequest();
        req.setNumber("CC-2025-001");
        req.setAmount(new BigDecimal("1000.00"));

        CallCardDTO response = callCardService.createCallCard(req);

        assertNotNull(response);
        assertEquals("CC-2025-001", response.getNumber());
    }
}
```

### REST API Testing

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CallCardRestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testGetCallCard() {
        String url = "http://localhost:" + port + "/rest/callcard/1";
        ResponseEntity<CallCardDTO> response =
            restTemplate.getForEntity(url, CallCardDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testCreateCallCard() {
        String url = "http://localhost:" + port + "/rest/callcard";
        CallCardDTO dto = new CallCardDTO();
        dto.setNumber("CC-2025-002");

        ResponseEntity<CallCardDTO> response =
            restTemplate.postForEntity(url, dto, CallCardDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
```

### Data Consistency Testing

**Comparison Test: Monolith vs. Microservice**
```java
public class DataConsistencyTest {

    @Autowired
    private MonolithCallCardService monolithService;

    @Autowired
    private MicroserviceCallCardService microserviceService;

    @Test
    public void testDataConsistency() throws Exception {
        // Get same record from both systems
        Long callCardId = 1L;

        CallCardDTO monolithDto = monolithService.getCallCard(callCardId);
        CallCardDTO microserviceDto = microserviceService.getCallCard(callCardId);

        // Compare all fields
        assertEquals(monolithDto.getNumber(), microserviceDto.getNumber());
        assertEquals(monolithDto.getStatus(), microserviceDto.getStatus());
        assertEquals(monolithDto.getAmount(), microserviceDto.getAmount());

        // Compare as JSON for complete structure validation
        String monolithJson = objectMapper.writeValueAsString(monolithDto);
        String microserviceJson = objectMapper.writeValueAsString(microserviceDto);

        JSONAssert.assertEquals(monolithJson, microserviceJson,
            JSONCompareMode.LENIENT);
    }
}
```

### Load Testing

**JMeter Configuration**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan>
      <elementProp name="TestPlan.user_defined_variables"/>
    </TestPlan>

    <ThreadGroup guiclass="ThreadGroupGui">
      <elementProp name="ThreadGroup.main_controller"/>
      <stringProp name="ThreadGroup.num_threads">100</stringProp>
      <stringProp name="ThreadGroup.ramp_time">60</stringProp>
      <elementProp name="ThreadGroup.scheduler"/>
      <stringProp name="ThreadGroup.duration">600</stringProp>
    </ThreadGroup>

    <ConfigTestElement guiclass="HttpConfigGui">
      <stringProp name="HTTPSampler.domain">${TARGET_HOST}</stringProp>
      <stringProp name="HTTPSampler.port">${TARGET_PORT}</stringProp>
    </ConfigTestElement>

    <HTTPSamplerProxy guiclass="HttpTestSampleGui">
      <stringProp name="HTTPSampler.path">/cxf/CallCard</stringProp>
      <stringProp name="HTTPSampler.method">POST</stringProp>
    </HTTPSamplerProxy>
  </hashTree>
</jmeterTestPlan>
```

**Run Load Test:**
```bash
jmeter -n -t callcard_load_test.jmx \
  -Jhost=callcard-staging \
  -Jport=8081 \
  -l results.jtl \
  -e -o ./report

# Expected Results:
# - Avg Response Time: < 500ms
# - 95th Percentile: < 1000ms
# - 99th Percentile: < 2000ms
# - Error Rate: < 0.1%
# - Throughput: > 100 req/sec
```

### Chaos Engineering Testing

**Test Resilience with Failure Simulation**
```java
@Test
public void testDatabaseFailureResilience() {
    // Simulate database unavailability
    simulateDatabaseDown();

    // Expect circuit breaker to trip
    CircuitBreaker cb = resilience4jRegistry.getCircuitBreaker("callcard-db");

    try {
        callCardService.getCallCard(1L);
    } catch (CallServiceException e) {
        // Expected: Circuit breaker prevents cascade
        assertEquals(CallBreakerState.OPEN, cb.getState());
    }

    // Simulate recovery
    simulateDatabaseUp();
    Thread.sleep(5000);  // Wait for half-open state

    // Service should recover
    CallCardDTO result = callCardService.getCallCard(1L);
    assertNotNull(result);
}

@Test
public void testConnectionPoolExhaustion() {
    // Fill connection pool
    List<Connection> connections = drainConnectionPool();

    // New requests should timeout/fail gracefully
    try {
        callCardService.getCallCard(1L);
        fail("Should have thrown exception");
    } catch (ConnectionTimeoutException e) {
        // Expected
        assertTrue(e.getMessage().contains("connection pool"));
    } finally {
        // Release connections
        connections.forEach(conn -> {
            try { conn.close(); } catch (Exception e) {}
        });
    }
}
```

---

## Production Cutover Plan

### Pre-Cutover Checklist (48 Hours Before)

- [ ] Verify microservice running stably in staging for 72+ hours
- [ ] Database backups taken and tested
- [ ] Rollback procedures documented and rehearsed
- [ ] Incident response team briefed and on-call
- [ ] Monitoring dashboards created and tested
- [ ] Communication plan sent to stakeholders
- [ ] Load balancer/API Gateway configuration reviewed
- [ ] DNS/proxy configuration updated and tested
- [ ] Health check endpoints verified on both systems
- [ ] Database connection pooling settings optimized
- [ ] Log aggregation pipeline tested
- [ ] All firewall rules and security groups verified
- [ ] Deployment runbook reviewed by team
- [ ] Database migration scripts tested (if needed)
- [ ] Cache warming strategy documented

### Day-Of Cutover (Phase 2.1 - Switch to 5% Traffic)

**Time Window:** 2:00 AM - 6:00 AM (low-traffic period)

**T-0:00 - Pre-Flight Check (2:00 AM)**
```bash
# Verify systems are operational
curl -X GET http://callcard-prod:8081/actuator/health
curl -X GET http://gameserver-prod:8080/cxf/CallCard?wsdl

# Check database connectivity
sqlcmd -S db-prod.saicon.local -U callcard_svc -P ... -Q "SELECT @@VERSION"

# Verify monitoring stack
# - Prometheus targets healthy
# - Log aggregation receiving data
# - Alert rules functional

# Confirm incident response team availability
# - On-call engineer ready
# - Management notified
# - War room established (Slack/Teams channel)
```

**T+0:10 - Start Traffic Shift (2:10 AM)**
```bash
# Update nginx configuration
cat > /etc/nginx/conf.d/callcard-routing.conf << 'EOF'
upstream callcard_microservice {
    server 192.168.1.100:8081 weight=5;
    server 192.168.1.101:8081 weight=5 backup;
}
upstream gameserver_monolith {
    server 192.168.1.50:8080 weight=95;
}

server {
    location ~ ^/cxf/CallCard {
        proxy_pass http://callcard_microservice;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF

# Test configuration
nginx -t

# Reload without dropping connections
systemctl reload nginx

# Verify routing
curl -v http://api.saicon.local/cxf/CallCard?wsdl
```

**T+0:30 - Monitor (2:30 AM - 4:00 AM)**
```bash
# Watch metrics continuously
# - Error rate (target: < 1%)
# - Response time P95 (target: < 1000ms)
# - Database connections (target: < 80%)
# - CPU/Memory (target: < 70%)

# Check logs for anomalies
tail -f /var/log/callcard-microservice/app.log | grep -i "ERROR\|WARN"

# Query production database for issues
SELECT COUNT(*), status FROM request_log
WHERE created_date > DATEADD(minute, -30, GETUTCDATE())
GROUP BY status;

# Compare with monolith
SELECT COUNT(*), status FROM request_log_monolith
WHERE created_date > DATEADD(minute, -30, GETUTCDATE())
GROUP BY status;
```

**T+1:30 - Assessment Point (3:30 AM)**

**If metrics are healthy:**
- Continue monitoring
- Prepare for next phase

**If issues detected:**
```bash
# Option 1: Immediate rollback (< 5 min recovery)
cat > /etc/nginx/conf.d/callcard-routing.conf << 'EOF'
upstream callcard_active {
    server 192.168.1.50:8080;  # Monolith 100%
}
server {
    location ~ ^/cxf/CallCard {
        proxy_pass http://callcard_active;
    }
}
EOF
systemctl reload nginx

# Option 2: Slow down traffic reduction
# Reduce microservice weight from 5 to 2
# Monitor for 1+ hour before continuing
```

**T+2:00 - Final Approval (4:00 AM)**

If all metrics nominal for 2 hours:
- Document cutover time
- Log final metrics snapshot
- Create post-incident review ticket
- Standby for ongoing monitoring

### Weeks After Cutover

**Week 1: Intensive Monitoring**
- Daily metrics review meeting
- Error log analysis
- Performance baseline comparison
- Customer issue tracking

**Week 2: Stabilization**
- Monitor unusual traffic patterns
- Validate integration points working
- Confirm no data consistency issues
- Begin planning Phase 3

**Week 4: Cleanup Phase Begins**
- Archive monolith CallCard code
- Remove from deployment procedures
- Update documentation

---

## Post-Migration Validation

### Data Validation Report

**Generate data consistency report:**
```sql
-- Record counts comparison
SELECT
    'CallCard' as TableName,
    (SELECT COUNT(*) FROM CallCard) as RecordCount,
    (SELECT COUNT(DISTINCT callcard_id) FROM CallCard) as UniqueIDs,
    (SELECT MAX(last_modified_date) FROM CallCard) as LastModified
UNION ALL
SELECT
    'CallCardDetail',
    COUNT(*), COUNT(DISTINCT detail_id), MAX(last_modified_date)
FROM CallCardDetail;

-- Data integrity checks
SELECT
    COUNT(*) as OrphanDetails
FROM CallCardDetail cd
LEFT JOIN CallCard c ON cd.callcard_id = c.callcard_id
WHERE c.callcard_id IS NULL;

-- Checksum validation
SELECT
    CHECKSUM_AGG(CHECKSUM(callcard_id, number, status_id))
FROM CallCard;
```

### Performance Validation

**Response time comparison:**
```sql
SELECT
    operation,
    COUNT(*) as RequestCount,
    AVG(DATEDIFF(ms, start_time, end_time)) as AvgResponseMs,
    MAX(DATEDIFF(ms, start_time, end_time)) as MaxResponseMs,
    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY DATEDIFF(ms, start_time, end_time))
        OVER () as P95ResponseMs
FROM request_metrics
WHERE service = 'callcard'
  AND response_date >= DATEADD(day, -7, GETUTCDATE())
GROUP BY operation
ORDER BY AvgResponseMs DESC;
```

### API Compatibility Validation

**Compare WSDL between systems:**
```bash
# Download WSDL from microservice
curl -s http://callcard-prod:8081/cxf/CallCard?wsdl > callcard_microservice.wsdl

# Download WSDL from any remaining legacy system
curl -s http://legacy-system/cxf/CallCard?wsdl > callcard_legacy.wsdl

# Compare schemas
diff -u <(grep -oP '(?<=<xs:element)[^>]*' callcard_legacy.wsdl | sort) \
        <(grep -oP '(?<=<xs:element)[^>]*' callcard_microservice.wsdl | sort)
```

### Security Validation

**Verify security posture:**
```bash
# Test authentication
curl -X POST http://callcard-prod:8081/cxf/CallCard \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer invalid_token" \
  # Should return 401

# Test HTTPS enforcement
curl -v http://callcard-prod:8081/...
# Should redirect to https:// or return 403

# Check security headers
curl -I https://callcard-prod:8081/cxf/CallCard
# Should have: Strict-Transport-Security, X-Frame-Options, Content-Security-Policy

# Test rate limiting
for i in {1..100}; do
  curl http://callcard-prod:8081/cxf/CallCard &
done
# Should begin returning 429 (Too Many Requests) after threshold
```

### Customer UAT Validation

**UAT Test Plan:**
```markdown
## CallCard Microservice UAT

### Functional Tests
- [ ] Create new CallCard
- [ ] View existing CallCard
- [ ] Update CallCard details
- [ ] Cancel CallCard
- [ ] Generate CallCard report
- [ ] Export CallCard data
- [ ] Print CallCard

### Integration Tests
- [ ] User lookup integration
- [ ] Sales order integration
- [ ] Address book integration
- [ ] App settings lookup

### Performance Tests
- [ ] Load 1000 records: < 5 seconds
- [ ] Search across 10,000 records: < 2 seconds
- [ ] Generate report: < 30 seconds

### User Acceptance
- [ ] All features working as expected
- [ ] No data loss detected
- [ ] Performance acceptable
- [ ] UI responsive
- [ ] Signed off by business owner
```

### Operational Validation

**Verify operational readiness:**
```bash
# Test health endpoint
curl -s http://callcard-prod:8081/actuator/health | jq .

# Check metrics exposure
curl -s http://callcard-prod:8081/actuator/metrics | jq .

# Verify log output format
tail -100 /var/log/callcard-microservice/app.log

# Test graceful shutdown
kill -TERM <pid>  # Should shutdown cleanly within 30s

# Verify database connection pooling
curl -s http://callcard-prod:8081/actuator/metrics/hikaricp.connections | jq .

# Confirm no memory leaks over 24 hours
# Monitor: free memory trend
# Expected: Stable free memory, no continuous growth
```

### Knowledge Transfer Validation

**Ensure team readiness:**
- [ ] Operations team trained on microservice deployment
- [ ] Support team briefed on troubleshooting procedures
- [ ] Runbooks documented and accessible
- [ ] Escalation procedures defined
- [ ] Post-incident review process established
- [ ] Metrics dashboard accessible to team
- [ ] Alerting rules understood and tested

---

## Monitoring & Observability

### Key Metrics to Track

**Application Metrics:**
```yaml
metrics:
  http_requests_total:
    labels: [service, method, path, status]
    target: track all requests

  http_request_duration_seconds:
    labels: [service, method, path]
    target: P95 < 1000ms, P99 < 2000ms

  callcard_operations_total:
    labels: [operation, status]
    target: 0% failure rate

  database_connections:
    labels: [pool_name]
    target: < 80% utilization

  cache_hits_total:
    labels: [cache_name]
    target: > 80% hit rate

business_metrics:
  callcard_created_total:
    target: track business volume

  callcard_revenue_total:
    target: monitor transaction value
```

### Alerting Rules

**Critical Alerts (Page On-Call):**
- Service down for > 5 minutes
- Error rate > 5% for > 5 minutes
- Database connection pool exhausted
- Unhandled exceptions > 100/hour

**Warning Alerts (Slack Notification):**
- Error rate 1-5% for > 10 minutes
- Response time P95 > 1500ms for > 15 minutes
- Database connections > 70% for > 20 minutes
- CPU usage > 80% for > 30 minutes

---

## Conclusion

This migration guide provides a comprehensive, risk-mitigated approach to extracting the CallCard service from the gameserver_v3 monolith. By following the strangler pattern with gradual traffic shifting, extensive testing, and robust rollback procedures, you can achieve zero-downtime migration with minimal customer impact.

**Key Success Factors:**
1. Extensive testing at each phase
2. Continuous monitoring and alerting
3. Documented rollback procedures
4. Clear communication with stakeholders
5. Experienced incident response team on standby
6. Phased approach allowing for quick course correction

**Next Steps:**
1. Schedule migration kick-off meeting
2. Prepare staging environment
3. Conduct team training
4. Execute Phase 1 (Weeks 1-4)
5. Monitor Phase 2 traffic shifting closely
6. Plan follow-up microservice extractions (e.g., Quiz, ERP)
