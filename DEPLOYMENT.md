# CallCard Microservice Deployment Guide

## Overview

This document provides comprehensive deployment instructions for the CallCard Microservice, a Spring Boot 2.7.x microservice that manages call card functionality extracted from the gameserver_v3 platform. The service provides both SOAP (Apache CXF) and REST (Jersey) web service interfaces with SQL Server backend integration.

**Key Characteristics:**
- **Framework**: Spring Boot 2.7.18
- **Build System**: Maven 3.x
- **Runtime**: Java 8+
- **Database**: Microsoft SQL Server 2008+
- **Web Services**: Apache CXF 3.5.x (SOAP), Jersey 2.39.1 (REST)
- **ORM**: Hibernate 5.6.15
- **Deployment**: WAR or Docker container

---

## 1. Prerequisites

### 1.1 System Requirements

**Development/Testing Environment:**
- Windows 10/Server 2019+ or Linux (Ubuntu 18.04+)
- 4GB RAM minimum (8GB recommended)
- 10GB free disk space
- Network connectivity to SQL Server instance

**Production Environment:**
- Linux Server (Ubuntu 20.04 LTS or equivalent) or Windows Server 2019+
- 8GB RAM minimum (16GB recommended for high traffic)
- 20GB free disk space
- Dedicated SQL Server 2012+ instance with backup strategy
- HTTPS/TLS support

### 1.2 Required Software

#### Java Development Kit (JDK)

```bash
# Verify Java 8+ installation
java -version
javac -version

# Expected output:
# openjdk version "1.8.0_XXX" or higher
# Java HotSpot(TM) 64-Bit Server VM (build 25.XXX)
```

**Download Links:**
- Oracle JDK 8: https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
- OpenJDK 8: https://adoptopenjdk.net/
- Eclipse Temurin 8: https://adoptium.net/

**Installation (Windows):**
```powershell
# Set JAVA_HOME environment variable
setx JAVA_HOME "C:\Program Files\Java\jdk1.8.0_XXX"

# Verify installation
java -version
```

**Installation (Linux):**
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-8-jdk-headless

# Verify
java -version
```

#### Maven

```bash
# Verify Maven 3.6+ installation
mvn --version

# Expected output:
# Apache Maven 3.6.x
# Maven home: /usr/local/apache-maven-3.6.x
```

**Download:** https://maven.apache.org/download.cgi

**Installation (Windows):**
```powershell
# Add Maven to PATH
setx PATH "%PATH%;C:\apache-maven-3.9.5\bin"

# Verify
mvn --version
```

**Installation (Linux):**
```bash
# Extract Maven
sudo tar -xf apache-maven-3.9.5-bin.tar.gz -C /opt/
sudo mv /opt/apache-maven-3.9.5 /opt/maven

# Add to PATH
echo 'export PATH="/opt/maven/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc

# Verify
mvn --version
```

#### SQL Server

**Connection Details Required:**
- Server hostname/IP
- Port (default: 1433)
- Database name
- Username and password
- TCP/IP enabled

**Connection String Format:**
```
jdbc:sqlserver://[host]:[port];databaseName=[database];encrypt=[true|false];trustServerCertificate=[true|false]
```

**Test Connection (Command Line):**
```powershell
# Windows - Using SQL Server Management Tools (SSMS)
# Use Object Explorer to verify connection

# Or via sqlcmd (if installed)
sqlcmd -S localhost -U sa -P password -Q "SELECT @@VERSION;"
```

**Connection Test (Linux/via Docker):**
```bash
# Using sqlcmd-docker
docker run -it --rm mcr.microsoft.com/mssql/server:2019-latest /opt/mssql-tools/bin/sqlcmd \
  -S sqlserver:1433 -U sa -P password -Q "SELECT @@VERSION;"
```

#### Git (Optional)

```bash
# Verify Git installation
git --version

# Expected output: git version 2.x.x or higher
```

#### Docker and Docker Compose (For Containerized Deployment)

```bash
# Verify Docker installation
docker --version
docker-compose --version

# Expected output:
# Docker version 20.10.x or higher
# Docker Compose version 1.29.x or higher
```

---

## 2. Build Instructions

### 2.1 Project Structure

The CallCard Microservice is organized as a Maven multi-module project:

```
tradetool_middleware/
├── pom.xml (Parent POM - Spring Boot 2.7.18)
├── callcard-entity/
│   └── pom.xml (JPA entities module)
├── callcard-ws-api/
│   └── pom.xml (Web service interface definitions)
├── callcard-components/
│   └── pom.xml (Business logic components)
├── callcard-service/
│   └── pom.xml (Service orchestration layer)
├── CallCard_Server_WS/
│   ├── pom.xml (Main WAR module)
│   ├── src/main/java/
│   │   └── com/saicon/callcard/
│   │       ├── CallCardMicroserviceApplication.java
│   │       ├── config/
│   │       │   ├── ComponentConfiguration.java
│   │       │   ├── CxfConfiguration.java (SOAP)
│   │       │   ├── DataSourceConfiguration.java
│   │       │   ├── JerseyConfiguration.java (REST)
│   │       │   └── ... (other configurations)
│   │       └── security/
│   │           └── SessionAuthenticationInterceptor.java
│   └── src/main/resources/
│       ├── application.yml
│       ├── db/
│       │   └── migration/
│       │       └── V003__create_transaction_history.sql
│       └── logback.xml
├── database/
│   └── migrations/
│       └── V001__initial_schema_verification.sql
├── docker-compose.yml
├── Dockerfile
└── DEPLOYMENT.md (This file)
```

### 2.2 Build Order and Dependencies

Modules must be built in the following order due to dependencies:

1. **callcard-entity** - JPA entities (no dependencies on other modules)
2. **callcard-ws-api** - Web service interfaces (depends on callcard-entity)
3. **callcard-components** - Business components and DAOs (depends on entity, ws-api)
4. **callcard-service** - Service layer (depends on components, entity)
5. **CallCard_Server_WS** - WAR deployment artifact (depends on all above)

### 2.3 Development Build

**Building from Source:**

```bash
# Navigate to project root
cd C:\Users\dimit\tradetool_middleware
# or
cd /home/user/tradetool_middleware

# Full build with default dev profile
mvn clean install

# Output: CallCard_Server_WS/target/CallCard_Server_WS.war
```

**Build Output Details:**

```bash
# After successful build, you'll see:
[INFO] BUILD SUCCESS
[INFO] Total time: 1.234 s
[INFO] Finished at: 2025-12-22T10:30:00Z
[INFO] Final Memory: 512M/1024M
```

**Cleaning Up (Remove build artifacts):**

```bash
# Clean all target directories
mvn clean

# Clean and rebuild
mvn clean install
```

### 2.4 Environment-Specific Builds

The project supports Maven profiles for different environments:

#### Development Build

```bash
# Default profile (already active)
mvn clean install -Pdev

