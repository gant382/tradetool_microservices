# CallCard Microservice

A modern Java/Spring Boot microservice for CallCard management, extracted from the TALOS Core platform. This service provides comprehensive call card functionality with SOAP and REST APIs, built on Spring Boot 2.7.x, Apache CXF 3.5.x, and Hibernate 5.6.x.

**Version:** 1.0.0-SNAPSHOT
**Java Target:** 1.8+
**Spring Boot:** 2.7.18
**License:** Proprietary

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Module Structure](#module-structure)
4. [Quick Start](#quick-start)
5. [Build Instructions](#build-instructions)
6. [Configuration](#configuration)
7. [Running the Service](#running-the-service)
8. [API Documentation](#api-documentation)
9. [Testing](#testing)
10. [Deployment](#deployment)
11. [Troubleshooting](#troubleshooting)
12. [Contributing](#contributing)
13. [Links to Documentation](#links-to-documentation)

---

## Project Overview

### What is CallCard Microservice?

CallCard Microservice is an independent microservice that manages call card operations including:

- **Call Card Management**: Create, update, and retrieve call card records
- **Transaction Processing**: Track call card transactions and financial movements
- **User References**: Link call cards to user accounts and customer relationships
- **Templates**: Define reusable call card templates and configurations
- **Statistics**: Aggregate and report on call card usage and performance
- **Multi-Tenant Support**: Isolation and filtering by organization/tenant

### Key Characteristics

- **Microservice Architecture**: Independently deployable and scalable
- **Dual Protocol Support**: SOAP (CXF) and REST (Jersey) endpoints
- **Database Agnostic**: Configured for Microsoft SQL Server 2008+ with Hibernate
- **Session-Based Auth**: Validates sessions via TALOS Core
- **Circuit Breaker Pattern**: Resilience4j for downstream service calls
- **Caching**: Caffeine cache for frequent lookups
- **Monitoring**: Spring Boot Actuator endpoints for health and metrics
- **OpenAPI Documentation**: Swagger/OpenAPI integration for API docs

---

## Architecture

### High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                     CallCard Microservice                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │              Spring Boot Application Container               │  │
│  │                                                              │  │
│  │  ┌────────────────────────────────────────────────────────┐ │  │
│  │  │         Web Layer (CXF SOAP + Jersey REST)            │ │  │
│  │  │  - SOAP Endpoints: /cxf/*                             │ │  │
│  │  │  - REST Endpoints: /rest/*                            │ │  │
│  │  │  - Swagger UI: /swagger-ui.html                       │ │  │
│  │  │  - Actuator: /actuator/*                              │ │  │
│  │  └────────────────────────────────────────────────────────┘ │  │
│  │                           ▲                                  │  │
│  │                           │ (HTTP/JSON/SOAP)                │  │
│  │  ┌────────────────────────────────────────────────────────┐ │  │
│  │  │     Interceptors & Security                           │ │  │
│  │  │  - SessionAuthenticationInterceptor                   │ │  │
│  │  │  - Request/Response logging (CXF)                     │ │  │
│  │  │  - CORS handling                                      │ │  │
│  │  └────────────────────────────────────────────────────────┘ │  │
│  │                           ▲                                  │  │
│  │  ┌────────────────────────────────────────────────────────┐ │  │
│  │  │     Service Layer (Business Logic)                    │ │  │
│  │  │  - CallCardService                                    │ │  │
│  │  │  - StatisticsService                                  │ │  │
│  │  │  - TransactionService                                 │ │  │
│  │  │  - Resilience4j Circuit Breaker                       │ │  │
│  │  └────────────────────────────────────────────────────────┘ │  │
│  │                           ▲                                  │  │
│  │  ┌────────────────────────────────────────────────────────┐ │  │
│  │  │     Component Layer (DAOs/Repositories)               │ │  │
│  │  │  - CallCardDAO                                        │ │  │
│  │  │  - Spring Data JPA Repositories                       │ │  │
│  │  │  - Query construction & caching                       │ │  │
│  │  └────────────────────────────────────────────────────────┘ │  │
│  │                           ▲                                  │  │
│  │  ┌────────────────────────────────────────────────────────┐ │  │
│  │  │     Data Access Layer (JPA/Hibernate)                 │ │  │
│  │  │  - Entity mapping (10+ domain entities)               │ │  │
│  │  │  - Caffeine 2nd-level caching                         │ │  │
│  │  │  - Connection pooling (HikariCP)                      │ │  │
│  │  └────────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                           ▲                                        │
│                           │ (SQL)                                  │
└───────────────────────────┼────────────────────────────────────────┘
                            │
              ┌─────────────┴──────────────┐
              │                            │
         ┌────▼────┐              ┌───────▼──────┐
         │SQL Server 2008+        │TALOS Core    │
         │  gameserver_v3_dev     │(Session Auth)│
         │  (Database)            └──────────────┘
         └─────────────────────────────────────────────
```

### Data Flow

```
Client Request (SOAP/REST)
           │
           ▼
    CXF Servlet / Jersey Container
           │
           ▼
    SessionAuthenticationInterceptor
    (Validate session via TALOS Core)
           │
           ▼
    Service Layer (Resilience4j Circuit Breaker)
           │
           ▼
    Component Layer (DAO/Repository)
           │
           ▼
    Hibernate ORM + Caffeine Cache
           │
           ▼
    SQL Server Database
           │
           ▼
    Response (JSON/SOAP XML) to Client
```

### 4-Layer Architecture Pattern

This microservice follows the proven 4-layer pattern:

```
┌─────────────────────────────────────────┐
│  callcard-ws-api (API Layer)            │
│  - Service interfaces (@WebService)     │
│  - DTO models                           │
│  - Exception definitions                │
│  - Annotations (SOAP, REST, Swagger)    │
└─────────────────────────────────────────┘
              ▲
              │ implements
              │
┌─────────────────────────────────────────┐
│  callcard-service (Service Layer)       │
│  - Business logic orchestration         │
│  - Transaction management               │
│  - Resilience4j patterns                │
│  - External service integration         │
└─────────────────────────────────────────┘
              ▲
              │ uses
              │
┌─────────────────────────────────────────┐
│  callcard-components (Component Layer)  │
│  - DAO classes                          │
│  - Spring Data JPA Repositories         │
│  - Query construction                   │
│  - Data access logic                    │
└─────────────────────────────────────────┘
              ▲
              │ uses
              │
┌─────────────────────────────────────────┐
│  callcard-entity (Entity Layer)         │
│  - JPA @Entity classes                  │
│  - Hibernate mappings                   │
│  - Validation annotations               │
│  - No business logic                    │
└─────────────────────────────────────────┘
              ▲
              │ JDBC
              │
         ┌────────────┐
         │SQL Server  │
         └────────────┘
```

---

## Module Structure

### Module Dependency Graph

```
callcard-entity (JAR)
    └─ Core JPA entities and validation
       • No business logic
       • Pure data mapping

callcard-ws-api (JAR)
    ├─ Depends: callcard-entity
    └─ Service interfaces and DTOs
       • SOAP interfaces (@WebService)
       • REST endpoint definitions
       • Exception types
       • Utility classes

callcard-components (JAR)
    ├─ Depends: callcard-entity, callcard-ws-api
    └─ Data access layer
       • DAO classes
       • JPA repositories
       • Query construction

callcard-service (JAR)
    ├─ Depends: callcard-components, callcard-ws-api
    └─ Service orchestration
       • Business logic
       • Transaction management
       • Resilience patterns
       • CXF client for TALOS Core

CallCard_Server_WS (WAR)
    ├─ Depends: All modules above
    ├─ Spring Boot application
    ├─ Deployable artifact
    └─ Configurations
       • CXF SOAP setup
       • Jersey REST setup
       • DataSource configuration
       • Security interceptors
```

### File Structure

```
tradetool_middleware/
├── README.md                              (This file)
├── pom.xml                                (Parent POM - Spring Boot 2.7.18)
│
├── callcard-entity/
│   ├── pom.xml
│   └── src/main/java/com/saicon/games/callcard/entity/
│       ├── CallCard.java
│       ├── CallCardRefUser.java
│       ├── CallCardTransaction.java
│       ├── CallCardTemplate.java
│       ├── CallCardTemplateEntry.java
│       ├── CallCardTemplatePOS.java
│       ├── CallCardTemplateUser.java
│       ├── CallCardTemplateUserReferences.java
│       └── CallCardTransactionType.java
│
├── callcard-ws-api/
│   ├── pom.xml
│   ├── src/main/java/com/saicon/games/callcard/
│   │   ├── exception/
│   │   │   ├── BusinessLayerException.java
│   │   │   └── ExceptionTypeTO.java
│   │   ├── resources/
│   │   │   ├── CallCardResources.java        (SOAP service interface)
│   │   │   ├── CallCardStatisticsResources.java
│   │   │   ├── CallCardTransactionResources.java
│   │   │   └── SimplifiedCallCardResources.java
│   │   ├── ws/
│   │   │   ├── response/
│   │   │   │   ├── CallCardDetailResponse.java
│   │   │   │   ├── CallCardListResponse.java
│   │   │   │   └── TransactionResponse.java
│   │   ├── util/
│   │   │   ├── Assert.java
│   │   │   ├── Constants.java
│   │   │   ├── DTOParam.java
│   │   │   ├── EventTO.java
│   │   │   └── PageInfo.java
│   │   └── dto/
│   │       ├── CallCardDTO.java
│   │       ├── TransactionDTO.java
│   │       └── StatisticsDTO.java
│
├── callcard-components/
│   ├── pom.xml
│   └── src/main/java/com/saicon/games/callcard/components/
│       ├── CallCardDAO.java
│       ├── CallCardRepository.java (Spring Data)
│       ├── CallCardTransactionDAO.java
│       └── ... (other DAOs)
│
├── callcard-service/
│   ├── pom.xml
│   └── src/main/java/com/saicon/games/callcard/service/
│       ├── CallCardService.java
│       ├── CallCardStatisticsService.java
│       ├── CallCardTransactionService.java
│       ├── TalosCoreClient.java        (Session validation)
│       └── impl/
│           ├── CallCardServiceImpl.java
│           ├── CallCardStatisticsServiceImpl.java
│           └── ... (implementations)
│
└── CallCard_Server_WS/
    ├── pom.xml
    ├── src/main/java/com/saicon/callcard/
    │   ├── CallCardMicroserviceApplication.java
    │   ├── config/
    │   │   ├── ComponentConfiguration.java
    │   │   ├── CxfConfiguration.java
    │   │   ├── DataSourceConfiguration.java
    │   │   ├── JerseyConfiguration.java
    │   │   └── CachingConfiguration.java
    │   ├── security/
    │   │   └── SessionAuthenticationInterceptor.java
    │   └── rest/
    │       ├── CallCardRestController.java
    │       ├── CallCardTransactionController.java
    │       └── StatisticsController.java
    ├── src/main/resources/
    │   └── application.yml              (Base configuration)
    └── src/main/config/
        ├── dev/
        │   └── application-dev.yml      (Development)
        ├── pmi-staging-v3-1/
        │   └── application-staging.yml  (Staging)
        └── pmi-production-v3-1/
            └── application-production.yml (Production)
```

---

## Quick Start

### Prerequisites

- **Java Development Kit (JDK)** 1.8 or higher
- **Apache Maven** 3.6.x or higher
- **Git** for version control
- **Microsoft SQL Server** 2008 or later (with `gameserver_v3_dev` database)
- **IDE** (Optional): IntelliJ IDEA, Eclipse, or VS Code

### Clone and Setup

```bash
# Clone the repository
git clone https://git.company.com/tradetool_middleware.git
cd tradetool_middleware

# Verify Maven installation
mvn --version

# Verify Java installation
java -version
```

### Build All Modules

```bash
# Build with default dev profile
mvn clean install

# Build specific module
mvn -pl callcard-entity clean install
mvn -pl callcard-ws-api clean install
mvn -pl callcard-components clean install
mvn -pl callcard-service clean install
mvn -pl CallCard_Server_WS clean package
```

### Run the Service

```bash
# From CallCard_Server_WS directory
cd CallCard_Server_WS
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Service will start on http://localhost:8080/callcard
```

### Access Service Endpoints

Once running, access these endpoints:

- **SOAP Service WSDL**: http://localhost:8080/callcard/cxf/CallCardService?wsdl
- **REST API Base**: http://localhost:8080/callcard/rest/
- **Swagger UI**: http://localhost:8080/callcard/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/callcard/api-docs
- **Health Check**: http://localhost:8080/callcard/actuator/health
- **Metrics**: http://localhost:8080/callcard/actuator/metrics

---

## Build Instructions

### Build Environment Setup

#### Windows

```powershell
# Install Maven (if not already installed)
choco install maven

# Verify installation
mvn -v
```

#### Linux/macOS

```bash
# Install Maven (if not already installed)
brew install maven    # macOS
sudo apt install maven # Debian/Ubuntu

# Verify installation
mvn -v
```

### Maven Profiles

The project supports multiple environment profiles:

#### Development (Default)

```bash
mvn clean install -Pdev

# Or implicitly (dev is default)
mvn clean install
```

**Configuration loaded:** `src/main/config/dev/application-dev.yml`

#### Staging

```bash
mvn clean install -Ppmi-staging-v3-1
```

**Configuration loaded:** `src/main/config/pmi-staging-v3-1/application-staging.yml`

#### Production

```bash
mvn clean install -Ppmi-production-v3-1
```

**Configuration loaded:** `src/main/config/pmi-production-v3-1/application-production.yml`

### Build Lifecycle

#### 1. Compile Phase

```bash
# Compile all source code
mvn compile

# Compile and run tests
mvn test
```

#### 2. Package Phase

```bash
# Create JAR/WAR artifacts
mvn package
```

**Output artifacts:**
- `callcard-entity/target/callcard-entity-1.0.0-SNAPSHOT.jar`
- `callcard-ws-api/target/callcard-ws-api-1.0.0-SNAPSHOT.jar`
- `callcard-components/target/callcard-components-1.0.0-SNAPSHOT.jar`
- `callcard-service/target/callcard-service-1.0.0-SNAPSHOT.jar`
- `CallCard_Server_WS/target/CallCard_Server_WS-1.0.0-SNAPSHOT.war`
- `CallCard_Server_WS/target/CallCard_Server_WS-1.0.0-SNAPSHOT-exec.jar` (Spring Boot executable)

#### 3. Install Phase

```bash
# Install artifacts to local Maven repository
mvn install
```

#### 4. Deploy Phase

```bash
# Deploy to remote repository
mvn deploy
```

### Advanced Build Options

#### Build Specific Module Only

```bash
# Build single module with dependencies
mvn -pl callcard-service clean install

# Build without dependencies
mvn -pl callcard-service -am clean install
```

#### Skip Tests

```bash
mvn clean package -DskipTests
```

#### Enable Verbose Output

```bash
mvn clean install -X
```

#### Clean Build Artifacts

```bash
# Clean only current module
mvn clean

# Clean all modules
mvn clean -r
```

---

## Configuration

### Database Configuration

#### SQL Server Connection

Configure in `src/main/config/{environment}/application-{environment}.yml`:

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://hostname:1433;databaseName=gameserver_v3_dev;encrypt=false;trustServerCertificate=true
    username: sa
    password: your_password
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
```

**Connection Pool Settings:**
- **maximum-pool-size**: 20 connections max
- **minimum-idle**: 5 minimum idle connections
- **idle-timeout**: 5 minutes
- **connection-timeout**: 20 seconds
- **max-lifetime**: 20 minutes

#### Hibernate Configuration

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # validate, update, create, create-drop
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServer2012Dialect
        format_sql: true
        use_sql_comments: true
        jdbc.batch_size: 50
        order_inserts: true
        order_updates: true
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region.factory_class: org.hibernate.cache.ehcache.EhCacheRegionFactory
```

### Server Configuration

```yaml
server:
  port: 8080                    # Server port
  servlet:
    context-path: /callcard     # Context path
  compression:
    enabled: true              # Enable gzip compression
    mime-types: application/json,application/xml,text/xml
    min-response-size: 1024
```

### CXF SOAP Configuration

```yaml
cxf:
  path: /cxf                              # SOAP servlet path
  servlet:
    init:
      service-list-path: /services        # Service listing endpoint
```

**Service URL:** `http://localhost:8080/callcard/cxf/CallCardService?wsdl`

### Jersey REST Configuration

REST endpoints are automatically registered under `/rest/` path.

**REST API Examples:**
- `http://localhost:8080/callcard/rest/callcards`
- `http://localhost:8080/callcard/rest/transactions`
- `http://localhost:8080/callcard/rest/statistics`

### Caching Configuration

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m
```

**Cache Settings:**
- **maximumSize**: 1000 entries max
- **expireAfterWrite**: 5 minutes expiration

### TALOS Core Integration

```yaml
callcard:
  talos-core:
    session-validation-url: http://localhost:8080/Game_Server_WS/cxf/GAMEInternalService
    connection-timeout: 5000
    read-timeout: 10000

  session-cache:
    enabled: true
    ttl-seconds: 300
    max-size: 10000

  circuit-breaker:
    failure-rate-threshold: 50
    wait-duration-in-open-state: 30000
    permitted-calls-in-half-open-state: 3
```

### Resilience4j Circuit Breaker

```yaml
resilience4j:
  circuitbreaker:
    instances:
      talosCore:
        registerHealthIndicator: true
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 3
```

### Logging Configuration

```yaml
logging:
  level:
    root: INFO
    com.saicon.games.callcard: DEBUG
    org.hibernate.SQL: DEBUG
    org.apache.cxf: INFO
    org.glassfish.jersey: INFO
```

### Environment Variables

```bash
# Database password (overrides config)
export DB_PASSWORD=your_secure_password

# Active profile
export SPRING_PROFILES_ACTIVE=dev

# Server port
export SERVER_PORT=8080

# Custom database URL
export SPRING_DATASOURCE_URL=jdbc:sqlserver://prod-server:1433;databaseName=gameserver_v3
```

---

## Running the Service

### Development Startup

#### Using Spring Boot Maven Plugin

```bash
cd CallCard_Server_WS

# Run with dev profile (default)
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Run with environment variables
mvn spring-boot:run \
  -Dspring-boot.run.arguments="--spring.profiles.active=dev --server.port=8080"
```

#### Using Java Directly

```bash
# Build first
mvn clean package

# Run executable JAR
java -jar CallCard_Server_WS/target/CallCard_Server_WS-1.0.0-SNAPSHOT-exec.jar \
  --spring.profiles.active=dev \
  --server.port=8080
```

#### Using IDE

**IntelliJ IDEA:**
1. Open project
2. Right-click `CallCardMicroserviceApplication.java`
3. Select "Run CallCardMicroserviceApplication"

**VS Code:**
1. Install "Spring Boot Extension Pack"
2. Open command palette: Ctrl+Shift+P
3. Select "Spring Boot: Start"

### Verify Service is Running

```bash
# Check health endpoint
curl http://localhost:8080/callcard/actuator/health

# Check info endpoint
curl http://localhost:8080/callcard/actuator/info

# Verify SOAP service
curl http://localhost:8080/callcard/cxf/CallCardService?wsdl

# Verify metrics
curl http://localhost:8080/callcard/actuator/metrics
```

### Production Deployment

#### Deploy to Tomcat

```bash
# Build WAR file
mvn clean package -Ppmi-production-v3-1

# Copy to Tomcat
cp CallCard_Server_WS/target/CallCard_Server_WS-1.0.0-SNAPSHOT.war \
   $CATALINA_HOME/webapps/callcard.war

# Tomcat will auto-deploy
# Access at: http://localhost:8080/callcard
```

#### Deploy as System Service (Linux)

```bash
# Create service file
sudo tee /etc/systemd/system/callcard.service > /dev/null <<EOF
[Unit]
Description=CallCard Microservice
After=network.target

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/callcard
ExecStart=/usr/bin/java -jar CallCard_Server_WS-1.0.0-SNAPSHOT-exec.jar \
          --spring.profiles.active=production
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable callcard
sudo systemctl start callcard

# Check status
sudo systemctl status callcard
```

#### Deploy with Docker

```bash
# Build Spring Boot executable JAR
mvn clean package -Ppmi-production-v3-1

# Create Dockerfile (example)
cat > Dockerfile <<EOF
FROM openjdk:8-jre-slim
COPY CallCard_Server_WS/target/CallCard_Server_WS-1.0.0-SNAPSHOT-exec.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=production"]
EOF

# Build Docker image
docker build -t callcard:1.0.0 .

# Run container
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:sqlserver://db-server:1433;... \
  -e SPRING_DATASOURCE_USERNAME=sa \
  -e SPRING_DATASOURCE_PASSWORD=password \
  callcard:1.0.0
```

---

## API Documentation

### SOAP Service Endpoints

#### Service Definition

Service Interface: `com.saicon.games.callcard.resources.CallCardResources`

**WSDL URL:** `http://localhost:8080/callcard/cxf/CallCardService?wsdl`

#### Available SOAP Operations

##### Call Card Management

```xml
<!-- Get Call Card by ID -->
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:res="http://resources.callcard.games.saicon.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <res:getCallCard>
         <callCardID>123</callCardID>
      </res:getCallCard>
   </soapenv:Body>
</soapenv:Envelope>

<!-- Create Call Card -->
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:res="http://resources.callcard.games.saicon.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <res:createCallCard>
         <callCardDTO>
            <cardNumber>CC-12345</cardNumber>
            <userID>100</userID>
            <!-- Additional fields -->
         </callCardDTO>
      </res:createCallCard>
   </soapenv:Body>
</soapenv:Envelope>
```

##### Statistics

```xml
<!-- Get Call Card Statistics -->
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:res="http://resources.callcard.games.saicon.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <res:getStatistics>
         <userID>100</userID>
         <startDate>2023-01-01</startDate>
         <endDate>2023-12-31</endDate>
      </res:getStatistics>
   </soapenv:Body>
</soapenv:Envelope>
```

##### Transactions

```xml
<!-- Get Call Card Transactions -->
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:res="http://resources.callcard.games.saicon.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <res:getTransactions>
         <callCardID>123</callCardID>
         <pageNumber>1</pageNumber>
         <pageSize>50</pageSize>
      </res:getTransactions>
   </soapenv:Body>
</soapenv:Envelope>
```

### REST API Endpoints

#### Base URL
`http://localhost:8080/callcard/rest/`

#### Call Card Endpoints

```http
GET    /callcards                 # List all call cards
GET    /callcards/{id}            # Get call card by ID
POST   /callcards                 # Create new call card
PUT    /callcards/{id}            # Update call card
DELETE /callcards/{id}            # Delete call card
```

#### Example REST Requests

```bash
# Get call card by ID
curl -X GET http://localhost:8080/callcard/rest/callcards/123 \
  -H "Content-Type: application/json"

# Create call card
curl -X POST http://localhost:8080/callcard/rest/callcards \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "CC-12345",
    "userID": 100,
    "balance": 1000.00
  }'

# Update call card
curl -X PUT http://localhost:8080/callcard/rest/callcards/123 \
  -H "Content-Type: application/json" \
  -d '{
    "balance": 900.00,
    "status": "ACTIVE"
  }'
```

#### Transaction Endpoints

```http
GET    /transactions              # List transactions
GET    /transactions/{id}         # Get transaction by ID
POST   /transactions              # Create transaction
GET    /callcards/{id}/transactions  # Get transactions for call card
```

#### Statistics Endpoints

```http
GET    /statistics/user/{userID}  # Get statistics for user
GET    /statistics/period         # Get statistics for period
POST   /statistics/report         # Generate statistics report
```

#### Authentication Header

All requests require a valid session token from TALOS Core:

```bash
curl -X GET http://localhost:8080/callcard/rest/callcards \
  -H "Authorization: Bearer <session_token>"
```

### OpenAPI/Swagger Documentation

**Interactive API Docs:** http://localhost:8080/callcard/swagger-ui.html

**OpenAPI JSON:** http://localhost:8080/callcard/api-docs

**OpenAPI YAML:** http://localhost:8080/callcard/api-docs.yaml

---

## Testing

### Test Structure

```
CallCard_Server_WS/src/test/
├── java/com/saicon/callcard/
│   ├── CallCardMicroserviceApplicationTest.java
│   ├── service/
│   │   ├── CallCardServiceTest.java
│   │   ├── CallCardStatisticsServiceTest.java
│   │   └── TalosCoreClientTest.java
│   ├── components/
│   │   ├── CallCardDAOTest.java
│   │   └── RepositoryTests.java
│   └── config/
│       └── DataSourceConfigurationTest.java
└── resources/
    ├── application-test.yml
    └── test-data.sql
```

### Running Tests

#### Run All Tests

```bash
mvn test
```

#### Run Specific Test Class

```bash
mvn test -Dtest=CallCardServiceTest
```

#### Run Specific Test Method

```bash
mvn test -Dtest=CallCardServiceTest#testGetCallCard
```

#### Run Tests with Coverage

```bash
mvn test jacoco:report

# View report
open target/site/jacoco/index.html
```

#### Run Tests Without Building

```bash
mvn test -DskipBuild=true
```

### Test Configuration

Test-specific configuration in `src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
```

### Test Categories

#### Unit Tests
- Service layer logic
- DAO operations
- Utility classes
- Business rule validation

#### Integration Tests
- Spring context loading
- Database integration
- CXF service endpoints
- Jersey REST endpoints

#### End-to-End Tests
- SOAP service invocations
- REST API workflows
- Authentication flow
- Error handling

### Example Unit Test

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class CallCardServiceTest {

    @Autowired
    private CallCardService callCardService;

    @Autowired
    private CallCardRepository repository;

    @Test
    public void testGetCallCard() {
        // Arrange
        CallCard card = new CallCard();
        card.setCardNumber("CC-12345");
        repository.save(card);

        // Act
        CallCard retrieved = callCardService.getCallCard(card.getId());

        // Assert
        assertNotNull(retrieved);
        assertEquals("CC-12345", retrieved.getCardNumber());
    }
}
```

### Debugging Tests

```bash
# Run tests with debug output
mvn test -X

# Run single test with debug
mvn test -Dtest=CallCardServiceTest -Dmaven.surefire.debug

# Enable Surefire debugging
mvn -Dmaven.surefire.debug test
```

---

## Deployment

### Deployment Checklist

- [ ] Code reviewed and merged to main branch
- [ ] All tests passing (mvn test)
- [ ] Build successful with production profile
- [ ] Configuration reviewed and set for target environment
- [ ] Database migrations (if any) applied
- [ ] Backup of previous version created
- [ ] Load balancer updated (if applicable)
- [ ] Monitoring and alerting configured
- [ ] Smoke tests executed in target environment
- [ ] Team notified of deployment

### Staging Deployment

```bash
# Build with staging profile
mvn clean package -Ppmi-staging-v3-1

# Upload to staging environment
scp CallCard_Server_WS/target/CallCard_Server_WS-1.0.0-SNAPSHOT.war \
    deploy@staging:/opt/deployments/

# Deploy
ssh deploy@staging
sudo systemctl stop callcard-staging
sudo rm /opt/tomcat-staging/webapps/callcard.war
sudo cp /opt/deployments/CallCard_Server_WS-1.0.0-SNAPSHOT.war \
        /opt/tomcat-staging/webapps/callcard.war
sudo systemctl start callcard-staging
```

### Production Deployment

```bash
# Build with production profile
mvn clean package -Ppmi-production-v3-1

# Create release tag
git tag -a v1.0.0 -m "Release CallCard Microservice v1.0.0"
git push origin v1.0.0

# Upload to production
scp CallCard_Server_WS/target/CallCard_Server_WS-1.0.0-SNAPSHOT.war \
    deploy@production:/opt/deployments/

# Deploy (requires approval/manual steps)
# ... (Follow your organization's production deployment procedures)
```

### Post-Deployment Verification

```bash
# Check service health
curl https://api.company.com/callcard/actuator/health

# Verify SOAP service
curl https://api.company.com/callcard/cxf/CallCardService?wsdl

# Check logs
tail -f /var/log/callcard/application.log

# Run smoke tests
./run-smoke-tests.sh production
```

### Rollback Procedure

```bash
# Stop current service
sudo systemctl stop callcard

# Restore previous version
sudo cp /opt/backups/CallCard_Server_WS-0.9.0.war \
        /opt/tomcat/webapps/callcard.war

# Start service
sudo systemctl start callcard

# Verify
curl http://localhost:8080/callcard/actuator/health
```

---

## Troubleshooting

### Common Issues

#### 1. Database Connection Fails

**Error:** `Connection timeout: Connection refused`

**Solution:**
```bash
# Verify SQL Server is running
sqlcmd -S localhost,1433 -U sa -P password -Q "SELECT 1"

# Check connection string in application-dev.yml
# Ensure database exists
sqlcmd -S localhost,1433 -U sa -P password -Q "USE gameserver_v3_dev; SELECT name FROM sys.databases"

# Verify network connectivity
telnet localhost 1433
```

#### 2. TALOS Core Session Validation Fails

**Error:** `Circuit breaker is open for talosCore`

**Solution:**
```bash
# Verify TALOS Core is running
curl http://localhost:8080/Game_Server_WS/cxf/GAMEInternalService?wsdl

# Check circuit breaker status
curl http://localhost:8080/callcard/actuator/health/circuitBreakers

# Reset circuit breaker
curl -X POST http://localhost:8080/callcard/actuator/circuitbreakersreset/talosCore

# Check configuration
grep -A5 "talos-core:" CallCard_Server_WS/src/main/config/dev/application-dev.yml
```

#### 3. Memory Issues

**Error:** `java.lang.OutOfMemoryError: Java heap space`

**Solution:**
```bash
# Increase heap size
export JAVA_OPTS="-Xmx2048m -Xms512m"

# Run with increased memory
java -Xmx2048m -Xms512m -jar CallCard_Server_WS-1.0.0-SNAPSHOT-exec.jar

# For Maven
mvn spring-boot:run -DargLine="-Xmx2048m"
```

#### 4. Port Already in Use

**Error:** `Address already in use: bind`

**Solution:**
```bash
# Windows: Find process using port 8080
netstat -ano | findstr :8080
taskkill /F /PID <process_id>

# Linux: Find and kill process
lsof -i :8080
kill -9 <process_id>

# Or change port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

#### 5. Compile Errors

**Error:** `[ERROR] cannot find symbol`

**Solution:**
```bash
# Clean and rebuild
mvn clean install -U

# Check Maven dependencies
mvn dependency:resolve

# Update dependencies
mvn -U clean install
```

#### 6. CORS Issues (REST API)

**Error:** `Access to XMLHttpRequest blocked by CORS policy`

**Solution:**
Check `JerseyConfiguration.java` for CORS settings:
```java
@Bean
public FilterRegistrationBean<CorsFilter> corsFilter() {
    // Verify CORS is properly configured
}
```

### Enable Debug Logging

```bash
# Development environment
export LOGGING_LEVEL_COM_SAICON_GAMES_CALLCARD=DEBUG
export LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG

# Or in application-dev.yml
logging:
  level:
    com.saicon.games.callcard: DEBUG
    org.hibernate.SQL: DEBUG
```

### Check Logs

```bash
# View application logs
tail -f logs/callcard.log

# View Catalina logs (if using Tomcat)
tail -f $CATALINA_HOME/logs/catalina.out

# View system logs
journalctl -u callcard -f
```

### Performance Troubleshooting

```bash
# Check JVM metrics
curl http://localhost:8080/callcard/actuator/metrics/jvm.memory.used

# Check database pool status
curl http://localhost:8080/callcard/actuator/metrics/hikaricp.connections

# Check cache hit rate
curl http://localhost:8080/callcard/actuator/metrics/cache.gets
```

---

## Contributing

### Development Workflow

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/JIRA-123-description
   ```

2. **Make Changes**
   - Follow code style guidelines
   - Write unit tests for new code
   - Update documentation

3. **Build and Test**
   ```bash
   mvn clean test
   mvn checkstyle:check  # Code style
   mvn spotbugs:check    # Bug detection
   ```

4. **Commit**
   ```bash
   git add .
   git commit -m "JIRA-123: Add feature description"
   ```

5. **Push and Create Pull Request**
   ```bash
   git push origin feature/JIRA-123-description
   ```

### Code Style Guide

- **Java Version**: 1.8+
- **Naming**: CamelCase for classes, camelCase for methods
- **Indentation**: 4 spaces
- **Line Length**: 120 characters max
- **Comments**: Javadoc for public APIs

Example:
```java
/**
 * Retrieves a call card by its ID.
 *
 * @param callCardId the unique identifier of the call card
 * @return CallCard object if found
 * @throws BusinessLayerException if call card not found
 */
public CallCard getCallCard(Long callCardId) {
    // Implementation
}
```

### Testing Requirements

- Minimum 80% code coverage for new code
- All tests must pass: `mvn clean test`
- Integration tests for new services
- Unit tests for business logic

### Documentation Requirements

- Update README.md for configuration changes
- Add Javadoc for new public methods
- Document new API endpoints
- Update CHANGELOG.md

### Pull Request Checklist

- [ ] Code follows style guidelines
- [ ] All tests passing
- [ ] Javadoc added for public APIs
- [ ] README updated (if needed)
- [ ] No breaking changes to existing APIs
- [ ] Performance implications considered
- [ ] Security implications considered

### Merge Strategy

- Squash commits for feature branches
- Preserve commit history for release branches
- Require 2 code reviews minimum
- All CI checks must pass

---

## Links to Documentation

### Project Documentation

- **[CLAUDE.md](./CLAUDE.md)** - Project instructions and guidelines
- **[Architecture Guide](./docs/ARCHITECTURE.md)** - Detailed architecture documentation
- **[API Reference](./docs/API_REFERENCE.md)** - Complete API documentation
- **[Configuration Guide](./docs/CONFIGURATION.md)** - Configuration options reference

### Related Projects

- **[TALOS Core (gameserver_v3)](../gameserver_v3)** - Main platform server
- **[TradeTools Middleware](../talosmaind_middleware)** - Parent middleware project

### Technical Stack

- **[Spring Boot 2.7 Documentation](https://spring.io/projects/spring-boot)**
- **[Apache CXF 3.5 Documentation](https://cxf.apache.org/)**
- **[Jersey REST Documentation](https://jersey.glassfish.org/)**
- **[Hibernate 5.6 Documentation](https://hibernate.org/orm/)**
- **[Resilience4j Documentation](https://resilience4j.readme.io/)**

### Tools & Resources

- **[Maven Documentation](https://maven.apache.org/documentation.html)**
- **[Springdoc OpenAPI](https://springdoc.org/)**
- **[Caffeine Cache](https://github.com/ben-manes/caffeine)**
- **[HikariCP Documentation](https://github.com/brettwooldridge/HikariCP)**

### Internal Documentation

- **Development Setup**: See [Quick Start](#quick-start)
- **Build Instructions**: See [Build Instructions](#build-instructions)
- **Configuration**: See [Configuration](#configuration)
- **Deployment**: See [Deployment](#deployment)

---

## Support & Contact

### Getting Help

1. **Documentation**: Check this README and linked documentation
2. **Logs**: Enable debug logging to diagnose issues
3. **Team**: Contact development team via internal channels
4. **Issues**: Report bugs in Jira under `CALLCARD` project

### Escalation Process

1. **Level 1**: Check documentation and logs
2. **Level 2**: Post in team Slack channel
3. **Level 3**: Create Jira ticket with logs
4. **Level 4**: Contact DevOps for infrastructure issues

### Key Contacts

- **Tech Lead**: [Name] - [Email]
- **DevOps**: [Name] - [Email]
- **Database Admin**: [Name] - [Email]

---

## Version History

### v1.0.0 (Current)
- Initial extraction from gameserver_v3
- Spring Boot 2.7.x migration
- SOAP and REST API support
- Multi-tenant support
- Session-based authentication
- Resilience4j circuit breaker
- Caffeine caching

### Roadmap

- [ ] GraphQL API support
- [ ] Event streaming integration (Kafka)
- [ ] Advanced analytics dashboard
- [ ] Performance optimizations
- [ ] API versioning strategy

---

## License

Proprietary - SAICON Games

All code and documentation in this repository are proprietary and confidential.

---

## Changelog

See [CHANGELOG.md](./CHANGELOG.md) for detailed version history and changes.

---

**Last Updated**: 2025-12-22
**Document Version**: 1.0
**Maintained By**: Development Team

For the latest version of this documentation, see the repository's main branch.
