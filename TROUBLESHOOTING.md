# CallCard Microservice - Comprehensive Troubleshooting Guide

**Last Updated**: 2025-12-22
**Version**: 1.0.0-SNAPSHOT
**Target Platform**: Spring Boot 2.7.18 + Apache CXF 3.5.9 + Hibernate 5.6.15

---

## Table of Contents

1. [Compilation Errors](#compilation-errors)
2. [Runtime Errors](#runtime-errors)
3. [Database Connection Issues](#database-connection-issues)
4. [SOAP/REST Endpoint Problems](#soaprest-endpoint-problems)
5. [Spring Boot Startup Failures](#spring-boot-startup-failures)
6. [ClassNotFoundException Issues](#classnotfoundexception-issues)
7. [Memory Issues](#memory-issues)
8. [Performance Degradation](#performance-degradation)
9. [Logging & Debugging](#logging--debugging)
10. [Known Issues & Workarounds](#known-issues--workarounds)

---

## Compilation Errors

### Error 1: Package/Import Mismatch

**Symptom**:
```
[ERROR] incompatible types: java.util.List<java.lang.Object[]> cannot be converted to java.util.List<com.saicon.games.callcard.ws.dto.EventTO>
```

**Root Cause**:
Multiple EventTO classes exist with different packages:
- `com.saicon.games.callcard.util.EventTO` - Core properties (21 fields)
- `com.saicon.games.callcard.ws.dto.EventTO` - API/DTO (6 fields)

**Solution**:
```bash
# Verify correct import in CallCardManagement.java
# Should use: com.saicon.games.callcard.ws.dto.EventTO
# NOT: com.saicon.games.callcard.util.EventTO
```

**Step-by-Step Fix**:
1. Open `callcard-components/src/main/java/com/saicon/games/callcard/components/impl/CallCardManagement.java`
2. Find line 45: `import com.saicon.games.callcard.util.EventTO;`
3. Replace with: `import com.saicon.games.callcard.ws.dto.EventTO;`
4. Rebuild: `mvn clean compile -pl callcard-components -am`

### Error 2: Missing Stub DTOs

**Symptom**:
```
[ERROR] cannot find symbol: class DecimalDTO
[ERROR] cannot find symbol: class KeyValueDTO
[ERROR] cannot find symbol: class ResourceDTO
```

**Root Cause**:
External DTO classes from other modules not included in classpath.

**Solution**:
Create stub implementations in `callcard-ws-api`:

```bash
# 1. Create DecimalDTO
cat > callcard-ws-api/src/main/java/com/saicon/games/client/data/DecimalDTO.java << 'EOF'
package com.saicon.games.client.data;

import java.io.Serializable;
import java.math.BigDecimal;

public class DecimalDTO implements Serializable {
    private BigDecimal value;

    public DecimalDTO() {}
    public DecimalDTO(BigDecimal value) { this.value = value; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
}
EOF

# 2. Create KeyValueDTO
cat > callcard-ws-api/src/main/java/com/saicon/multiplayer/dto/KeyValueDTO.java << 'EOF'
package com.saicon.multiplayer.dto;

import java.io.Serializable;

public class KeyValueDTO implements Serializable {
    private String key;
    private String value;

    public KeyValueDTO() {}
    public KeyValueDTO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
EOF

# 3. Rebuild
mvn clean install -pl callcard-ws-api
```

### Error 3: Type Conversion Errors

**Symptom**:
```
[ERROR] incompatible types: java.lang.Object[] cannot be converted to java.lang.Number
[ERROR] incompatible types: long cannot be converted to int
```

**Root Cause**:
Generic return types from queries need explicit casting.

**Solution**:

**For Object[] conversions** (line 3923 in CallCardManagement):
```java
// WRONG:
if (results != null && !results.isEmpty()) {
    return ((Number) results.get(0)).longValue();
}

// CORRECT:
if (results != null && !results.isEmpty()) {
    Object[] row = results.get(0);
    return ((Number) row[0]).longValue();
}
```

**For long to int conversions** (line 2672):
```java
// WRONG:
public int countCallCards(...) {
    return erpDynamicQueryManager.countCallCards(...);
}

// CORRECT:
public int countCallCards(...) {
    return (int) erpDynamicQueryManager.countCallCards(...);
}
```

**For List type ambiguity**:
```java
// WRONG:
List<CallCardTemplate> templates = erpDynamicQueryManager.listCallCardTemplates(
    userGroupId, gameTypeId, null, null, true, true, null, 0, -1
);

// CORRECT (explicit cast):
List<CallCardTemplate> templates = erpDynamicQueryManager.listCallCardTemplates(
    userGroupId, gameTypeId, null,
    (List<com.saicon.multiplayer.dto.KeyValueDTO>)null,
    true, true, null, 0, -1
);
```

### Error 4: Missing Constants

**Symptom**:
```
[ERROR] cannot find symbol: variable APP_SETTING_KEY_THEME
[ERROR] cannot find symbol: variable USER_SESSION_ID_NOT_VALID
```

**Root Cause**:
Constants defined elsewhere or need to be added to utility classes.

**Solution**:

**Add to Constants.java**:
```java
// com.saicon.games.callcard.util.Constants.java
public class Constants {
    public static final String APP_SETTING_KEY_THEME = "THEME";
    public static final String APP_SETTING_KEY_LANGUAGE = "LANGUAGE";
    public static final String ITEM_TYPE_CALLCARD = "CALLCARD";
    public static final String ITEM_TYPE_TEMPLATE = "TEMPLATE";
}
```

**Add to ExceptionTypeTO.java**:
```java
// callcard-ws-api/src/main/java/com/saicon/games/callcard/ws/exception/ExceptionTypeTO.java
public class ExceptionTypeTO {
    public static final String USER_SESSION_ID_NOT_VALID = "1008";
    public static final String GENERIC = "9999";

    public static String valueOf(int errorNumber) {
        switch (errorNumber) {
            case 1008: return USER_SESSION_ID_NOT_VALID;
            case 9999: return GENERIC;
            default: return GENERIC;
        }
    }
}
```

### Error 5: Module Build Order

**Symptom**:
```
[ERROR] Failed to execute goal on project callcard-components:
[ERROR] Could not resolve dependencies for project
```

**Root Cause**:
Building modules in wrong dependency order.

**Solution**:
```bash
# Build in CORRECT order:

# 1. Build parent POM
mvn clean install -pl . -N

# 2. Build entity layer (no internal dependencies)
mvn clean install -pl callcard-entity -am

# 3. Build WS API layer (depends on entity)
mvn clean install -pl callcard-ws-api -am

# 4. Build components layer (depends on entity + ws-api)
mvn clean install -pl callcard-components -am

# 5. Build service layer (depends on components + ws-api)
mvn clean install -pl callcard-service -am

# 6. Build WAR application (depends on all)
mvn clean install -pl CallCard_Server_WS -am

# Or build all at once:
mvn clean install
```

---

## Runtime Errors

### Error 1: Initialization Failed

**Symptom**:
```
org.springframework.beans.factory.BeanCreationException:
Error creating bean with name 'callCardManagement' defined in class path resource
```

**Root Cause**:
Missing Spring bean configuration or unresolved component dependencies.

**Solution**:

**Check Configuration** (`CallCard_Server_WS/src/main/resources/application.yml`):
```yaml
spring:
  application:
    name: callcard-microservice
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=GameServerDb
    username: sa
    password: <your-password>
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServer2012Dialect
        show_sql: false
        format_sql: true
```

**Add Spring Boot Main Class** if missing:
```java
// CallCard_Server_WS/src/main/java/com/saicon/games/callcard/CallCardMicroserviceApplication.java
package com.saicon.games.callcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CallCardMicroserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CallCardMicroserviceApplication.class, args);
    }
}
```

**Enable Component Scanning**:
```java
@SpringBootApplication(scanBasePackages = {
    "com.saicon.games.callcard",
    "com.saicon.games.core"
})
```

### Error 2: Null Pointer Exception in Business Logic

**Symptom**:
```
java.lang.NullPointerException: Cannot invoke method on null object
at com.saicon.games.callcard.components.impl.CallCardManagement.getCallCard(CallCardManagement.java:XXX)
```

**Root Cause**:
External component dependencies return null (stub implementations).

**Solution**:

**Defensive Null Checks**:
```java
// Before calling external components
if (addressbookManagement != null) {
    result = addressbookManagement.getAddress(userId);
} else {
    logger.warn("AddressBook component not available");
    result = new Address(); // fallback
}
```

**Mock External Dependencies in Dev/Test**:
```yaml
# application-dev.yml
callcard:
  external-components:
    enabled: false
    fallback-enabled: true
```

### Error 3: Transaction Rollback

**Symptom**:
```
org.springframework.transaction.TransactionSystemException:
Could not commit JPA transaction; nested exception is javax.persistence.RollbackException
```

**Root Cause**:
Constraint violations, deadlocks, or constraint definition issues.

**Solution**:

**Check Database Constraints**:
```sql
-- SQL Server diagnostic
SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_NAME = 'CallCard'

-- Check for deadlocks
sp_who2
-- Kill blocking process: KILL <SPID>
```

**Enable SQL Logging**:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

**Review Entity Annotations**:
```java
@Entity
@Table(name = "CallCard")
public class CallCard {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID callCardId;

    @Column(nullable = false, unique = true)
    private String callCardCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "callCardTemplateId", nullable = false)
    private CallCardTemplate callCardTemplate;
}
```

---

## Database Connection Issues

### Error 1: Connection Refused

**Symptom**:
```
com.microsoft.sqlserver.jdbc.SQLServerException:
The TCP/IP connection to the host [hostname], port [1433] has failed.
Connection refused: connect
```

**Root Cause**:
SQL Server not running, wrong host/port, or firewall blocking.

**Solution**:

**Step 1: Verify SQL Server is running**:
```powershell
# Windows Services
Get-Service "MSSQLSERVER" | Select-Object Status

# Or use SQL Server Configuration Manager
"C:\Program Files (x86)\Microsoft SQL Server\160\Tools\Binn\SqlServerManager16.msc"
```

**Step 2: Check connectivity**:
```bash
# Test connection with sqlcmd (installed with SSMS)
sqlcmd -S localhost -U sa -P <password>

# From Maven build:
mvn clean test -Dspring.datasource.url="jdbc:sqlserver://localhost:1433;databaseName=GameServerDb"
```

**Step 3: Verify connection string**:
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:sqlserver://SERVER_NAME:1433;databaseName=GameServerDb;encrypt=true;trustServerCertificate=true
    username: sa
    password: ${DB_PASSWORD}  # Use environment variable in production
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
```

### Error 2: Authentication Failed

**Symptom**:
```
com.microsoft.sqlserver.jdbc.SQLServerException:
Login failed for user 'sa'.
The user is not associated with a trusted SQL Server connection.
```

**Root Cause**:
Wrong username/password or SQL Server authentication mode disabled.

**Solution**:

**Check SQL Server Authentication Mode**:
```sql
-- In SQL Server Management Studio
EXEC xp_instance_regread N'HKEY_LOCAL_MACHINE',
  N'Software\Microsoft\MSSQLServer\MSSQLServer',
  N'LoginMode'

-- 1 = Windows auth only (NOT SUPPORTED)
-- 2 = Mixed mode (Windows + SQL auth)
-- Change to Mixed if needed: Restart SQL Server service
```

**Verify User Exists and Has Permissions**:
```sql
-- SQL Server
SELECT * FROM sys.sql_logins WHERE name = 'sa'

-- Grant permissions
GRANT ALTER ANY LOGIN TO sa
GRANT ALTER ANY USER TO sa
ALTER LOGIN sa ENABLE
ALTER LOGIN sa WITH PASSWORD = 'new_password'
```

### Error 3: Network/Timeout Issues

**Symptom**:
```
java.sql.SQLException: com.microsoft.sqlserver.jdbc.SQLServerException:
The login has timed out; the connection has been closed
```

**Root Cause**:
Network latency, server overload, or query timeout.

**Solution**:

**Increase Timeout Values**:
```yaml
spring:
  datasource:
    hikari:
      connection-timeout: 60000  # 60 seconds
      idle-timeout: 600000       # 10 minutes
      max-lifetime: 1800000      # 30 minutes

  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
          fetch_size: 50
```

**Add Connection String Parameters**:
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://SERVER:1433;databaseName=DB;
         loginTimeout=10;
         connectionTimeout=10000;
         socketTimeout=30000;
         encrypt=true;
         trustServerCertificate=true
```

**Connection Pooling Configuration**:
```java
// CallCard_Server_WS/src/main/java/com/saicon/games/callcard/config/DataSourceConfig.java
package com.saicon.games.callcard.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean
    public DataSource dataSource(HikariConfig hikariConfig) {
        return new HikariDataSource(hikariConfig);
    }
}
```

### Error 4: Entity Mapping Errors

**Symptom**:
```
org.hibernate.HibernateException:
Mapped class not found for @ManyToOne mapping on CallCard.callCardTemplate
```

**Root Cause**:
Missing entity in persistence.xml or wrong package scan.

**Solution**:

**Verify Persistence Configuration** (`callcard-entity/src/main/resources/META-INF/persistence.xml`):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.0" xmlns="http://java.sun.com/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd">

    <persistence-unit name="callcardPU" transaction-type="RESOURCE_LOCAL">
        <class>com.saicon.games.callcard.entity.CallCard</class>
        <class>com.saicon.games.callcard.entity.CallCardTemplate</class>
        <class>com.saicon.games.callcard.entity.CallCardTemplateEntry</class>
        <!-- ... list all entities ... -->

        <properties>
            <property name="hibernate.dialect" value="org.hibernate.dialect.SQLServer2012Dialect"/>
            <property name="hibernate.connection.driver_class" value="com.microsoft.sqlserver.jdbc.SQLServerDriver"/>
            <property name="hibernate.show_sql" value="false"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

**Or use Spring Boot's Entity Scanning**:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServer2012Dialect

# Add to main application class:
@SpringBootApplication(scanBasePackages = {
    "com.saicon.games.callcard.entity",
    "com.saicon.games.callcard.components"
})
```

---

## SOAP/REST Endpoint Problems

### Error 1: WSDL Not Found

**Symptom**:
```
HTTP 404 Not Found
http://localhost:8080/callcard-microservice/cxf/CallCardService?wsdl
```

**Root Cause**:
CXF servlet not configured or service endpoint not registered.

**Solution**:

**Verify CXF Configuration** (`application.yml`):
```yaml
cxf:
  path: /cxf
  servlet:
    init-parameters:
      properties-location: classpath:cxf.properties
```

**Create CXF Properties** (`resources/cxf.properties`):
```properties
org.apache.cxf.logging.enabled=true
```

**Add CXF Endpoint Configuration**:
```java
// CallCard_Server_WS/src/main/java/com/saicon/games/callcard/config/CxfConfig.java
package com.saicon.games.callcard.config;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.saicon.games.callcard.ws.ICallCardService;
import com.saicon.games.callcard.ws.impl.CallCardService;

@Configuration
public class CxfConfig {

    @Autowired
    private Bus cxfBus;

    @Bean
    public ICallCardService callCardServiceImpl() {
        return new CallCardService();
    }

    @Bean
    public EndpointImpl callCardServiceEndpoint(ICallCardService callCardService) {
        EndpointImpl endpoint = new EndpointImpl(cxfBus, callCardService);
        endpoint.publish("/CallCardService");
        return endpoint;
    }
}
```

**Check WAR Application Name** in `pom.xml`:
```xml
<build>
    <finalName>callcard-microservice</finalName>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

### Error 2: SOAP Fault Response

**Symptom**:
```xml
<soap:Fault>
    <faultcode>Server</faultcode>
    <faultstring>Internal Server Error</faultstring>
    <detail>
        <ns2:CallCardException>
            <message>User not found</message>
        </ns2:CallCardException>
    </detail>
</soap:Fault>
```

**Root Cause**:
Service throws exception during processing.

**Solution**:

**Enable SOAP Logging**:
```yaml
logging:
  level:
    org.apache.cxf: DEBUG
    org.apache.cxf.interceptor: DEBUG
    org.springframework.web: DEBUG
```

**Create Custom SOAP Fault Handler**:
```java
// CallCard_Server_WS/src/main/java/com/saicon/games/callcard/ws/handler/SoapFaultHandler.java
package com.saicon.games.callcard.ws.handler;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.FaultOutInterceptor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CustomSoapFaultHandler extends FaultOutInterceptor {
    @Override
    public void handleMessage(Message message) throws Fault {
        Throwable cause = message.getContent(Exception.class);
        if (cause instanceof BusinessLayerException) {
            BusinessLayerException ble = (BusinessLayerException) cause;
            // Convert to SOAP fault
            SOAPFault fault = createFault(ble);
            message.setContent(SOAPMessage.class, fault.getParentElement());
        }
    }
}
```

### Error 3: REST API Endpoint Not Found

**Symptom**:
```
HTTP 404 Not Found
http://localhost:8080/callcard-microservice/api/callcards
```

**Root Cause**:
REST controller not registered or wrong path configuration.

**Solution**:

**Create REST Controller**:
```java
// CallCard_Server_WS/src/main/java/com/saicon/games/callcard/rest/CallCardRestController.java
package com.saicon.games.callcard.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.saicon.games.callcard.components.ICallCardManagement;
import com.saicon.games.callcard.ws.dto.CallCardDTO;
import java.util.List;

@RestController
@RequestMapping("/api/callcards")
public class CallCardRestController {

    @Autowired
    private ICallCardManagement callCardManagement;

    @GetMapping
    public ResponseEntity<List<CallCardDTO>> listCallCards(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Implementation
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/{callCardId}")
    public ResponseEntity<CallCardDTO> getCallCard(@PathVariable String callCardId) {
        // Implementation
        return ResponseEntity.ok(new CallCardDTO());
    }
}
```

**Configure REST in application.yml**:
```yaml
server:
  servlet:
    context-path: /callcard-microservice
  port: 8080

spring:
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher  # For Swagger compatibility
```

### Error 4: Content-Type Mismatch

**Symptom**:
```
org.apache.cxf.interceptor.Fault:
Could not unmarshal the response as the expected type
```

**Root Cause**:
Wrong content type or serialization mismatch.

**Solution**:

**Check Request Headers**:
```bash
# Should be:
Content-Type: application/soap+xml;charset=UTF-8
# or for REST:
Content-Type: application/json;charset=UTF-8
```

**Configure Content Negotiation**:
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML);
    }
}
```

---

## Spring Boot Startup Failures

### Error 1: Application Context Initialization Failed

**Symptom**:
```
org.springframework.context.ApplicationContextException:
Unable to start ServletWebServerApplicationContext due to missing ServletWebServerFactory bean
```

**Root Cause**:
Missing `spring-boot-starter-web` dependency or incorrect configuration.

**Solution**:

**Verify Dependency** in `CallCard_Server_WS/pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**Check for Conflicting Excludes**:
```bash
mvn dependency:tree | grep -A5 "starter-web"

# If issues found, rebuild with clean:
mvn clean install -U
```

### Error 2: Embedded Server Initialization Failed

**Symptom**:
```
org.springframework.boot.web.server.WebServerException:
Unable to start embedded Tomcat server
java.net.BindException: Address already in use: bind
```

**Root Cause**:
Port 8080 already in use by another process.

**Solution**:

**Find Process Using Port** (Windows):
```powershell
netstat -ano | findstr :8080

# Kill process if needed:
taskkill /F /PID <PROCESS_ID>
```

**Change Port in application.yml**:
```yaml
server:
  port: 8081  # or any available port
```

**Or pass via command line**:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Error 3: Auto-Configuration Failed

**Symptom**:
```
The following matches were found based on the configured matching strategy
(case-insensitive non-words), however they were not accepted:
- DataSourceAutoConfiguration (OnClassCondition)
  did not match because @ConditionalOnClass did not find required class 'org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType'
```

**Root Cause**:
Missing JDBC driver or database autoconfiguration dependency.

**Solution**:

**Add SQL Server JDBC Driver** to `pom.xml`:
```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.4.2.jre8</version>
</dependency>
```

**Explicitly Configure DataSource**:
```java
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return new HikariDataSource(config);
    }
}
```

### Error 4: Configuration Properties Not Found

**Symptom**:
```
Could not resolve placeholder 'DB_PASSWORD' in value "${DB_PASSWORD}"
```

**Root Cause**:
Missing environment variables or properties file.

**Solution**:

**Set Environment Variables** (Windows):
```powershell
$env:DB_PASSWORD = "your_password"
$env:DB_HOST = "localhost"
$env:DB_USERNAME = "sa"

# Persist (requires restart):
[Environment]::SetEnvironmentVariable("DB_PASSWORD", "your_password", "User")
```

**Or use application.yml**:
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=GameServerDb
    username: sa
    password: your_password_here  # NOT recommended for production
```

**Or use application-dev.yml** (Git-ignored):
```yaml
# application-dev.yml (add to .gitignore)
spring:
  datasource:
    password: local_dev_password
```

**Activate Profile**:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

---

## ClassNotFoundException Issues

### Error 1: Missing Internal Dependency Class

**Symptom**:
```
java.lang.ClassNotFoundException: com.saicon.games.callcard.components.ICallCardManagement
at java.net.URLClassLoader.findClass(URLClassLoader.java:XXX)
```

**Root Cause**:
Module not built or not in classpath.

**Solution**:

**Rebuild Dependent Module**:
```bash
# Rebuild components module
mvn clean install -pl callcard-components -am

# Then rebuild WAR
mvn clean install -pl CallCard_Server_WS -am

# Verify JAR is in WAR:
jar tf CallCard_Server_WS/target/CallCard_Server_WS.war | grep ICallCardManagement
```

**Check Module Dependency Order**:
```bash
# Display dependency tree:
mvn dependency:tree -pl CallCard_Server_WS

# Expected order:
# CallCard_Server_WS
#  ├─ callcard-entity
#  ├─ callcard-ws-api
#  ├─ callcard-components
#  │   ├─ callcard-entity
#  │   └─ callcard-ws-api
#  └─ callcard-service
#      ├─ callcard-components
#      ├─ callcard-entity
#      └─ callcard-ws-api
```

### Error 2: Third-Party Dependency Missing

**Symptom**:
```
java.lang.ClassNotFoundException: org.apache.cxf.Bus
org.springframework.beans.factory.BeanDefinitionStoreException:
Failed to parse configuration class [CallCardMicroserviceApplication]
```

**Root Cause**:
Maven dependency not downloaded or corrupted local repository.

**Solution**:

**Clean and Rebuild**:
```bash
# Remove local Maven cache
rm -rf ~/.m2/repository/org/apache/cxf

# Rebuild with -U (update snapshots):
mvn clean install -U

# Or force download:
mvn dependency:purge-local-repository clean install
```

**Check Effective POM**:
```bash
mvn help:effective-pom -pl CallCard_Server_WS > effective-pom.xml
# Review to ensure all CXF dependencies are present
```

### Error 3: Conflicting Versions

**Symptom**:
```
java.lang.ClassNotFoundException: org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl
java.lang.NoClassDefFoundError: org/hibernate/version/Version
```

**Root Cause**:
Multiple Hibernate versions in classpath.

**Solution**:

**View Dependency Tree**:
```bash
mvn dependency:tree | grep -i hibernate

# Output might show duplicates:
# [INFO] org.hibernate:hibernate-core:5.6.15.Final
# [INFO] org.hibernate:hibernate-core:5.4.0.Final
```

**Exclude Conflicting Version**:
```xml
<!-- In CallCard_Server_WS/pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- Explicitly include correct version -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>5.6.15.Final</version>
</dependency>
```

---

## Memory Issues

### Error 1: OutOfMemoryError: Java Heap Space

**Symptom**:
```
java.lang.OutOfMemoryError: Java heap space
at java.util.Arrays.copyOf(Arrays.java:3210)
```

**Root Cause**:
Processing large result sets or memory leaks.

**Solution**:

**Increase Heap Size**:
```bash
# Maven execution
export MAVEN_OPTS="-Xmx1024m -Xms512m"
mvn clean install

# Spring Boot application
java -Xmx1024m -Xms512m -jar CallCard_Server_WS.war

# Or in application.yml:
# Note: application.yml cannot set heap, use JVM args or properties file
```

**Optimize Queries**:
```java
// BAD: Loads all records into memory
List<CallCard> allCards = callCardDao.findAll();

// GOOD: Use pagination
Page<CallCard> page = callCardDao.findAll(
    PageRequest.of(0, 100));

// GOOD: Use streaming for large exports
Stream<CallCard> cardStream = callCardDao.findAllAsStream();
```

**Configure Query Limits**:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
          fetch_size: 50
        default_batch_fetch_size: 20
```

### Error 2: OutOfMemoryError: PermGen Space

**Symptom**:
```
java.lang.OutOfMemoryError: PermGen space
```

**Root Cause**:
Too many class definitions (older Java versions < 1.8).

**Solution**:
```bash
# Java 7:
export MAVEN_OPTS="-XX:PermSize=256m -XX:MaxPermSize=512m"

# Java 8+ (uses Metaspace instead):
export MAVEN_OPTS="-XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m"
```

### Error 3: Memory Leak Detection

**Symptom**:
```
Heap memory keeps increasing over time
GC pause times getting longer
```

**Root Cause**:
Unclosed resources or circular references.

**Solution**:

**Enable GC Logging**:
```bash
# Detailed GC output
java -Xmx1024m -XX:+PrintGCDetails -XX:+PrintGCDateStamps \
     -Xloggc:gc.log -jar CallCard_Server_WS.war

# Or use:
java -Xmx1024m -Xlog:gc*:file=gc.log -jar CallCard_Server_WS.war
```

**Ensure Proper Resource Cleanup**:
```java
// Good: Try-with-resources
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, value);
    ResultSet rs = ps.executeQuery();
    // Use rs
}

// Good: AutoCloseable beans
@Component
public class ResourcePool implements AutoCloseable {
    @Override
    public void close() {
        // Cleanup
    }
}
```

**Analyze Heap Dump**:
```bash
# Generate heap dump
jmap -dump:live,format=b,file=heap.bin <PID>

# Analyze with jhat
jhat heap.bin

# Or use Eclipse Memory Analyzer
# Tools -> Analyze -> Import heap dump
```

---

## Performance Degradation

### Issue 1: Slow Query Execution

**Symptom**:
```
Queries taking >5 seconds
Database CPU at 100%
Spring application responding slowly
```

**Root Cause**:
Missing indexes, N+1 query problem, or inefficient joins.

**Solution**:

**Enable Query Logging**:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.springframework.data: DEBUG

# application-debug.yml
spring:
  jpa:
    properties:
      hibernate:
        show_sql: true
        use_sql_comments: true
        format_sql: true
        statistics: true  # Enable Hibernate statistics
```

**Analyze Query Performance** (SQL Server):
```sql
-- Enable execution plan
SET STATISTICS IO ON
SET STATISTICS TIME ON

-- Your query here
SELECT * FROM CallCard WHERE callCardCode = '123'

-- Review output for:
-- - CPU time
-- - Elapsed time
-- - Physical reads (should be low)
-- - Logical reads (should be optimized with indexes)
```

**Fix N+1 Problem**:
```java
// WRONG: N+1 query problem
List<CallCard> cards = cardDao.findAll();  // 1 query
for (CallCard card : cards) {
    CallCardTemplate template = card.getCallCardTemplate();  // N queries!
}

// CORRECT: Use eager loading
@Query("SELECT c FROM CallCard c LEFT JOIN FETCH c.callCardTemplate")
List<CallCard> findAllWithTemplate();

// OR use @EntityGraph
@EntityGraph(attributePaths = {"callCardTemplate"})
List<CallCard> findAll();

// OR specify in repository
List<CallCard> findAll(Pageable pageable);  // Spring Data auto-optimizes
```

**Add Database Indexes**:
```sql
-- SQL Server
CREATE INDEX IDX_CallCard_UserId ON CallCard(userId)
CREATE INDEX IDX_CallCard_TemplateId ON CallCard(callCardTemplateId)
CREATE INDEX IDX_CallCard_Code ON CallCard(callCardCode) WHERE active = 1

-- Verify index usage
EXEC sp_helpindex 'CallCard'
```

### Issue 2: High Memory Usage

**Symptom**:
```
Memory usage reaches 90%+
Frequent GC pauses (>100ms)
Application unresponsive
```

**Root Cause**:
Large result sets, cache misconfiguration, or connection pool issues.

**Solution**:

**Limit Query Results**:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        max_fetch_depth: 3
```

**Configure Caching**:
```yaml
spring:
  cache:
    type: caffeine

caffeine:
  cache:
    max-size: 1000
    ttl-minutes: 10

# Or in code:
@Cacheable(value = "callcards", key = "#callCardId")
public CallCard getCallCard(String callCardId) {
    return callCardDao.findById(callCardId).orElse(null);
}
```

**Connection Pool Limits**:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Not too high!
      minimum-idle: 5
      max-lifetime: 1800000  # 30 minutes
      leak-detection-threshold: 60000  # 1 minute
```

### Issue 3: Slow Startup Time

**Symptom**:
```
Application takes >30 seconds to start
"Started CallCardMicroserviceApplication in 45.123 seconds"
```

**Root Cause**:
Scanning too many packages, initializing unnecessary beans, or slow database queries.

**Solution**:

**Optimize Component Scanning**:
```java
@SpringBootApplication(scanBasePackages = {
    "com.saicon.games.callcard",  // Only scan necessary packages
    "com.saicon.games.core"
})
public class CallCardMicroserviceApplication {
}
```

**Lazy Initialize Beans**:
```java
@Configuration
public class LazyBeanConfig {

    @Bean
    @Lazy  // Only initialize when needed
    public ExpensiveComponent expensiveComponent() {
        return new ExpensiveComponent();
    }
}
```

**Defer DataSource Initialization**:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Don't create/update schema at startup
  datasource:
    initialization-mode: never  # Don't run SQL scripts
```

**Profile Startup Time**:
```bash
# Enable startup profiling
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"

# Or in code:
ApplicationContext context = SpringApplication.run(...);
ApplicationStartup.recordStartupTime(context);
```

---

## Logging & Debugging

### Configuration

**Logback Configuration** (`resources/logback-spring.xml`):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_FILE" value="logs/callcard-microservice.log"/>
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/callcard-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
    </appender>

    <logger name="com.saicon.games.callcard" level="DEBUG"/>
    <logger name="org.hibernate.SQL" level="DEBUG"/>
    <logger name="org.apache.cxf" level="INFO"/>
    <logger name="org.springframework" level="INFO"/>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### Debugging Techniques

**Enable Debug Output**:
```yaml
# application-debug.yml
logging:
  level:
    root: DEBUG
    com.saicon.games.callcard: TRACE
    org.springframework: DEBUG
    org.hibernate: DEBUG
    org.apache.cxf: DEBUG
```

**Add Detailed Logging**:
```java
@Service
public class CallCardService {
    private static final Logger logger = LoggerFactory.getLogger(CallCardService.class);

    public CallCard getCallCard(String callCardId) {
        logger.debug("Fetching CallCard: {}", callCardId);
        try {
            CallCard card = callCardDao.findById(callCardId).orElse(null);
            if (card == null) {
                logger.warn("CallCard not found: {}", callCardId);
                return null;
            }
            logger.info("Successfully retrieved CallCard: {} - Code: {}",
                callCardId, card.getCallCardCode());
            return card;
        } catch (Exception e) {
            logger.error("Error retrieving CallCard: " + callCardId, e);
            throw new BusinessLayerException("Failed to retrieve callcard", e);
        }
    }
}
```

**Remote Debugging**:
```bash
# Start application with debug agent
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

# In IDE (IntelliJ): Run -> Edit Configurations -> Remote
# Host: localhost, Port: 5005
```

---

## Known Issues & Workarounds

### Issue 1: Stub Component Implementations

**Problem**: External components (AddressbookManagement, SalesOrderManagement, etc.) return null or default values.

**Impact**:
- Address lookups fail
- Sales order integration incomplete
- Metadata operations limited

**Workaround**:
```java
// Add defensive checks in calling code
if (addressbookComponent != null) {
    address = addressbookComponent.getAddress(userId);
} else {
    logger.warn("AddressBook component unavailable, using default");
    address = new Address();  // Default empty address
}

// Or feature-flag the feature:
@ConditionalOnProperty(name = "callcard.enable-addressbook", havingValue = "true")
public class AddressIntegrationService {
    // Only loads if feature enabled
}
```

**Resolution**:
- Contact platform team for actual implementations
- Add dependency once available
- Remove stub classes and use real implementations

### Issue 2: Multiple EventTO Classes

**Problem**: Two EventTO classes exist with different fields:
- `com.saicon.games.callcard.util.EventTO` (core)
- `com.saicon.games.callcard.ws.dto.EventTO` (API)

**Impact**:
- Type conversion errors
- Serialization issues
- API contract violations

**Workaround**:
- Always use `ws.dto.EventTO` for API communication
- Use `util.EventTO` only for internal event processing
- Add comments to clarify distinction

**Resolution**:
- Consolidate into single EventTO class
- Create converter utility if both needed
- Document API contract

### Issue 3: Spring Boot vs Traditional Spring Configuration

**Problem**: Project mixes Spring Boot 2.7.x with legacy Spring 3.0 patterns.

**Impact**:
- Some legacy SOAP configurations may not work
- Classpath scanning conflicts
- Context initialization issues

**Workaround**:
```yaml
# Disable Spring Boot defaults that conflict with legacy config:
spring:
  xml:
    ignore: true
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

**Resolution**:
- Migrate legacy Spring configurations to Spring Boot
- Use Spring Boot auto-configuration where possible
- Document any Spring 3.0 legacy patterns

### Issue 4: SQL Server Dialect Issues

**Problem**: SQL Server 2012+ specific features may not be available in older versions.

**Impact**:
- GROUP_CONCAT not available
- OFFSET FETCH syntax issues
- String functions differences

**Workaround**:
```java
// Use dialect-specific queries:
@Query(value = "SELECT STRING_AGG(id, ',') FROM CallCard", nativeQuery = true)
String aggregateIds();

// Or use pagination instead of LIMIT:
Page<CallCard> findAll(Pageable pageable);
```

**Resolution**:
- Verify SQL Server version: 2012 or later recommended
- Test queries with target SQL Server version
- Use JPA/Hibernate abstractions when possible

### Issue 5: Docker Deployment (Optional)

**Problem**: Application may fail in containerized environment.

**Impact**:
- Port mapping issues
- Network configuration
- Database connectivity from container

**Workaround**:
```dockerfile
# Dockerfile
FROM openjdk:8-jre-slim
RUN apt-get update && apt-get install -y curl
COPY target/CallCard_Server_WS.war /app/
EXPOSE 8080
CMD ["java", "-Xmx512m", "-jar", "/app/CallCard_Server_WS.war"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  callcard:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:sqlserver://sqlserver:1433;databaseName=GameServerDb
      - SPRING_DATASOURCE_USERNAME=sa
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    depends_on:
      - sqlserver

  sqlserver:
    image: mcr.microsoft.com/mssql/server:2019-latest
    environment:
      - ACCEPT_EULA=Y
      - SA_PASSWORD=${DB_PASSWORD}
    ports:
      - "1433:1433"
```

---

## Troubleshooting Checklist

Before reporting an issue, verify:

- [ ] Correct Java version installed (`java -version` shows 1.8+)
- [ ] Maven configured (`mvn -v` works)
- [ ] SQL Server running and accessible
- [ ] All modules built in correct order
- [ ] No compilation errors (`mvn clean compile`)
- [ ] Application properties configured correctly
- [ ] Database schema created with tables
- [ ] Necessary tables exist: `CallCard`, `CallCardTemplate`, `CallCardTemplateEntry`
- [ ] No conflicting processes on port 8080
- [ ] Firewall allows database connections
- [ ] Proper privileges on database (SELECT, INSERT, UPDATE, DELETE)
- [ ] Logs reviewed for error messages
- [ ] Search existing issues for similar problems

---

## Quick Reference Commands

```bash
# Build commands
mvn clean install                                    # Full build
mvn clean compile -pl callcard-components -am       # Single module
mvn dependency:tree                                 # Show dependencies
mvn help:effective-pom                              # Show computed POM

# Debugging
mvn clean install -X                                # Verbose output
mvn clean install -e                                # Show stack traces
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"  # Debug mode

# Testing
mvn test                                            # Run all tests
mvn test -Dtest=CallCardManagementTest              # Single test class
mvn test -Dtest=CallCardManagementTest#testMethod   # Single test method

# Deployment
mvn clean package -Ppmi-production-v3-1             # Production build
mvn clean package -Ppmi-staging-v3-1                # Staging build
mvn spring-boot:run                                 # Local development

# Database
sqlcmd -S localhost -U sa -P <password>             # SQL Server CLI
# Check running processes on port:
netstat -ano | findstr :1433                        # Windows
lsof -i :1433                                       # Linux/Mac

# Docker
docker-compose up                                   # Start services
docker-compose logs -f callcard                     # View logs
docker-compose down                                 # Stop services
```

---

## Contact & Support

For additional help:
- **Project Issues**: Review `callcard-ws-api/target/build.log`
- **Build Logs**: Check `build_phase3_*.log` files
- **Code Changes**: See `CATEGORY_4_FIXES_DETAILED_REPORT.md`, `COMPLETE_TYPE_CONVERSION_FIXES.md`
- **Architecture**: Review parent `CLAUDE.md`

---

**Document Version**: 1.0.0
**Last Reviewed**: 2025-12-22
**Status**: Ready for Phase 3+ Development