# Or without explicit profile (dev is default)
mvn clean install
```

**Generated Artifact:** `CallCard_Server_WS/target/CallCard_Server_WS.war`

**Configuration Used:** `src/main/config/dev/application-dev.yml`

**Characteristics:**
- SQL show-sql: true
- Hibernate ddl-auto: validate
- Default logging level: DEBUG
- Local database: localhost:1433

#### Staging Build

```bash
mvn clean install -Ppmi-staging-v3-1
```

**Configuration Used:** `src/main/config/pmi-staging-v3-1/application-staging.yml`

**Characteristics:**
- SQL show-sql: false
- Encrypted database connection
- Logging level: INFO
- Remote database: staging-db.internal

#### Production Build

```bash
mvn clean install -Ppmi-production-v3-1
```

**Configuration Used:** `src/main/config/pmi-production-v3-1/application-production.yml`

**Characteristics:**
- SQL show-sql: false
- Hibernate ddl-auto: none (no schema changes)
- Encrypted database connection
- Logging level: WARN (minimal logging)
- Connection pool size: 50

### 2.5 Build with Specific Options

**Skip Tests During Build:**

```bash
# Build faster by skipping test phase
mvn clean install -DskipTests

# Build even if tests fail
mvn clean install -DskipTests -fae
```

**Build Single Module:**

```bash
# Build only CallCard Server WS
mvn clean install -pl CallCard_Server_WS -am
# -pl = project list
# -am = also make (dependencies)

# Build only callcard-components
mvn clean install -pl callcard-components -am
```

**Check Dependencies:**

```bash
# Display dependency tree
mvn dependency:tree

# Find unused dependencies
mvn dependency:analyze

# Display security vulnerabilities
mvn org.owasp:dependency-check-maven:check
```

### 2.6 Troubleshooting Build Issues

**"Cannot find symbol" Compilation Error:**

```bash
# Clean Maven cache and rebuild
mvn clean install -U

# -U = Update snapshots
```

**"Dependency Resolution Failed":**

```bash
# Clear local repository cache
rm -rf ~/.m2/repository/com/saicon
# or on Windows:
rmdir /s %USERPROFILE%\.m2\repository\com\saicon

# Rebuild with fresh downloads
mvn clean install -U
```

**"Java Compilation Error (source 1.6 too old)":**

```bash
# Ensure Java 8+ is in use
java -version

# Set JAVA_HOME if needed
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
# or on Windows:
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_XXX
```

**Tests Fail but Build Completes:**

```bash
# Run only integration tests
mvn clean install -Dgroups=integration

# Skip all tests
mvn clean install -DskipTests
```

---

## 3. Configuration Options

### 3.1 Application Configuration Files

The application configuration is managed through YAML files with profile-specific overrides:

**Base Configuration:** `CallCard_Server_WS/src/main/resources/application.yml`

**Environment-Specific Overrides:**
- Development: `CallCard_Server_WS/src/main/config/dev/application-dev.yml`
- Staging: `CallCard_Server_WS/src/main/config/pmi-staging-v3-1/application-staging.yml`
- Production: `CallCard_Server_WS/src/main/config/pmi-production-v3-1/application-production.yml`

### 3.2 Database Configuration

**Connection Pool Settings:**

```yaml
spring:
  datasource:
    # SQL Server JDBC Connection
    url: jdbc:sqlserver://[host]:[port];databaseName=[database];encrypt=false;trustServerCertificate=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver

    hikari:
      maximum-pool-size: 20      # Max connections (production: 50)
      minimum-idle: 5            # Min idle connections
      idle-timeout: 300000       # 5 minutes
      connection-timeout: 20000  # 20 seconds
      max-lifetime: 1200000      # 20 minutes
```

**Key Parameters:**

| Parameter | Development | Staging | Production | Notes |
|-----------|-------------|---------|------------|-------|
| `maximum-pool-size` | 20 | 30 | 50 | Adjust based on expected load |
| `minimum-idle` | 5 | 10 | 10 | Connections kept ready |
| `connection-timeout` | 20000ms | 20000ms | 20000ms | Fail-fast timeout |
| `max-lifetime` | 20min | 20min | 20min | Database connection timeout |

**Connection String Examples:**

```yaml
# Development (Local)
url: jdbc:sqlserver://localhost:1433;databaseName=gameserver_v3_dev;encrypt=false;trustServerCertificate=true

# Staging (Remote)
url: jdbc:sqlserver://staging-db.internal:1433;databaseName=gameserver_v3_staging;encrypt=true;trustServerCertificate=false

# Production (Encrypted)
url: jdbc:sqlserver://prod-db.saicon.com:1433;databaseName=gameserver_v3;encrypt=true;trustServerCertificate=false
```

### 3.3 JPA/Hibernate Configuration

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate          # Schema validation mode
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    show-sql: false              # Log all SQL (development only: true)
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServer2012Dialect
        format_sql: true
        use_sql_comments: true
        jdbc.batch_size: 50       # Batch processing optimization
        order_inserts: true
        order_updates: true

        # Second-level cache (EhCache)
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region.factory_class: org.hibernate.cache.ehcache.EhCacheRegionFactory
```

**ddl-auto Modes:**

| Mode | Behavior | Use Case |
|------|----------|----------|
| `validate` | Check schema exists (fail if not) | Production (safest) |
| `update` | Auto-create/update schema | Development only |
| `create` | Drop and recreate schema | Testing only |
| `create-drop` | Create on startup, drop on shutdown | Integration tests |
| `none` | No action | Manual schema management |

### 3.4 Server Configuration

```yaml
server:
  port: 8080                              # Service port
  servlet:
    context-path: /callcard              # Application context path
  compression:
    enabled: true                         # Enable gzip compression
    mime-types: application/json,application/xml,text/xml
    min-response-size: 1024               # Min size to compress
  error:
    include-message: always
    include-stacktrace: never              # never (prod), always (dev)
```

**Accessing Services:**

```
Health Check:     http://localhost:8080/callcard/actuator/health
Metrics:          http://localhost:8080/callcard/actuator/metrics
API Docs:         http://localhost:8080/callcard/swagger-ui.html
SOAP Services:    http://localhost:8080/callcard/cxf/services
REST Services:    http://localhost:8080/callcard/rest/*
```

### 3.5 SOAP Web Services (CXF) Configuration

```yaml
cxf:
  path: /cxf                              # CXF servlet path
  servlet:
    init:
      service-list-path: /services        # Services list endpoint
```

**Service Endpoints:**

```
SOAP WSDL:   http://localhost:8080/callcard/cxf/[ServiceName]?wsdl
Example:     http://localhost:8080/callcard/cxf/CallCardService?wsdl
```

### 3.6 REST Services (Jersey) Configuration

```yaml
# Configured in: com.saicon.callcard.config.JerseyConfiguration

# Base REST path: /rest
# Example endpoints:
# GET    /rest/callcards
# GET    /rest/callcards/{id}
# POST   /rest/callcards
# PUT    /rest/callcards/{id}
# DELETE /rest/callcards/{id}
```

### 3.7 Cache Configuration

```yaml
spring:
  cache:
    type: caffeine              # Or: ehcache, redis, none
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m  # 1000 items, 5min TTL
```

**CallCard-Specific Cache:**

```yaml
callcard:
  session-cache:
    enabled: true
    ttl-seconds: 300            # 5 minutes (dev), 600 (prod)
    max-size: 10000             # (dev), 50000 (prod)
```

### 3.8 TALOS Core Integration

```yaml
callcard:
  talos-core:
    # Session validation with TALOS Core Game Server
    session-validation-url: http://localhost:8080/Game_Server_WS/cxf/GAMEInternalService
    connection-timeout: 5000
    read-timeout: 10000

  circuit-breaker:
    failure-rate-threshold: 50   # Fail-open at 50% error rate
    wait-duration-in-open-state: 30000  # 30 seconds
    permitted-calls-in-half-open-state: 3
```

### 3.9 Resilience4j Circuit Breaker

```yaml
resilience4j:
  circuitbreaker:
    instances:
      talosCore:
        registerHealthIndicator: true
        slidingWindowSize: 10            # Last 10 calls
        failureRateThreshold: 50         # 50% failures trigger open
        waitDurationInOpenState: 30s     # Wait 30s before trying again
        permittedNumberOfCallsInHalfOpenState: 3  # Test with 3 calls
```

### 3.10 Management Endpoints

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics    # Expose these endpoints
  endpoint:
    health:
      show-details: when_authorized    # Show health details only if authorized
```

**Available Endpoints:**

```
GET /callcard/actuator/health              # Health status
GET /callcard/actuator/info                # Application info
GET /callcard/actuator/metrics             # All metrics
GET /callcard/actuator/metrics/{metric}    # Specific metric
```

### 3.11 Logging Configuration

**Application Configuration:**

```yaml
logging:
  level:
    root: INFO                              # Root logger level
    com.saicon.games.callcard: DEBUG       # CallCard service logs
    org.hibernate.SQL: DEBUG                # SQL statements
    org.apache.cxf: INFO                    # SOAP logs
    org.glassfish.jersey: INFO              # REST logs
```

**File Configuration:** `CallCard_Server_WS/src/main/resources/logback.xml`

```xml
<configuration>
  <!-- Console appender for development -->
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <!-- File appender for production -->
  <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/callcard.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
      <fileNamePattern>logs/callcard.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
      <maxFileSize>10MB</maxFileSize>
      <maxHistory>30</maxHistory>
      <totalSizeCap>1GB</totalSizeCap>
    </rollingPolicy>
    <encoder>
      <pattern>%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <root level="INFO">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
  </root>
</configuration>
```

### 3.12 Environment Variables

**Required Environment Variables:**

```bash
# Database credentials (required)
export DB_USERNAME=sa
export DB_PASSWORD=YourSecurePassword123!

# TALOS Core integration (optional, defaults provided)
export TALOS_CORE_URL=http://talos-core:8080

# Server configuration (optional)
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=production
```

**Setting Environment Variables:**

**Windows (PowerShell):**
```powershell
[Environment]::SetEnvironmentVariable("DB_USERNAME", "sa", "Machine")
[Environment]::SetEnvironmentVariable("DB_PASSWORD", "YourSecurePassword", "Machine")
```

**Linux (Bash):**
```bash
export DB_USERNAME=sa
export DB_PASSWORD=YourSecurePassword123!

# Persistent (add to ~/.bashrc or /etc/profile):
echo 'export DB_USERNAME=sa' >> ~/.bashrc
source ~/.bashrc
```

---

## 4. Database Setup Requirements

### 4.1 SQL Server Instance Preparation

**Verify SQL Server Connection:**

```bash
# Windows - Using SSMS or sqlcmd
sqlcmd -S localhost -U sa -P password

# Linux - Using Docker
docker run -it --rm mcr.microsoft.com/mssql/server:2019-latest \
  /opt/mssql-tools/bin/sqlcmd -S sqlserver:1433 -U sa -P password
```

**Create Database (if not exists):**

```sql
-- Development database
CREATE DATABASE gameserver_v3_dev;
GO

-- Staging database
CREATE DATABASE gameserver_v3_staging;
GO

-- Production database
CREATE DATABASE gameserver_v3;
GO
```

**Create Service Account (Recommended):**

```sql
-- Create CallCard service account
CREATE LOGIN callcard_svc WITH PASSWORD = 'ComplexPassword123!@#';
GO

-- Assign to databases
USE gameserver_v3_dev;
CREATE USER callcard_svc FROM LOGIN callcard_svc;
ALTER ROLE db_owner ADD MEMBER callcard_svc;
GO

USE gameserver_v3_staging;
CREATE USER callcard_svc FROM LOGIN callcard_svc;
ALTER ROLE db_owner ADD MEMBER callcard_svc;
GO

USE gameserver_v3;
CREATE USER callcard_svc FROM LOGIN callcard_svc;
ALTER ROLE db_owner ADD MEMBER callcard_svc;
GO
```

### 4.2 Schema Verification and Initialization

**Database Schema Requirements:**

The CallCard Microservice expects tables from the gameserver_v3 schema to exist:

**Required Tables:**
- CallCard-related tables (from WS_API_CallCard module)
- User tables (from user management)
- Organization tables (multi-tenant support)
- Transaction history tables
- Supporting entities (address books, resources, etc.)

**Migration Scripts:**

Located in: `C:\Users\dimit\tradetool_middleware\database\migrations\`

```bash
# Run migrations manually with your database tool
sqlcmd -S localhost -U sa -P password -d gameserver_v3 -i V001__initial_schema_verification.sql
```

**Verification Query:**

```sql
-- Verify CallCard tables exist
SELECT TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'dbo'
  AND TABLE_NAME LIKE '%CallCard%'
ORDER BY TABLE_NAME;

-- Sample output:
-- CallCard
-- CallCardManagement
-- CallCardTransaction
-- etc.
```

### 4.3 Data Requirements

**Sample Data for Testing:**

```sql
USE gameserver_v3_dev;

-- Verify sample organizations exist
SELECT * FROM Organization WHERE IsActive = 1;

-- Verify sample users exist
SELECT * FROM [User] WHERE IsActive = 1 LIMIT 10;

-- If no data exists, you may need to populate from production backup
```

### 4.4 Connection Verification

**Test SQL Server Connection with Java:**

```bash
# Add sqlserverclient-test.jar to classpath
cd CallCard_Server_WS
mvn exec:java -Dexec.mainClass="com.microsoft.sqlserver.jdbc.SQLServerDriver"
```

**Alternative - Using Spring Boot:**

```bash
# Build project with test profile
mvn clean install -Pdev

# Run with spring-boot:run to test database connection
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Check for SQL connection errors in logs
# Look for: "Hibernate: HHH000030: The following Connection characteristics did not match..."
```

### 4.5 Backup and Recovery

**Regular Backup Strategy:**

```sql
-- Full backup (nightly)
BACKUP DATABASE gameserver_v3
TO DISK = 'C:\Backup\gameserver_v3_full.bak'
WITH FORMAT, COMPRESSION;

-- Transaction log backup (hourly)
BACKUP LOG gameserver_v3
TO DISK = 'C:\Backup\gameserver_v3_tlog.bak'
WITH COMPRESSION;
```

**Restore from Backup:**

```sql
-- Restore database
RESTORE DATABASE gameserver_v3
FROM DISK = 'C:\Backup\gameserver_v3_full.bak'
WITH REPLACE;
```

---

## 5. Running Locally

### 5.1 Development Server Startup

**Prerequisites:**
- Java 8+ installed and JAVA_HOME set
- Maven installed
- SQL Server running with database created
- Database credentials set in environment or application-dev.yml

**Start Development Server:**

```bash
# Navigate to CallCard_Server_WS directory
cd C:\Users\dimit\tradetool_middleware\CallCard_Server_WS

# Method 1: Using Maven
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Method 2: Using pre-built WAR (after mvn install)
cd ..
mvn clean install -Pdev
java -jar CallCard_Server_WS/target/CallCard_Server_WS.war --spring.profiles.active=dev

# Method 3: Run WAR with custom properties
java -Dspring.datasource.url="jdbc:sqlserver://localhost:1433;databaseName=gameserver_v3_dev" \
     -Dspring.datasource.username=sa \
     -Dspring.datasource.password=password \
     -jar CallCard_Server_WS/target/CallCard_Server_WS.war
```

**Expected Startup Output:**

```
==================================================
Starting CallCard Microservice
==================================================
...
INFO  [org.springframework.boot.StartupInfoLogger] Started CallCardMicroserviceApplication in X.XXX seconds
...
INFO  [org.apache.cxf.transport.servlet.CXFServlet] Load the bus from classpath:/META-INF/cxf/cxf.xml
...
==================================================
CallCard Microservice started successfully
==================================================
```

**Startup Options:**

```bash
# Run on different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# Enable all actuator endpoints
mvn spring-boot:run -Dspring-boot.run.arguments="--management.endpoints.web.exposure.include=*"

# Set log level
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.root=DEBUG"

# External properties file
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.config.location=file:./application-dev.yml"
```

### 5.2 Service Health Verification

**Health Check Endpoints:**

```bash
# Basic health status
curl http://localhost:8080/callcard/actuator/health

# Response:
# {"status":"UP"}

# Detailed health (if authorized)
curl http://localhost:8080/callcard/actuator/health?details=true

# Response:
# {
#   "status": "UP",
#   "components": {
#     "circuitBreakers": {...},
#     "db": {...},
#     "diskSpace": {...}
#   }
# }
```

**Metrics Endpoint:**

```bash
# List all available metrics
curl http://localhost:8080/callcard/actuator/metrics

# Get specific metric
curl http://localhost:8080/callcard/actuator/metrics/jvm.memory.used
```

### 5.3 Testing Web Services

**SOAP Service Testing:**

```bash
# Get WSDL
curl http://localhost:8080/callcard/cxf/CallCardService?wsdl

# Service list
curl http://localhost:8080/callcard/cxf/services
```

**REST Service Testing:**

```bash
# List all call cards
curl -X GET http://localhost:8080/callcard/rest/callcards \
  -H "Content-Type: application/json"

# Get specific call card
curl -X GET http://localhost:8080/callcard/rest/callcards/123 \
  -H "Content-Type: application/json"

# Create new call card
curl -X POST http://localhost:8080/callcard/rest/callcards \
  -H "Content-Type: application/json" \
  -d '{"cardNumber":"123456","balance":100.00}'

# Update call card
curl -X PUT http://localhost:8080/callcard/rest/callcards/123 \
  -H "Content-Type: application/json" \
  -d '{"cardNumber":"123456","balance":150.00}'

# Delete call card
curl -X DELETE http://localhost:8080/callcard/rest/callcards/123
```

**Using Postman/Insomnia:**

1. Import WSDL: Drag WSDL URL into Postman
2. Create SOAP request with body:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:tns="http://ws.callcard.saicon.com/">
  <soap:Body>
    <tns:getCallCard>
      <id>123</id>
    </tns:getCallCard>
  </soap:Body>
</soap:Envelope>
```

### 5.4 Debugging

**Enable Debug Logging:**

```bash
# Run with DEBUG level
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.root=DEBUG --logging.level.org.springframework=DEBUG"

# Or modify application-dev.yml before running:
logging:
  level:
    root: DEBUG
    com.saicon.games.callcard: DEBUG
    org.hibernate.SQL: DEBUG
    org.springframework: DEBUG
```

**Debug Remote Application (JPDA):**

```bash
# Start with debug port
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

# Connect IDE to localhost:5005 (Eclipse/IntelliJ)
```

**Log File Monitoring:**

```bash
# Follow logs in real-time
tail -f logs/callcard.log

# Filter for errors only
grep ERROR logs/callcard.log

# Watch for a specific class
grep "com.saicon.games.callcard" logs/callcard.log
```

### 5.5 Stopping Development Server

```bash
# Press Ctrl+C in terminal running the service

# Or kill by port (if stuck)
# Windows:
netstat -ano | findstr :8080
taskkill /PID <process_id> /F

# Linux:
lsof -i :8080
kill -9 <pid>
```

---

## 6. Docker Deployment

### 6.1 Docker Prerequisites

**Install Docker:**

```bash
# Windows - Download Docker Desktop
https://www.docker.com/products/docker-desktop

# Linux
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
sudo usermod -aG docker $USER

# Verify installation
docker --version
docker-compose --version
```

### 6.2 Building Docker Image

**Dockerfile (included in project):**

Located: `C:\Users\dimit\tradetool_middleware\Dockerfile`

```dockerfile
FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=CallCard_Server_WS/target/*.war
COPY ${JAR_FILE} app.war
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.war"]
```

**Build Steps:**

```bash
# 1. Build Maven project first (creates WAR)
cd C:\Users\dimit\tradetool_middleware
mvn clean install -Ppmi-staging-v3-1 -DskipTests

# 2. Build Docker image
docker build -t callcard-microservice:1.0.0 .

# 3. Verify image created
docker images | grep callcard

# Output:
# REPOSITORY               TAG      IMAGE ID      CREATED        SIZE
# callcard-microservice    1.0.0    abc123def456  2 minutes ago   500MB
```

**Tag Image for Registry:**

```bash
# For Docker Hub
docker tag callcard-microservice:1.0.0 your-docker-id/callcard-microservice:1.0.0

# For Private Registry
docker tag callcard-microservice:1.0.0 registry.example.com/callcard-microservice:1.0.0

# Push to registry
docker push your-docker-id/callcard-microservice:1.0.0
```

### 6.3 Running Container Standalone

**Run Standalone Container:**

```bash
# Run with environment variables
docker run -d \
  --name callcard-service \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SPRING_DATASOURCE_URL=jdbc:sqlserver://sqlserver:1433;databaseName=gameserver_v3 \
  -e SPRING_DATASOURCE_USERNAME=sa \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e TALOS_CORE_URL=http://talos-core:8080 \
  callcard-microservice:1.0.0
```

**Container Management:**

```bash
# View running containers
docker ps

# View all containers (including stopped)
docker ps -a

# View container logs
docker logs callcard-service
docker logs -f callcard-service  # Follow logs

# Stop container
docker stop callcard-service

# Start stopped container
docker start callcard-service

# Remove container
docker rm callcard-service

# Execute command in container
docker exec -it callcard-service bash
docker exec callcard-service curl http://localhost:8080/callcard/actuator/health
```

### 6.4 Docker Compose (Multi-Container Setup)

**docker-compose.yml (included in project):**

Located: `C:\Users\dimit\tradetool_middleware\docker-compose.yml`

```yaml
version: '3.8'
services:
  callcard-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:sqlserver://sqlserver:1433;databaseName=gameserver_v3
      - SPRING_DATASOURCE_USERNAME=sa
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    depends_on:
      - sqlserver
    networks:
      - callcard-network

  sqlserver:
    image: mcr.microsoft.com/mssql/server:2019-latest
    environment:
      - ACCEPT_EULA=Y
      - SA_PASSWORD=${DB_PASSWORD}
    ports:
      - "1433:1433"
    networks:
      - callcard-network
    volumes:
      - sqlserver-data:/var/opt/mssql

networks:
  callcard-network:
    driver: bridge

volumes:
  sqlserver-data:
```

**Start Docker Compose Stack:**

```bash
# Create .env file with secrets
echo "DB_PASSWORD=ComplexPassword123!@#" > .env

# Start services
docker-compose up -d

# Expected output:
# Creating network "tradetool_middleware_callcard-network" with driver "bridge"
# Creating tradetool_middleware_sqlserver_1
# Creating tradetool_middleware_callcard-service_1

# View status
docker-compose ps

# View logs
docker-compose logs -f
docker-compose logs -f callcard-service
docker-compose logs -f sqlserver
```

**Stop Docker Compose Stack:**

```bash
# Stop services
docker-compose stop

# Remove containers but keep volumes
docker-compose down

# Remove everything including volumes
docker-compose down -v
```

### 6.5 Health Checks in Docker

**Check Container Health:**

```bash
# Method 1: Docker health status
docker inspect --format='{{json .State.Health}}' callcard-service

# Method 2: Call health endpoint
docker exec callcard-service curl http://localhost:8080/callcard/actuator/health

# Method 3: View docker-compose health
docker-compose exec callcard-service curl http://localhost:8080/callcard/actuator/health
```

**Add Health Check to Docker Compose:**

```yaml
services:
  callcard-service:
    build: .
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/callcard/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

### 6.6 Docker Network Configuration

**Connecting Multiple Containers:**

```bash
# Services can reach each other using service name as hostname
# Example (in container):
curl http://sqlserver:1433

# To access from host:
# Windows: Use Docker Desktop VM IP
docker-machine ip

# Linux: Use localhost
http://localhost:1433

# Using named volume for persistence:
docker volume create sqlserver-data
```

### 6.7 Troubleshooting Docker

**Container Won't Start:**

```bash
# Check error logs
docker logs callcard-service

# Verify image exists
docker images | grep callcard

# Try running with interactive terminal for debugging
docker run -it --name callcard-debug callcard-microservice:1.0.0 /bin/sh
```

**Database Connection Issues:**

```bash
# Test SQL Server connectivity from container
docker exec callcard-service ping sqlserver

# Access container bash
docker exec -it callcard-service /bin/sh

# Inside container, test SQL connection
apt-get update && apt-get install -y telnet
telnet sqlserver 1433
```

**Port Conflicts:**

```bash
# Check port usage
netstat -ano | findstr :8080    # Windows
lsof -i :8080                   # Linux

# Use different port
docker run -p 8081:8080 callcard-microservice:1.0.0

# Or in docker-compose.yml:
ports:
  - "8081:8080"
```

---

## 7. Production Deployment Considerations

### 7.1 Production Build and Packaging

**Create Production Build:**

```bash
# Build with production profile
cd C:\Users\dimit\tradetool_middleware
mvn clean install -Ppmi-production-v3-1 -DskipTests

# Generate deployment artifact
mvn package -Ppmi-production-v3-1 -DskipTests
```

**Output Artifact:**
- Location: `CallCard_Server_WS/target/CallCard_Server_WS.war`
- Size: ~150-200MB

**Verify Production Configuration:**

```bash
# Check that production properties are used
unzip -l CallCard_Server_WS/target/CallCard_Server_WS.war | grep -i "application"

# Expected:
# application.yml
# BOOT-INF/classes/application-production.yml
```

### 7.2 Application Server Deployment

**Tomcat Deployment:**

```bash
# Copy WAR to Tomcat
cp CallCard_Server_WS/target/CallCard_Server_WS.war /var/lib/tomcat/webapps/

# Or using Tomcat Manager:
curl -T CallCard_Server_WS/target/CallCard_Server_WS.war \
  http://admin:password@localhost:8080/manager/text/deploy?path=/callcard
```

**JBoss/WildFly Deployment:**

```bash
# Copy to deployment directory
cp CallCard_Server_WS/target/CallCard_Server_WS.war /opt/wildfly/standalone/deployments/

# Or use CLI
$JBOSS_HOME/bin/jboss-cli.sh --connect
deploy CallCard_Server_WS/target/CallCard_Server_WS.war
```

### 7.3 Kubernetes Deployment (Optional)

**Create Kubernetes Deployment YAML:**

```yaml
# callcard-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: callcard-microservice
  labels:
    app: callcard
spec:
  replicas: 2
  selector:
    matchLabels:
      app: callcard
  template:
    metadata:
      labels:
        app: callcard
    spec:
      containers:
      - name: callcard
        image: your-registry/callcard-microservice:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-secrets
              key: url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-secrets
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secrets
              key: password
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /callcard/actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /callcard/actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: callcard-service
spec:
  type: LoadBalancer
  ports:
  - port: 8080
    targetPort: 8080
  selector:
    app: callcard
```

**Deploy to Kubernetes:**

```bash
# Create secrets for database credentials
kubectl create secret generic db-secrets \
  --from-literal=url=jdbc:sqlserver://db:1433;databaseName=gameserver_v3 \
  --from-literal=username=sa \
  --from-literal=password=YourPassword123!

# Deploy application
kubectl apply -f callcard-deployment.yaml

# Verify deployment
kubectl get deployments
kubectl get pods
kubectl logs -f deployment/callcard-microservice
```

### 7.4 Load Balancing and Scalability

**Horizontal Scaling:**

```bash
# Scale deployment to 3 instances
kubectl scale deployment callcard-microservice --replicas=3

# Or with Docker Swarm:
docker service scale callcard-service=3
```

**Load Balancer Configuration (nginx):**

```nginx
upstream callcard_backend {
    server callcard1:8080 weight=1;
    server callcard2:8080 weight=1;
    server callcard3:8080 weight=1;
}

server {
    listen 80;
    server_name api.example.com;

    location /callcard {
        proxy_pass http://callcard_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Connection pooling
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }
}
```

### 7.5 Security Hardening

**SQL Server Security:**

```sql
-- Encrypt database
ALTER DATABASE gameserver_v3 SET ENCRYPTION ON;

-- Create minimal privilege user
CREATE LOGIN callcard_app WITH PASSWORD = 'ComplexPassword123!@#';
CREATE USER callcard_app FROM LOGIN callcard_app;
GRANT SELECT, INSERT, UPDATE ON SCHEMA::dbo TO callcard_app;
```

**Application Security:**

```yaml
# application-production.yml
server:
  ssl:
    enabled: true
    key-store: /etc/callcard/keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    protocol: TLS
    enabled-protocols: TLSv1.2,TLSv1.3

management:
  endpoints:
    web:
      exposure:
        include: health,metrics    # Limit exposed endpoints

security:
  require-https: true
  max-login-attempts: 5
  lock-duration: 15m
```

**Firewall Rules:**

```bash
# Allow only necessary ports
# Port 8080: Application traffic
# Port 1433: SQL Server (internal only, not from outside)
# Port 443: HTTPS (if SSL enabled)

# Example (iptables):
sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 443 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 1433 -j DROP  # SQL Server only internal
```

### 7.6 Monitoring and Alerting

**Enable Detailed Metrics:**

```yaml
# application-production.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**Prometheus Configuration:**

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'callcard-service'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/callcard/actuator/prometheus'
```

**Grafana Dashboard:**

```json
{
  "dashboard": {
    "title": "CallCard Microservice",
    "panels": [
      {
        "title": "Request Rate",
        "targets": [{"expr": "rate(http_requests_total[5m])"}]
      },
      {
        "title": "Error Rate",
        "targets": [{"expr": "rate(http_requests_total{status=~\"5..\"}[5m])"}]
      },
      {
        "title": "JVM Memory",
        "targets": [{"expr": "jvm_memory_used_bytes"}]
      },
      {
        "title": "Database Connection Pool",
        "targets": [{"expr": "hikaricp_connections"}]
      }
    ]
  }
}
```

### 7.7 Backup and Disaster Recovery

**Automated Database Backups:**

```bash
# Backup script (backup.sh)
#!/bin/bash
BACKUP_DIR="/backup/callcard"
DB_NAME="gameserver_v3"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Full backup daily
sqlcmd -S localhost -U sa -P $DB_PASSWORD -Q \
  "BACKUP DATABASE $DB_NAME TO DISK = '$BACKUP_DIR/full_$TIMESTAMP.bak' WITH COMPRESSION;"

# Compress and archive
gzip $BACKUP_DIR/full_$TIMESTAMP.bak

# Upload to S3
aws s3 cp $BACKUP_DIR/full_$TIMESTAMP.bak.gz s3://backup-bucket/callcard/

# Retain only last 30 days
find $BACKUP_DIR -type f -mtime +30 -delete
```

**Schedule with cron:**

```bash
# Run daily at 2 AM
0 2 * * * /opt/scripts/backup.sh
```

### 7.8 Disaster Recovery Plan

**Recovery Procedures:**

1. **Database Failure:**
   - Restore from latest backup
   - Verify TALOS Core connectivity
   - Run schema verification scripts
   - Health check all endpoints

2. **Application Crash:**
   - Automatic restart via supervisor/systemd
   - Health check validates startup
   - Load balancer detects and redirects

3. **Complete Site Failure:**
   - Deploy to secondary data center
   - Update DNS to failover IP
   - Restore database from backup
   - Verify full functionality

---

## 8. Monitoring and Logging

### 8.1 Application Metrics

**Available Metrics Endpoints:**

```bash
# All metrics list
GET /callcard/actuator/metrics

# Specific metric
GET /callcard/actuator/metrics/jvm.memory.used
GET /callcard/actuator/metrics/http.server.requests
GET /callcard/actuator/metrics/db.connection.pool.usage

# Example output:
{
  "names": [
    "jvm.memory.used",
    "jvm.threads.live",
    "http.server.requests",
    "db.connection.pool.size",
    "cache.gets.miss",
    "cache.size"
  ]
}
```

**Key Metrics to Monitor:**

| Metric | Description | Threshold (Warning) |
|--------|-------------|---------------------|
| `jvm.memory.used` | JVM heap memory usage | > 75% of max |
| `http.server.requests` | HTTP request count | Normal baseline + 20% |
| `http.server.requests.max` | Request processing time | > 1000ms |
| `db.connection.pool.usage` | DB connection utilization | > 80% |
| `cache.size` | Cache entries count | > 80% of max |
| `cache.gets.miss` | Cache miss rate | > 20% |

### 8.2 Health Checks

**Health Check Endpoint:**

```bash
# Basic health
GET /callcard/actuator/health
Response: {"status":"UP"}

# Detailed health
GET /callcard/actuator/health?details=true
Response:
{
  "status": "UP",
  "components": {
    "circuitBreakers": {
      "status": "UP",
      "details": {
        "talosCore": {"status": "CLOSED"}
      }
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "Microsoft SQL Server"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {"total": 107374182400, "free": 53687091200}
    }
  }
}
```

**Health Check Components:**

1. **Database Connectivity** - Can connect to SQL Server
2. **Circuit Breaker** - TALOS Core service status
3. **Disk Space** - Sufficient disk available
4. **Memory** - JVM memory not exhausted

### 8.3 Log Levels and Configuration

**Default Log Levels:**

```yaml
# Development
logging:
  level:
    root: INFO
    com.saicon.games.callcard: DEBUG
    org.hibernate.SQL: DEBUG
    org.apache.cxf: DEBUG

# Production
logging:
  level:
    root: WARN
    com.saicon.games.callcard: INFO
    org.hibernate.SQL: ERROR
    org.apache.cxf: WARN
```

**Change Log Level at Runtime:**

```bash
# POST request to change log level
curl -X POST http://localhost:8080/callcard/actuator/loggers/com.saicon.games.callcard \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel":"DEBUG"}'

# View specific logger
curl http://localhost:8080/callcard/actuator/loggers/com.saicon.games.callcard
```

### 8.4 Logging Output

**Log Format:**

```
2025-12-22 10:30:45.123 [http-nio-8080-exec-1] INFO  com.saicon.games.callcard.service.CallCardService - Processing call card request for user: 12345
2025-12-22 10:30:45.234 [http-nio-8080-exec-1] DEBUG org.hibernate.SQL - SELECT * FROM CallCard WHERE Id = ?
2025-12-22 10:30:46.345 [http-nio-8080-exec-1] INFO  com.saicon.games.callcard.service.CallCardService - Call card processing completed in 222ms
```

**Log File Locations:**

```
Development:  logs/callcard.log (relative to working directory)
Docker:       STDOUT (use docker logs)
Kubernetes:   kubectl logs deployment/callcard-microservice
Tomcat:       CATALINA_HOME/logs/catalina.out
```

### 8.5 Structured Logging with JSON

**Add Logback JSON Encoder:**

```xml
<!-- pom.xml dependency -->
<dependency>
  <groupId>net.logstash.logback</groupId>
  <artifactId>logstash-logback-encoder</artifactId>
  <version>7.2</version>
</dependency>

<!-- logback.xml appender -->
<appender name="FILE_JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
  <file>logs/callcard.json</file>
  <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
  <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
    <fileNamePattern>logs/callcard.json.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
    <maxFileSize>100MB</maxFileSize>
    <maxHistory>30</maxHistory>
  </rollingPolicy>
</appender>
```

### 8.6 Centralized Logging with ELK Stack

**Logstash Configuration:**

```conf
# logstash.conf
input {
  file {
    path => "/var/log/callcard/callcard.json"
    codec => json
    start_position => "beginning"
  }
}

filter {
  if [message] =~ /ERROR/ {
    mutate { add_tag => ["error"] }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "callcard-%{+YYYY.MM.dd}"
  }
}
```

**Docker Compose with ELK:**

```yaml
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.14.0
    environment:
      - discovery.type=single-node
    ports:
      - "9200:9200"

  logstash:
    image: docker.elastic.co/logstash/logstash:7.14.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    depends_on:
      - elasticsearch

  kibana:
    image: docker.elastic.co/kibana/kibana:7.14.0
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
```

### 8.7 Performance Monitoring

**JVM Metrics:**

```bash
# Memory usage
curl http://localhost:8080/callcard/actuator/metrics/jvm.memory.used | jq

# Garbage collection
curl http://localhost:8080/callcard/actuator/metrics/jvm.gc.max.data.size | jq

# Thread usage
curl http://localhost:8080/callcard/actuator/metrics/jvm.threads.live | jq
```

**Database Performance:**

```bash
# Connection pool stats
curl http://localhost:8080/callcard/actuator/metrics/hikaricp.connections | jq

# Query response time
curl http://localhost:8080/callcard/actuator/metrics/http.server.requests | jq '.measurements | sort_by(.value) | reverse | .[0:5]'
```

---

## 9. Troubleshooting Common Issues

### 9.1 Database Connection Issues

**Symptom: "Cannot get a connection, pool error"**

```
ERROR: javax.servlet.ServletException: org.hibernate.exception.JDBCConnectionException:
Could not open connection
```

**Solutions:**

```bash
# 1. Verify SQL Server is running
sqlcmd -S localhost -U sa -P password -Q "SELECT @@VERSION;"

# 2. Check JDBC connection string
# Verify format: jdbc:sqlserver://host:1433;databaseName=dbname

# 3. Test connection from Java
mvn exec:java@test-db-connection

# 4. Verify credentials
echo "Connection: jdbc:sqlserver://localhost:1433;databaseName=gameserver_v3_dev"
echo "Username: sa"
echo "Password: [check environment variable DB_PASSWORD]"

# 5. Check firewall
# Allow port 1433 on firewall
netstat -an | findstr :1433  # Windows
ss -an | grep 1433            # Linux

# 6. Increase connection pool size if all connections exhausted
# In application.yml:
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
```

**Application Config Changes (application-dev.yml):**

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=gameserver_v3_dev;encrypt=false;trustServerCertificate=true
    username: sa
    password: ${DB_PASSWORD:dev_password}
    hikari:
      maximum-pool-size: 30
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 9.2 Out of Memory (OOM) Errors

**Symptom: "java.lang.OutOfMemoryError: Java heap space"**

```
Exception in thread "GC overhead limit exceeded"
java.lang.OutOfMemoryError: GC overhead limit exceeded
```

**Solutions:**

```bash
# 1. Increase JVM heap size
# When running WAR:
java -Xmx1024m -Xms512m -jar CallCard_Server_WS/target/CallCard_Server_WS.war

# 2. When using Maven:
export MAVEN_OPTS="-Xmx1024m -Xms512m"
mvn spring-boot:run

# 3. Check for memory leaks
# Enable heap dump on OOM:
java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/dumps -jar app.war

# 4. Monitor JVM memory:
curl http://localhost:8080/callcard/actuator/metrics/jvm.memory.used

# 5. Analyze heap dump
jhat /dumps/java_pid*.hprof

# Or using Eclipse MAT:
# File > Open Heap Dump > /dumps/java_pid*.hprof
```

**Long-term Solutions:**

```yaml
# Increase cache limits
spring:
  cache:
    caffeine:
      spec: maximumSize=500,expireAfterWrite=5m  # Reduce from 1000 to 500

# Optimize database connection pool
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Reduce from 30 to 20
```

### 9.3 Slow Queries or High Response Times

**Symptom: "HTTP requests taking > 5 seconds"**

**Solutions:**

```bash
# 1. Enable SQL logging and timing
# In application-dev.yml:
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql: TRACE

# 2. Check query metrics
curl http://localhost:8080/callcard/actuator/metrics/http.server.requests | jq

# 3. Identify slow queries in logs
grep "took .* ms" logs/callcard.log | sort -k3 -r | head -20

# 4. Analyze with SQL Server profiler
# SQL Server Management Studio > Tools > SQL Server Profiler

# 5. Enable query caching (Hibernate L2 cache)
spring:
  jpa:
    properties:
      hibernate:
        cache:
          use_second_level_cache: true
          region.factory_class: org.hibernate.cache.ehcache.EhCacheRegionFactory

# 6. Check database indexes exist
USE gameserver_v3;
EXEC sp_helpindex 'CallCard';
```

**CREATE INDEX if missing:**

```sql
-- Add missing indexes
CREATE NONCLUSTERED INDEX idx_callcard_org_id ON CallCard(OrganizationId);
CREATE NONCLUSTERED INDEX idx_callcard_user_id ON CallCard(UserId);
CREATE NONCLUSTERED INDEX idx_transaction_callcard_id ON CallCardTransaction(CallCardId);
```

### 9.4 Circuit Breaker Open (TALOS Core Unavailable)

**Symptom: "Circuit breaker is OPEN; call not permitted"**

```
Error: io.github.resilience4j.circuitbreaker.CallNotPermittedException:
CircuitBreaker 'talosCore' is OPEN and does not permit further calls
```

**Solutions:**

```bash
# 1. Check TALOS Core service availability
curl http://localhost:8080/Game_Server_WS/cxf/GAMEInternalService?wsdl

# 2. View circuit breaker status
curl http://localhost:8080/callcard/actuator/health | jq '.components.circuitBreakers'

# 3. Update TALOS Core URL if needed
# In application-dev.yml:
callcard:
  talos-core:
    session-validation-url: http://talos-core-staging:8080/Game_Server_WS/cxf/GAMEInternalService
    connection-timeout: 5000
    read-timeout: 10000

# 4. Adjust circuit breaker thresholds
resilience4j:
  circuitbreaker:
    instances:
      talosCore:
        failureRateThreshold: 60          # Increase from 50 to 60
        waitDurationInOpenState: 60s      # Increase from 30s to 60s
        permittedNumberOfCallsInHalfOpenState: 5  # Increase from 3

# 5. Monitor call statistics
curl http://localhost:8080/callcard/actuator/metrics/resilience4j.circuitbreaker.calls | jq
```

### 9.5 SOAP/REST Endpoint Not Found (404)

**Symptom: "404 Not Found" when calling services"**

**Solutions:**

```bash
# 1. Check correct URLs
# SOAP: http://localhost:8080/callcard/cxf/[ServiceName]?wsdl
# REST: http://localhost:8080/callcard/rest/[resource]

# 2. List all available services
curl http://localhost:8080/callcard/cxf/services

# 3. Check context path
# Log output should show:
# INFO  [org.apache.cxf.transport.servlet.CXFServlet] Load the bus from classpath:/META-INF/cxf/cxf.xml

# 4. Verify CXF configuration
# In CallCard_Server_WS/src/main/java/com/saicon/callcard/config/CxfConfiguration.java

# 5. Check @WebService annotations on service classes
find . -name "*.java" -exec grep -l "@WebService" {} \;

# 6. View Spring bean registrations
curl http://localhost:8080/callcard/actuator/beans | jq '.contexts.application.beans | keys' | grep -i service
```

### 9.6 Authentication/Authorization Failures

**Symptom: "401 Unauthorized" or "403 Forbidden"**

```
ERROR: org.springframework.security.access.AccessDeniedException:
Access is denied
```

**Solutions:**

```bash
# 1. Verify authentication interceptor is active
# Check logs for:
# INFO [com.saicon.callcard.security.SessionAuthenticationInterceptor] Validating session

# 2. Check TALOS session validation working
curl -X POST http://localhost:8080/Game_Server_WS/cxf/GAMEInternalService \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
    <soapenv:Body>
      <tns:validateSession><sessionId>test</sessionId></tns:validateSession>
    </soapenv:Body>
  </soapenv:Envelope>'

# 3. Check request headers
curl -v http://localhost:8080/callcard/rest/callcards \
  -H "X-Session-Id: [session-id]"

# 4. Look for auth configuration in:
# - src/main/java/com/saicon/callcard/security/
# - src/main/resources/application-*.yml

# 5. Temporarily disable auth to test service:
# Add to application-dev.yml:
security:
  enable-auth: false  # Development only!
```

### 9.7 Docker Container Issues

**Symptom: "Docker build fails" or "Container won't start"**

```bash
# 1. Check Docker daemon
docker info

# 2. View build logs in detail
docker build -t callcard:1.0 . --progress=plain

# 3. Check disk space
docker system df

# 4. Troubleshoot container startup
docker run -it --name callcard-debug callcard:1.0 /bin/sh

# 5. Check container logs
docker logs -f callcard-service --tail=100

# 6. Inspect running processes
docker top callcard-service

# 7. Check network connectivity
docker exec callcard-service ping sqlserver

# 8. Clean up Docker resources
docker system prune -a  # Warning: removes all unused images
```

**Common Docker Issues:**

```bash
# Issue: "Cannot connect to database from container"
# Solution: Use service name (not localhost) in connection string
# In docker-compose.yml environment:
- SPRING_DATASOURCE_URL=jdbc:sqlserver://sqlserver:1433;databaseName=gameserver_v3
# NOT:
# - SPRING_DATASOURCE_URL=jdbc:sqlserver://localhost:1433;databaseName=gameserver_v3

# Issue: "Port already in use"
# Solution: Map to different port or kill existing service
docker run -p 8081:8080 callcard:1.0  # Use different port
# Or kill existing:
docker rm -f callcard-service

# Issue: "Out of disk space"
# Solution: Clean up Docker
docker system prune
docker image prune -a  # Remove unused images
docker volume prune    # Remove unused volumes
```

### 9.8 Performance Tuning

**Monitor and Optimize:**

```bash
# 1. Check current resource usage
docker stats callcard-service

# 2. Identify slow components
curl http://localhost:8080/callcard/actuator/metrics/system.load.average.1m | jq

# 3. Optimize thread pool size
callcard:
  thread-pool:
    core-size: 10
    max-size: 20
    queue-capacity: 100

# 4. Enable caching
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=5000,expireAfterWrite=10m

# 5. Optimize database queries
# Use @Cacheable on frequently called methods:
@Cacheable(cacheNames = "callcards")
public CallCard getCallCard(Long id) { ... }

# 6. Enable query result cache
hibernate.cache.use_query_cache: true
```

### 9.9 Support and Escalation

**When to escalate:**

1. **Database corruption** - Cannot resolve with restart/queries
2. **Security breach** - Unauthorized access detected
3. **Data loss** - Missing or corrupted records
4. **Persistent performance issues** - Despite optimization attempts

**Escalation Process:**

```bash
# 1. Gather diagnostics
# Collect all logs, metrics, and configuration
tar -czf callcard-diagnostics-$(date +%Y%m%d).tar.gz \
  logs/ \
  CallCard_Server_WS/src/main/config/ \
  /var/lib/docker/containers/*/

# 2. Include health check results
curl http://localhost:8080/callcard/actuator/health > health.json
curl http://localhost:8080/callcard/actuator/metrics > metrics.json

# 3. Document issue details
# - When issue started
# - Exact error messages
# - Steps to reproduce
# - Environment details (Java version, OS, etc.)

# 4. Submit to support with diagnostics package
```

---

## Appendix: Quick Reference

### Quick Start Commands

```bash
# Full development setup
cd C:\Users\dimit\tradetool_middleware
mvn clean install -Pdev
cd CallCard_Server_WS
mvn spring-boot:run

# Full production setup
mvn clean install -Ppmi-production-v3-1 -DskipTests
java -Xmx1024m -jar CallCard_Server_WS/target/CallCard_Server_WS.war \
  --spring.profiles.active=production \
  --spring.datasource.url=jdbc:sqlserver://prod-db:1433;databaseName=gameserver_v3

# Docker quick start
docker-compose up -d
docker-compose logs -f callcard-service

# Health check
curl http://localhost:8080/callcard/actuator/health
```

### Useful URLs

| Purpose | URL |
|---------|-----|
| Health Check | http://localhost:8080/callcard/actuator/health |
| Metrics | http://localhost:8080/callcard/actuator/metrics |
| API Documentation | http://localhost:8080/callcard/swagger-ui.html |
| SOAP Services | http://localhost:8080/callcard/cxf/services |
| REST Services | http://localhost:8080/callcard/rest/* |

### Common Maven Commands

```bash
# Build only
mvn clean install -DskipTests

# Build and run tests
mvn clean install

# Build specific module
mvn clean install -pl CallCard_Server_WS -am

# Run locally
mvn spring-boot:run

# View dependencies
mvn dependency:tree

# Check for security vulnerabilities
mvn org.owasp:dependency-check-maven:check
```

### Environment Variables Quick Reference

```bash
# Required
DB_USERNAME=sa
DB_PASSWORD=YourPassword123!

# Optional (with defaults)
DB_HOST=localhost
TALOS_CORE_URL=http://localhost:8080
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

---

## Document Version

- **Version**: 1.0.0
- **Last Updated**: 2025-12-22
- **Status**: Ready for Production

---

## Support and Feedback

For questions, issues, or updates to this documentation:

- **Repository**: C:\Users\dimit\tradetool_middleware
- **Branch**: 001-callcard-microservice
- **Contact**: Development Team

---

**End of Deployment Documentation**
