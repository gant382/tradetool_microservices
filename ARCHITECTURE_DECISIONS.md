# CallCard Microservice - Architecture Decision Records

This document captures all major architecture decisions made during the extraction and implementation of the CallCard microservice from gameserver_v3 Platform_Core/ERP subsystem.

---

## ADR-001: Five-Module Maven Architecture

### Status
ACCEPTED

### Context
The CallCard functionality was originally monolithic within gameserver_v3. Extracting it as an independent microservice required establishing a clear modular structure to support:
- Separation of concerns (entity, API, business logic, deployment)
- Reusability of API interfaces for client generation
- Independent deployment and scaling
- Clear dependency boundaries

### Decision
Implement a five-module Maven structure:
1. **callcard-entity** (JAR) - JPA entities only
2. **callcard-ws-api** (JAR) - Service interfaces, DTOs, exceptions, utilities
3. **callcard-components** (JAR) - Business components, DAOs, query managers
4. **callcard-service** (JAR) - Service orchestration layer
5. **CallCard_Server_WS** (WAR) - Spring Boot deployable with CXF/Jersey endpoints

### Rationale
- **Entity Separation**: Isolates JPA entity definitions, allowing other modules to depend on entity classes without pulling in web service or component dependencies
- **API Separation**: Following gameserver_v3 pattern (WS_API_*), enables type-safe API contracts and potential client code generation without implementation details
- **Component Layer**: Contains DAOs, query managers, and business components that operate on entities
- **Service Layer**: Orchestration layer that uses components and implements service interfaces defined in ws-api
- **Server Module**: Spring Boot application that wires everything together and exposes SOAP/REST endpoints

This mirrors the proven WS_API pattern from gameserver_v3 (e.g., WS_API_DataImport → DataImport_Server_WS).

### Dependency Hierarchy
```
callcard-entity (foundation)
    ↑
callcard-ws-api (contracts)
    ↑
callcard-components (business logic)
    ↑
callcard-service (orchestration)
    ↑
CallCard_Server_WS (deployment)
```

### Benefits
- **Clear dependency flow**: Unidirectional dependencies prevent circular references
- **Testability**: Each layer can be tested independently
- **Reusability**: Components and service can be used by other microservices if needed
- **API-First**: WS-API module forces contract definition before implementation
- **Deployment flexibility**: Can deploy service independently or alongside other services

### Risks Mitigated
- **Circular dependencies**: Strict hierarchy prevents tight coupling
- **API breaking changes**: API module separation makes compatibility explicit
- **Monolithic growth**: Clear separation boundaries discourage feature creep

---

## ADR-002: Stub Implementation Strategy for External Dependencies

### Status
ACCEPTED

### Context
CallCard functionality depends on other subsystems that are not being extracted initially:
- ERP (SalesOrder, Invoice entities and management)
- User/Session Management
- AddressBook
- Application settings
- Solr search client

Rather than creating hard dependencies on full modules or deferring implementation, stub implementations provide:
- Compilation without full dependencies
- Clear contracts for future integration
- Ability to run CallCard independently for testing
- Type safety through Java interfaces/classes

### Decision
Create **external stub package** (`com.saicon.games.callcard.components.external`) containing:
- **Stub entity classes**: SalesOrder, Invoice, Addressbook, City, State, Postcode, etc.
- **Stub management interfaces**: IUserSessionManagement, ISalesOrderManagement, IAddressbookManagement, IMetadataComponent, IAppSettingsComponent, etc.
- **Stub utility classes**: SolrClient for search operations

### Implementation Patterns

#### 1. Stub Entity Classes
Minimal POJO implementations with fields and getters/setters:
```java
public class SalesOrder {
    private String salesOrderId;
    private String userId;
    private Date dateCreated;
    private String status;
    private Integer salesOrderStatus;
    private List<SalesOrderDetails> salesOrderDetails;

    // Getters and setters only
}
```

**Rationale**:
- Entities are typically just data containers (POJOs)
- No business logic needed for compilation
- Minimal footprint reduces coupling
- Easy to replace with real entities later

#### 2. Stub Interfaces with Default Methods
```java
public interface IUserSessionManagement {
    default boolean validateSession(String sessionId) {
        throw new UnsupportedOperationException(
            "Session validation should use GameInternalServiceClient for TALOS Core integration");
    }
}
```

**Rationale**:
- Interfaces define contracts without forcing implementation
- Default methods throw UnsupportedOperationException to catch unimplemented calls
- Callers can choose to use actual service clients instead
- Java 8+ default methods allow optional implementation

#### 3. External Service Client Integration
The real implementation delegates to external SOAP clients (e.g., GameInternalServiceClient for IGameInternalService) rather than implementing stubbed interfaces directly.

**Pattern**:
- Stub interfaces define what's needed
- Real SOAP/REST clients (GameInternalServiceClient) provide actual implementation
- Service layer can inject the real client and use it instead of stub
- Maintains separation between local logic and remote calls

### Stub Location Structure
```
callcard-components/
├── src/main/java/com/saicon/games/callcard/components/
│   ├── impl/
│   │   └── CallCardManagement.java (uses stubbed interfaces)
│   ├── external/
│   │   ├── entities/
│   │   │   ├── SalesOrder.java
│   │   │   ├── Invoice.java
│   │   │   ├── Addressbook.java
│   │   │   └── ...
│   │   ├── interfaces/
│   │   │   ├── IUserSessionManagement.java
│   │   │   ├── ISalesOrderManagement.java
│   │   │   └── ...
│   │   └── clients/
│   │       └── SolrClient.java
│   └── (main CallCard management)
```

### Benefits
- **Independent development**: CallCard can be implemented and tested without waiting for other modules
- **Type safety**: Java interfaces and classes instead of casting to Object
- **Clear contracts**: Explicit definition of what external dependencies are needed
- **Minimal coupling**: Stubs are simple POJOs, not full implementations
- **Documentation**: Stub interfaces document expected behavior

### Integration Strategy
When integrating with actual subsystems:
1. Replace stub entities with real JPA-mapped entities (same package name)
2. Implement stub interfaces in actual component modules
3. Or continue using service clients (SOAP/REST) for remote calls
4. Update JPA persistence.xml to include real entities
5. Minimal code changes needed in CallCard components

### Risks and Mitigations
- **Missing methods**: Some stub methods throw UnsupportedOperationException
  - *Mitigation*: Clear error messages guide developers to correct integration points
- **Type mismatches**: Stub versions may not match real entity structures
  - *Mitigation*: Register in git review process; compare with real entities regularly
- **Runtime surprises**: Unimplemented stubs fail at runtime, not compile time
  - *Mitigation*: Integration tests verify all paths use real implementations

---

## ADR-003: Exception Handling Strategy with BusinessLayerException

### Status
ACCEPTED

### Context
CallCard needs consistent error handling across:
- Business logic (components)
- Service orchestration
- Web service (SOAP/REST) endpoints

The original gameserver_v3 implementation uses BusinessLayerException for business-level errors. CallCard should maintain this pattern for:
- Consistency with legacy system
- Client compatibility (SOAP/REST APIs expect this format)
- Clear separation of business vs. technical errors

### Decision
Implement checked exception hierarchy:
```
Exception
  └─ BusinessLayerException (checked)
       └─ ExceptionTypeTO (DTO for SOAP serialization)
```

### Exception Implementation Details

#### BusinessLayerException Class
```java
public class BusinessLayerException extends Exception {
    private String errorCode;
    private ExceptionTypeTO exceptionType;

    public BusinessLayerException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.exceptionType = new ExceptionTypeTO(errorCode, message);
    }

    public String getErrorCode() { return errorCode; }
    public ExceptionTypeTO getExceptionType() { return exceptionType; }
}
```

**Design decisions**:
- **Checked exception**: Forces callers to handle business errors explicitly
- **Error code + message**: Separates machine-readable error codes from human-readable messages
- **ExceptionTypeTO field**: Enables direct SOAP serialization without conversion

#### ExceptionTypeTO Transfer Object
```java
public class ExceptionTypeTO {
    public static final String GENERAL_ERROR = "GEN_ERROR";
    public static final String VALIDATION_ERROR = "VAL_ERROR";
    public static final String BUSINESS_ERROR = "BUS_ERROR";

    private String errorCode;
    private String message;
}
```

### Exception Propagation Strategy

#### Throws Declaration vs. Try-Catch

**When to use `throws` (propagate)**:
- Business rules violations (validation errors)
- Data integrity issues
- Domain model constraints
- Allows caller to handle specific business errors

```java
public ICallCardService {
    ResponseListCallCard addCallCardRecords(...) throws BusinessLayerException;
}
```

**When to use `try-catch` (convert)**:
- Converting technical exceptions to business exceptions
- Wrapping infrastructure errors
- At service layer boundaries
- Before returning to SOAP/REST layer

```java
public class CallCardService implements ICallCardService {
    @Override
    public ResponseListCallCard addCallCardRecords(...) {
        try {
            CallCardDTO result = callCardManagement.updateCallCard(...);
            return new ResponseListCallCard("", ResponseStatus.OK, ...);
        } catch (BusinessLayerException e) {
            return new ResponseListCallCard(
                e.getErrorCode(),
                e.getMessage(),
                ResponseStatus.ERROR,
                null, 0
            );
        } catch (Exception e) {  // Technical exceptions
            return new ResponseListCallCard(
                ExceptionTypeTO.GENERAL_ERROR,
                "System error: " + e.getMessage(),
                ResponseStatus.ERROR,
                null, 0
            );
        }
    }
}
```

### Exception Handling at Service Boundaries

#### Component Layer
- **Throws**: BusinessLayerException for business rule violations
- **Catches**: JPA/database exceptions, converts to BusinessLayerException

```java
public class CallCardManagement implements ICallCardManagement {
    public CallCardDTO updateCallCard(...) throws BusinessLayerException {
        try {
            // Validate business rules
            if (callCard == null) {
                throw new BusinessLayerException(
                    "INVALID_CALLCARD",
                    "CallCard cannot be null"
                );
            }
            // Update database
            return callCardDao.update(callCard);
        } catch (DatabaseException e) {
            throw new BusinessLayerException(
                "DB_ERROR",
                "Failed to update CallCard: " + e.getMessage(),
                e
            );
        }
    }
}
```

#### Service Layer
- **Catches**: All exceptions from components
- **Converts**: To response objects with error codes
- **Does not throw**: Exceptions caught and converted to response objects

```java
public class CallCardService implements ICallCardService {
    @Override
    public ResponseListCallCard addCallCardRecords(...) {
        ResponseListCallCard toReturn;
        try {
            CallCardDTO result = callCardManagement.updateCallCard(...);
            toReturn = new ResponseListCallCard("", ResponseStatus.OK, ...);
        } catch (BusinessLayerException e) {
            toReturn = new ResponseListCallCard(
                e.getErrorCode(),
                e.getMessage(),
                ResponseStatus.ERROR,
                null, 0
            );
        } catch (Exception e) {
            toReturn = new ResponseListCallCard(
                ExceptionTypeTO.GENERAL_ERROR,
                "System error occurred",
                ResponseStatus.ERROR,
                null, 0
            );
        }
        return toReturn;
    }
}
```

#### REST/SOAP Layer
- **Receives**: Response objects with error codes already set
- **Returns**: Serialized response (XML/JSON) via JAXB/Jackson
- **Status codes**: HTTP error codes derived from ResponseStatus enum

### Response Object Design

All service methods return wrapper objects with error information:
```java
public class ResponseListCallCard {
    private String errorCode;
    private String errorMessage;
    private ResponseStatus status;  // OK, ERROR, WARNING
    private List<CallCardDTO> data;
    private int totalRecordCount;
}
```

**Benefits**:
- SOAP/REST clients get consistent error format
- Error codes can be localized by client
- No HTTP 500 errors for business logic violations
- Backward compatible with legacy clients

### Benefits
- **Consistency**: Single exception type across layers
- **Type safety**: Compile-time checking for business exceptions
- **Client compatibility**: SOAP/REST clients understand standard response format
- **Logging**: Error codes enable analytics and monitoring
- **Localization**: Error codes can be translated client-side
- **Legacy compatibility**: Matches original gameserver_v3 pattern

### Risks Mitigated
- **Unhandled exceptions**: Checked exception requires explicit handling
- **Inconsistent error responses**: StandardResponse format ensures consistency
- **Lost context**: ErrorCode + message + ExceptionTypeTO provides full context
- **Hard debugging**: Error codes enable tracing from client logs

---

## ADR-004: Type Conversion and Generic Return Types

### Status
ACCEPTED

### Context
Stub methods for external dependencies (SalesOrder management, Addressbook, etc.) need to compile and provide type hints without implementation. However, to minimize complexity, these methods:
- Can't return specific types (unknown entity structure)
- Need to be clearly marked as unimplemented
- Should not break code that calls them

### Decision
Use **generic return types** and **@SuppressWarnings annotations** for stub methods that will later be overridden:

```java
public class ISalesOrderManagement {
    /**
     * Stub method - will be implemented by real ERP module.
     * Returns generic Object type because real implementation is unknown.
     */
    @SuppressWarnings("unchecked")
    default List<Object> getSalesOrders(String userId) {
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    default Object getSalesOrderById(String orderId) {
        return null;
    }
}
```

### Type Conversion Patterns

#### 1. Unimplemented Stub Methods

```java
// Stub interface - method will be overridden
public interface IMetadataComponent {
    @SuppressWarnings("unchecked")
    default <T> List<T> getMetadata(String metadataType, String... params) {
        // Stub returns empty list - real implementation will return typed results
        return (List<T>) Collections.emptyList();
    }
}

// Later usage in CallCard components:
public class CallCardManagement {
    private IMetadataComponent metadataComponent;

    public void someMethod() {
        @SuppressWarnings("unchecked")
        List<String> metadata = (List<String>)
            metadataComponent.getMetadata("someType");
        // Use metadata
    }
}
```

#### 2. Cast Patterns for Type Safety

```java
// Stub returns Object - caller casts when real implementation is available
Object result = salesOrderManagement.getSalesOrderById(orderId);

// Safe cast with null check:
if (result instanceof SalesOrder) {
    SalesOrder order = (SalesOrder) result;
    // Use order
}
```

#### 3. @SuppressWarnings Justification

Warnings are suppressed for:
- **unchecked**: Generic type conversions with untyped stubs
- **unused**: Stub methods that aren't called in this module

```java
@SuppressWarnings("unchecked")  // Generic type erasure in stub
public <T> T getEntity(Class<T> type, String id) {
    return (T) new Object();
}

@SuppressWarnings("unused")  // Stub method for future integration
public void syncWithERP() {
    // Not implemented yet
}
```

### Why Not Type-Safe Alternatives?

We could use:
1. **Generics everywhere** - Adds complexity, still requires casts at integration
2. **Return Optional<?>** - Better but still requires client-side casts
3. **Reflection** - Runtime overhead, less type-safe
4. **Wildcard types** - Harder to use than Object in stub context

**Decision**: Object + explicit casts is clearest and most pragmatic for stubs.

### Integration Path

When integrating with real modules:

**Before**:
```java
@SuppressWarnings("unchecked")
List<Object> orders = (List<Object>) salesOrderManagement.getSalesOrders(userId);
```

**After** (real implementation):
```java
List<SalesOrder> orders = salesOrderManagement.getSalesOrders(userId);
// @SuppressWarnings no longer needed
```

### Benefits
- **Compilable stubs**: Code compiles without real implementations
- **Clear intention**: @SuppressWarnings documents expected casts
- **Minimal refactoring**: Small changes needed when real implementations arrive
- **Runtime flexibility**: Can switch implementations at runtime if needed

### Risks Mitigated
- **Type safety violations**: Suppressions are documented and reviewed
- **Unexpected casts**: Clear code reviews catch invalid casts
- **Hidden bugs**: Null checks and instanceof guards prevent ClassCastException

---

## ADR-005: Shared Database Architecture with gameserver_v3

### Status
ACCEPTED

### Context
CallCard is extracted from gameserver_v3 but needs to:
- Access the same database tables for CallCard entities
- Query related data from ERP, User, Invoice modules
- Maintain data consistency and relationships
- Avoid database duplication

However, it must also:
- Be independently deployable
- Not require full gameserver_v3 codebase
- Support migration path toward microservices separation

### Decision
**Single shared Microsoft SQL Server database** with entity mapping strategy:

1. **CallCard entities** (extracted): Maps to existing gameserver_v3 tables
   - CallCard, CallCardTemplate, CallCardTemplateEntry, etc.
   - Entities duplicated in callcard-entity module

2. **Related entities** (stubbed for now): Maps to gameserver_v3 tables
   - SalesOrder (ERP module)
   - Invoice (Invoice module)
   - Users (User module)
   - Application, Postcode (Generic module)
   - Addressbook entities

3. **JPA Persistence Configuration**: Declares all entities in single persistence.xml

### Database Connection Configuration

#### Spring Boot Configuration (application.yml)
```yaml
spring:
  datasource:
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    url: jdbc:sqlserver://[host]:[port];databaseName=gameserver_v3
    username: sa
    password: [password]

  jpa:
    hibernate:
      ddl-auto: validate  # Never create/modify schema
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServer2012Dialect
        cache:
          use_second_level_cache: true
          region.factory_class: org.hibernate.cache.ehcache.EhCacheRegionFactory
```

**Key decisions**:
- **ddl-auto: validate**: Never modify schema automatically, assumes schema exists
- **SQLServer2012Dialect**: Matches gameserver_v3 SQL Server version
- **Physical naming strategy**: Preserves existing table/column names
- **Second-level caching**: Improves query performance for read-heavy operations

#### JPA Persistence Unit (persistence.xml)
```xml
<persistence-unit name="callcard-pu" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

    <!-- CallCard Entities -->
    <class>com.saicon.games.entities.CallCard</class>
    <class>com.saicon.games.entities.CallCardTemplate</class>
    ...

    <!-- Related Entities (from other gameserver_v3 modules) -->
    <class>com.saicon.games.salesorder.entities.SalesOrder</class>
    <class>com.saicon.games.invoice.entities.Invoice</class>
    <class>com.saicon.user.entities.Users</class>
    ...

    <exclude-unlisted-classes>true</exclude-unlisted-classes>
    <shared-cache-mode>ENABLE_SELECTIVE</shared-cache-mode>
</persistence-unit>
```

### Entity Mapping Strategy

#### 1. Direct Entity Copy
CallCard entities are copied from gameserver_v3 with JPA annotations:
```java
@Entity
@Table(name = "CallCard")
@Cacheable
public class CallCard {
    @Id
    private String id;

    @Column(name = "UserId")
    private String userId;

    @ManyToOne
    @JoinColumn(name = "TemplateId")
    private CallCardTemplate template;
}
```

#### 2. Relationship Mapping
Foreign key relationships use JPA annotations:
```java
// One-to-many relationship
@OneToMany(mappedBy = "callCard", cascade = CascadeType.ALL)
private List<CallCardRefUser> refUsers;

// Many-to-one relationship
@ManyToOne
@JoinColumn(name = "ApplicationId")
private Application application;

// Many-to-many relationship
@ManyToMany
@JoinTable(name = "...")
private List<Users> users;
```

#### 3. Inheritance Mapping
If entities use inheritance (single table, joined table):
```java
@Entity
@Table(name = "BaseEntity")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "entity_type")
public abstract class BaseEntity { }
```

### Transaction Management

All database operations use Spring Transaction Management:
```java
@Configuration
@EnableTransactionManagement
public class DataSourceConfiguration {
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
```

Service layer uses @Transactional:
```java
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public void submitTransactions(CallCardDTO callCard) throws BusinessLayerException {
    // Database changes automatically rolled back on exception
}
```

### Query Patterns

#### 1. JPA Queries (Type-safe)
```java
@Query("SELECT c FROM CallCard c WHERE c.userId = :userId")
List<CallCard> findByUserId(@Param("userId") String userId);
```

#### 2. Native Queries (Complex joins)
```java
@Query(value = "SELECT cc.*, so.* FROM CallCard cc " +
               "LEFT JOIN SalesOrder so ON cc.RefItemId = so.Id " +
               "WHERE cc.UserId = ?1",
       nativeQuery = true)
List<Object[]> findCallCardsWithSalesOrders(String userId);
```

#### 3. Dynamic Queries
```java
public class ErpDynamicQueryManager {
    public List<?> executeQuery(String hql, Map<String, Object> params) {
        Query query = entityManager.createQuery(hql);
        for (Entry<String, Object> param : params.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }
        return query.getResultList();
    }
}
```

### Multi-Tenancy Filtering

Some deployments need organization-based filtering:
```java
public class MultiTenantQueryFilter {
    /**
     * Automatically adds WHERE clause for organization isolation
     */
    @Transactional
    public List<CallCard> findByOrgId(String orgId) {
        return entityManager.createQuery(
            "SELECT c FROM CallCard c WHERE c.organizationId = :orgId",
            CallCard.class
        )
        .setParameter("orgId", orgId)
        .getResultList();
    }
}
```

### Benefits
- **No duplication**: Single source of truth in gameserver_v3 database
- **Relationship integrity**: Foreign keys and joins work correctly
- **Transaction consistency**: ACID guarantees across CallCard and related data
- **Minimal deployment**: No separate database setup needed
- **Change management**: Schema changes made once in gameserver_v3
- **Query power**: Can join across CallCard and other modules in single queries

### Risks and Mitigations

| Risk | Mitigation |
|------|-----------|
| **Data mismatch** | Validate/annotation-based checks on entity updates |
| **Schema changes** | Coordinate with gameserver_v3 team; version control schema |
| **Performance** | Use second-level caching; optimize frequently-used queries |
| **Tight coupling** | Stub interfaces enable future separation |
| **Transaction isolation** | Connection pooling with proper isolation levels |
| **Deployment timing** | Database migration scripts run before microservice deployment |

### Migration Path to Separate Database

When ready to separate:
1. Create CallCard-specific database
2. Copy CallCard tables
3. Update datasource configuration
4. For external entities, use service clients (REST/SOAP) instead of JPA
5. Minimal code changes thanks to stub interfaces

---

## ADR-006: Spring Boot 2.7.x with Java 1.8+ Target

### Status
ACCEPTED

### Context
CallCard extraction needs to balance:
- Compatibility with gameserver_v3 ecosystem (legacy Java versions)
- Modern Spring ecosystem (Spring Boot 2.7.x is LTS)
- Apache CXF 3.5.x (requires Spring 5.x+)
- Jersey REST support
- Java language features for maintainability

### Decision
**Spring Boot 2.7.18** (LTS) with **Java 1.8+ compilation target**

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
    <relativePath/>
</parent>

<properties>
    <java.version>1.8</java.version>
    <spring-boot.version>2.7.18</spring-boot.version>
    <cxf.version>3.5.9</cxf.version>
</properties>
```

### Spring Boot Version Selection

#### Why 2.7.x (not 3.x)?
- **Compatibility**: Fewer breaking changes from Spring 4.x used in gameserver_v3
- **LTS support**: Spring Boot 2.7.x has extended support until 2026
- **SOAP support**: CXF 3.5.x works well with Spring 5.x/Boot 2.7.x
- **Migration path**: Easier upgrade path to 3.x later if needed
- **Dependency stability**: All transitive dependencies are stable

#### Spring 3.x rejected due to:
- Requires Spring Framework 6.x (major breaking changes)
- Requires Java 17+ (too aggressive for legacy systems)
- SOAP/CXF support less stable
- Client libraries may not support it yet (GameInternalService SOAP client)

### Starter Dependencies

#### Web & Core
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

**Includes**:
- Embedded Tomcat servlet container
- Spring MVC for REST endpoints
- Hibernate JPA implementation
- Spring Data JPA repositories
- Caffeine caching provider

#### SOAP & REST
```xml
<!-- Apache CXF SOAP -->
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-spring-boot-starter-jaxws</artifactId>
    <version>${cxf.version}</version>
</dependency>

<!-- Jersey REST -->
<dependency>
    <groupId>org.glassfish.jersey.containers</groupId>
    <artifactId>jersey-container-servlet</artifactId>
</dependency>
```

**Why both?**
- **CXF**: Legacy SOAP clients compatibility, matches gameserver_v3
- **Jersey**: Modern REST API support, API-first design
- **Dual protocols**: Allows parallel migration path

#### Additional Core
```xml
<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Monitoring -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Resilience -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
</dependency>
```

### Configuration Management

#### Profile-Based Configuration
```yaml
# application.yml - Base configuration
spring:
  profiles:
    active: dev

---
# application-dev.yml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=gameserver_v3

---
# application-staging.yml (activated by: pmi-staging-v3-1 Maven profile)
spring:
  datasource:
    url: jdbc:sqlserver://staging-server:1433;databaseName=gameserver_v3_staging

---
# application-production.yml (activated by: pmi-production-v3-1 Maven profile)
spring:
  datasource:
    url: jdbc:sqlserver://production-server:1433;databaseName=gameserver_v3_prod
```

#### Maven Profile Mapping
```xml
<profiles>
    <profile>
        <id>dev</id>
        <activation><activeByDefault>true</activeByDefault></activation>
        <properties><spring.profiles.active>dev</spring.profiles.active></properties>
    </profile>
    <profile>
        <id>pmi-staging-v3-1</id>
        <properties><spring.profiles.active>staging</spring.profiles.active></properties>
    </profile>
    <profile>
        <id>pmi-production-v3-1</id>
        <properties><spring.profiles.active>production</spring.profiles.active></properties>
    </profile>
</profiles>
```

**Usage**:
```bash
mvn spring-boot:run -Pdev                    # Dev profile
mvn spring-boot:run -Ppmi-staging-v3-1      # Staging profile
mvn spring-boot:run -Ppmi-production-v3-1   # Production profile
```

### Java Version Compatibility

**Compilation Target: 1.8**
```xml
<properties>
    <java.version>1.8</java.version>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
</properties>
```

**Why 1.8?**
- Compatible with gameserver_v3 entities (compiled as 1.6+)
- Supports Java 8 features used in modern code:
  - Lambda expressions (default methods in stub interfaces)
  - Stream API (query results processing)
  - Optional (null-safe handling)
  - Method references
- Runs on Java 8+ runtime (8, 11, 17, 21, etc.)

**Runtime Requirements**:
- Minimum: Java 8 (same as compilation target)
- Tested: Java 8, 11, 17
- Recommended: Java 11 LTS or 17 LTS for production

### Spring Boot Application Structure

#### Main Application Class
```java
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.saicon.callcard",
    "com.saicon.games.callcard"
})
@EntityScan(basePackages = {
    "com.saicon.games.entities",
    "com.saicon.games.salesorder.entities",
    "com.saicon.games.invoice.entities",
    "com.saicon.user.entities",
    "com.saicon.application.entities",
    "com.saicon.addressbook.entities",
    "com.saicon.generic.entities"
})
@EnableJpaRepositories(basePackages = {
    "com.saicon.games.entities"
})
public class CallCardMicroserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CallCardMicroserviceApplication.class, args);
    }
}
```

**Annotations**:
- **@SpringBootApplication**: Enables auto-configuration, component scanning, configuration
- **@ComponentScan**: Explicit bean discovery from CallCard and gameserver_v3 packages
- **@EntityScan**: JPA entity discovery (non-standard packages)
- **@EnableJpaRepositories**: Spring Data repository auto-implementation

#### Configuration Classes
```java
@Configuration
public class CxfConfiguration {
    // CXF SOAP endpoint configuration
}

@Configuration
public class JerseyConfiguration {
    // Jersey REST configuration
}

@Configuration
public class DataSourceConfiguration {
    // Database and JPA configuration
}

@Configuration
public class ComponentConfiguration {
    // Bean definitions for services and DAOs
}
```

### Auto-Configuration Benefits
Spring Boot 2.7.x provides auto-configuration for:
- DataSource (HikariCP connection pool)
- JPA/Hibernate EntityManager
- Transaction management
- Caching (Caffeine/EhCache)
- Logging (SLF4J/Logback)
- Web servlet container
- Actuator metrics
- JSON serialization (Jackson)

**Benefit**: Minimal configuration files, sensible defaults, easy overrides.

### Benefits
- **LTS support**: 2.7.18 supported until 2026
- **Ecosystem maturity**: Stable, well-documented, widely used
- **SOAP/REST support**: CXF 3.5.x + Jersey both well-integrated
- **Java 8 features**: Modern code style with compatibility
- **Auto-configuration**: Minimal XML, properties-based setup
- **Embedded container**: No separate application server needed
- **Production-ready**: Actuator, metrics, health checks built-in
- **Migration path**: Clear upgrade path to Spring Boot 3.x later

### Risks Mitigated
- **Version conflicts**: Spring Boot dependency management resolves versions
- **Transitive dependencies**: Auto-excluded in starter POMs
- **Configuration complexity**: Profiles and application.yml manage environments
- **Runtime compatibility**: Java 8+ runtime compatibility maintained

---

## ADR-007: Multi-Module Maven Build with No Root Aggregator

### Status
ACCEPTED

### Context
Unlike typical Maven multi-module projects with a root aggregator POM, CallCard follows gameserver_v3 pattern with a **parent POM but no aggregator**. This means:
- No single root `mvn clean install` command
- Modules built individually in dependency order
- Each module has independent lifecycle

### Decision
**Parent POM approach with explicit build order**:

```xml
<!-- tradetool_middleware/pom.xml (parent) -->
<project>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
    </parent>

    <groupId>com.saicon.games</groupId>
    <artifactId>tradetool_middleware</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>callcard-entity</module>
        <module>callcard-ws-api</module>
        <module>callcard-components</module>
        <module>callcard-service</module>
        <module>CallCard_Server_WS</module>
    </modules>

    <dependencyManagement>
        <!-- Managed versions for all modules -->
    </dependencyManagement>
</project>
```

### Why No Aggregator?

#### What is a "Parent" vs. "Aggregator"?
**Parent POM** (this architecture):
- `<packaging>pom</packaging>` declares it's not a JAR/WAR
- `<modules>` section defines submodules
- Provides dependency versions via `<dependencyManagement>`
- Does NOT build anything itself
- Child modules declare `<parent>` reference
- `mvn install` on parent only sets up inheritance, doesn't build children

**Aggregator POM** (common pattern):
- Same as parent, but command runs on parent automatically cascades to children
- Builds all modules with single command: `mvn clean install`

**This project**: Parent without aggregation
- Must explicitly build each module
- Enables flexible build order
- Matches gameserver_v3 pattern (IconParentPom external)

### Build Order Strategy

CallCard uses this build order due to dependencies:

```
1. callcard-entity
   └─ Depends on: (none)

2. callcard-ws-api
   └─ Depends on: callcard-entity

3. callcard-components
   └─ Depends on: callcard-entity, callcard-ws-api

4. callcard-service
   └─ Depends on: callcard-components, callcard-ws-api

5. CallCard_Server_WS
   └─ Depends on: all above modules
```

**Build script example**:
```bash
#!/bin/bash
cd tradetool_middleware

echo "Building CallCard Microservice..."
echo "================================="

cd callcard-entity && mvn clean install || exit 1
cd ../callcard-ws-api && mvn clean install || exit 1
cd ../callcard-components && mvn clean install || exit 1
cd ../callcard-service && mvn clean install || exit 1
cd ../CallCard_Server_WS && mvn clean package || exit 1

echo "================================="
echo "CallCard build completed successfully!"
```

### Dependency Management

#### Parent POM DependencyManagement
```xml
<dependencyManagement>
    <dependencies>
        <!-- Internal CallCard modules -->
        <dependency>
            <groupId>com.saicon.games</groupId>
            <artifactId>callcard-entity</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.saicon.games</groupId>
            <artifactId>callcard-ws-api</artifactId>
            <version>${project.version}</version>
        </dependency>
        <!-- ... -->

        <!-- External frameworks -->
        <dependency>
            <groupId>org.apache.cxf</groupId>
            <artifactId>cxf-spring-boot-starter-jaxws</artifactId>
            <version>${cxf.version}</version>
        </dependency>
        <!-- ... -->
    </dependencies>
</dependencyManagement>
```

**Benefits**:
- Single version definition for each dependency
- Child modules reference without version (inherited)
- Prevents version conflicts across modules
- Easy updates (change version in parent, rebuild children)

#### Child Module Dependencies
```xml
<!-- callcard-components/pom.xml -->
<dependencies>
    <!-- No version specified - comes from parent -->
    <dependency>
        <groupId>com.saicon.games</groupId>
        <artifactId>callcard-entity</artifactId>
    </dependency>
    <dependency>
        <groupId>com.saicon.games</groupId>
        <artifactId>callcard-ws-api</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
</dependencies>
```

### Maven Profiles for Builds

#### Environment Profiles
```xml
<profiles>
    <profile>
        <id>dev</id>
        <activation><activeByDefault>true</activeByDefault></activation>
        <properties><spring.profiles.active>dev</spring.profiles.active></properties>
    </profile>

    <profile>
        <id>pmi-staging-v3-1</id>
        <properties><spring.profiles.active>staging</spring.profiles.active></properties>
    </profile>

    <profile>
        <id>pmi-production-v3-1</id>
        <properties><spring.profiles.active>production</spring.profiles.active></properties>
    </profile>
</profiles>
```

**Usage**:
```bash
mvn clean package -Ppmi-production-v3-1   # Build for production
mvn clean package -Ppmi-staging-v3-1      # Build for staging
```

### Advantage Over Aggregator Pattern

| Aspect | Aggregator | No Aggregator (This) |
|--------|-----------|----------------------|
| **Build all** | `mvn clean install` from root | Must build each module |
| **Control** | Less control, cascades to all | More explicit, can skip modules |
| **Partial builds** | Must skip modules with `-pl` | Natural - build only what's needed |
| **Dependencies** | Requires POM ordering | Explicit build order script |
| **Flexibility** | Limited to parent-child relationship | Can have external parents |

### Configuration Strategy

Each module has own configuration:
- **Entity module**: JPA configuration (persistence.xml)
- **WS-API module**: DTO/Exception configuration
- **Components module**: Spring beans for DAOs
- **Service module**: Service orchestration
- **Server-WS module**: Main application configuration

Minimal duplication because Spring Boot provides sensible defaults.

### Benefits
- **Flexibility**: Build order can be optimized
- **Explicit dependencies**: Build failures are clear
- **Reusability**: Each module can be consumed independently
- **Matches gameserver_v3**: Uses same pattern as parent project
- **Clear layering**: Dependencies flow one direction
- **Fast incremental builds**: Can rebuild only changed modules

### Risks and Mitigations

| Risk | Mitigation |
|------|-----------|
| **Easy to forget build order** | Build script or Maven enforcer plugin |
| **Parallel builds fail** | Always build in dependency order |
| **Different versions of same lib** | DependencyManagement in parent |
| **Complex to understand** | Clear documentation and scripts |

---

## ADR-008: Test Strategy - Integration and Unit Testing

### Status
ACCEPTED

### Context
CallCard needs comprehensive testing covering:
- JPA entity mapping to shared database
- Service layer logic with business exceptions
- SOAP/REST endpoint serialization
- Integration with external TALOS Core service
- Mock/stub behavior for unimplemented components

### Decision
**Mixed test strategy**:
1. **Unit tests**: Component and service layer logic (TestNG)
2. **Integration tests**: Database operations and SOAP endpoints (Spring TestContainers)
3. **Mock strategy**: External services mocked, database real

### Unit Test Approach

#### Component Layer Testing
```java
@Test(groups = "unit")
public class CallCardManagementTest {

    private CallCardManagement management;
    private IGenericDAO<CallCard> mockDao;

    @BeforeClass
    public void setup() {
        management = new CallCardManagement();
        mockDao = mock(IGenericDAO.class);
        management.setCallCardDao(mockDao);
    }

    @Test
    public void testUpdateCallCard_validData_succeeds() throws BusinessLayerException {
        // Arrange
        CallCard card = new CallCard();
        card.setId("123");
        card.setUserId("user1");

        when(mockDao.update(card)).thenReturn(card);

        // Act
        CallCardDTO result = management.updateCallCard(
            "group1", "game1", "app1", "user1",
            Arrays.asList(new CallCardDTO())
        );

        // Assert
        assertNotNull(result);
        assertEquals(result.getId(), "123");
    }

    @Test(expectedExceptions = BusinessLayerException.class)
    public void testUpdateCallCard_nullData_throwsException()
            throws BusinessLayerException {
        // Act
        management.updateCallCard("group1", "game1", "app1", "user1", null);
    }
}
```

**Approach**:
- Mock all DAO dependencies
- Test business logic in isolation
- Fast execution (no database)
- TestNG @Test annotations for explicit test methods

#### Service Layer Testing
```java
@Test(groups = "unit")
public class CallCardServiceTest {

    private CallCardService service;
    private ICallCardManagement mockManagement;

    @BeforeClass
    public void setup() {
        service = new CallCardService();
        mockManagement = mock(ICallCardManagement.class);
        service.setCallCardManagement(mockManagement);
    }

    @Test
    public void testAddCallCardRecords_componentSuccess_returnsOk()
            throws BusinessLayerException {
        // Arrange
        CallCardDTO cardDto = new CallCardDTO();
        when(mockManagement.updateCallCard(
            anyString(), anyString(), anyString(),
            anyString(), anyList()
        )).thenReturn(cardDto);

        // Act
        ResponseListCallCard response = service.addCallCardRecords(
            "group1", "game1", "app1", "user1",
            Arrays.asList(cardDto)
        );

        // Assert
        assertEquals(response.getStatus(), ResponseStatus.OK);
        assertNotNull(response.getCallCards());
    }

    @Test
    public void testAddCallCardRecords_componentThrowsException_returnsError()
            throws BusinessLayerException {
        // Arrange
        when(mockManagement.updateCallCard(
            anyString(), anyString(), anyString(),
            anyString(), anyList()
        )).thenThrow(
            new BusinessLayerException("CC_001", "Invalid CallCard")
        );

        // Act
        ResponseListCallCard response = service.addCallCardRecords(
            "group1", "game1", "app1", "user1", null
        );

        // Assert
        assertEquals(response.getStatus(), ResponseStatus.ERROR);
        assertEquals(response.getErrorCode(), "CC_001");
    }
}
```

**Approach**:
- Mock component dependencies
- Test error handling and response conversion
- Verify SOAP/REST response structure
- Fast execution

### Integration Test Approach

#### Database Integration Tests
```java
@ContextConfiguration(classes = {
    DataSourceConfiguration.class,
    ComponentConfiguration.class
})
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@Listeners(DirtiesContextTestExecutionListener.class)
public class CallCardDaoIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private IGenericDAO<CallCard> callCardDao;

    @Autowired
    private EntityManager entityManager;

    @BeforeMethod(alwaysRun = true)
    @Transactional
    public void setup() {
        // Create test data
        CallCard card = new CallCard();
        card.setId("test-1");
        card.setUserId("user1");
        card.setGroupId("group1");
        callCardDao.save(card);
    }

    @Test
    @Transactional
    public void testCallCardPersistence() {
        // Query database
        CallCard result = callCardDao.findById("test-1");

        // Verify
        assertNotNull(result);
        assertEquals(result.getUserId(), "user1");
    }

    @Test
    @Transactional
    public void testCallCardUpdate() {
        CallCard card = callCardDao.findById("test-1");
        card.setUserId("user2");
        callCardDao.update(card);

        entityManager.flush();
        entityManager.clear();

        CallCard updated = callCardDao.findById("test-1");
        assertEquals(updated.getUserId(), "user2");
    }
}
```

**Approach**:
- Use H2 in-memory database for speed
- Spring TestContext for dependency injection
- @Transactional for test isolation
- Create/drop schema per test
- TestNG for test orchestration

#### SOAP Endpoint Integration Tests
```java
@ContextConfiguration(classes = {
    CallCardMicroserviceApplication.class
})
@WebAppConfiguration
public class CallCardSoapEndpointTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeClass
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testCallCardServiceWsdl() throws Exception {
        mockMvc.perform(
            get("/cxf/CallCardService?wsdl")
                .accept(MediaType.APPLICATION_XML)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }

    @Test
    public void testCallCardSoapRequest() throws Exception {
        String soapRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:cc=\"http://ws.callCard.saicon.com/\">" +
            "<soap:Body>" +
            "<cc:listPendingCallCard>" +
            "<userId>user1</userId>" +
            "</cc:listPendingCallCard>" +
            "</soap:Body>" +
            "</soap:Envelope>";

        mockMvc.perform(
            post("/cxf/CallCardService")
                .contentType(MediaType.APPLICATION_XML)
                .content(soapRequest)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }
}
```

**Approach**:
- Use MockMvc for servlet testing
- Real Spring context with embedded server
- Test WSDL generation
- Test SOAP request/response handling

### Mock Strategy for External Services

#### Mocking TALOS Core SOAP Client
```java
@Configuration
@Profile("test")
public class TestGameInternalServiceClientConfig {

    @Bean
    public IGameInternalService gameInternalService() {
        // Return mock that doesn't call actual TALOS Core
        IGameInternalService mockService = mock(IGameInternalService.class);

        when(mockService.validateSession("valid-session"))
            .thenReturn(true);

        when(mockService.validateSession("invalid-session"))
            .thenReturn(false);

        return mockService;
    }
}
```

**Usage in tests**:
```java
@ContextConfiguration(classes = {
    TestGameInternalServiceClientConfig.class,
    // Other configs
})
public class SessionValidationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private IGameInternalService gameInternalService;

    @Test
    public void testValidSession() {
        boolean valid = gameInternalService.validateSession("valid-session");
        assertTrue(valid);
    }
}
```

#### Mocking External Component Stubs
```java
@Test
public void testCallCardWithSalesOrderStub() throws BusinessLayerException {
    // Arrange
    ISalesOrderManagement mockSalesOrder = mock(ISalesOrderManagement.class);

    when(mockSalesOrder.getSalesOrderById("order-1"))
        .thenReturn(createTestSalesOrder("order-1"));

    management.setSalesOrderManagement(mockSalesOrder);

    // Act
    List<CallCardDTO> results = management.getCallCardsFromTemplate(
        "user1", "group1", "game1", "app1"
    );

    // Assert
    assertNotNull(results);
}
```

### Test Configuration (application-test.yml)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: true

  test:
    mockmvc:
      print: true

# Disable external service calls
game:
  internal:
    service:
      url: mock://local
```

### Test Dependencies

```xml
<dependencies>
    <!-- Testing framework -->
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Spring Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
        <exclusions>
            <exclusion>
                <groupId>org.junit.vintage</groupId>
                <artifactId>junit-vintage-engine</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- H2 Database (in-memory testing) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Test Organization

```
callcard-*/src/test/java/com/saicon/games/callcard/
├── components/
│   ├── CallCardManagementTest.java
│   └── QueryManagerTest.java
├── service/
│   ├── CallCardServiceTest.java
│   └── CallCardStatisticsServiceTest.java
├── integration/
│   ├── CallCardDaoIntegrationTest.java
│   └── CallCardSoapEndpointTest.java
└── fixtures/
    ├── CallCardTestDataBuilder.java
    └── TestDataFactory.java
```

### Running Tests

```bash
# All tests
mvn test

# Unit tests only
mvn test -Dgroups="unit"

# Integration tests only
mvn test -Dgroups="integration"

# Specific test class
mvn test -Dtest=CallCardManagementTest

# With coverage report
mvn test jacoco:report
```

### Benefits
- **Comprehensive coverage**: Unit + integration tests
- **Fast unit tests**: Mocked dependencies execute quickly
- **Real integration tests**: Database operations validated
- **Isolation**: Each test is independent via H2 in-memory DB
- **TestNG groups**: Flexible test categorization
- **Spring integration**: Full context testing for beans
- **SOAP testing**: Real endpoint validation
- **Mock strategy**: External services mocked safely

### Risks Mitigated
- **Database coupling**: In-memory H2 for test isolation
- **Flaky tests**: Transactional rollback ensures clean state
- **Slow builds**: Mock-based unit tests are fast
- **External service failures**: Mocks prevent dependency on TALOS Core
- **Test pollution**: DirtiesContext and transaction rollback

---

## Summary of Architecture Decisions

| ADR | Decision | Status | Key Benefit |
|-----|----------|--------|------------|
| **ADR-001** | Five-module Maven structure | ACCEPTED | Clear separation of concerns, reusable components |
| **ADR-002** | Stub implementations for external deps | ACCEPTED | Independent development, type-safe contracts |
| **ADR-003** | BusinessLayerException with code/message | ACCEPTED | Consistent error handling, SOAP serialization |
| **ADR-004** | Generic types with @SuppressWarnings | ACCEPTED | Compilable stubs, minimal integration friction |
| **ADR-005** | Shared database with gameserver_v3 | ACCEPTED | No duplication, transaction consistency |
| **ADR-006** | Spring Boot 2.7.x with Java 1.8+ | ACCEPTED | LTS support, modern features, compatibility |
| **ADR-007** | Multi-module without aggregator | ACCEPTED | Flexible build order, explicit dependencies |
| **ADR-008** | Mixed unit + integration tests | ACCEPTED | Fast feedback + real validation |

---

## Implementation Checklist

- [x] Module structure created (5 modules)
- [x] Parent POM with dependency management
- [x] Entity layer with JPA mapping
- [x] API layer with DTOs and exceptions
- [x] Component layer with DAOs and business logic
- [x] Service layer with orchestration
- [x] Spring Boot WAR deployment module
- [x] CXF SOAP endpoint configuration
- [x] Jersey REST endpoint configuration
- [x] Database configuration with HikariCP
- [x] Stub implementations for external dependencies
- [x] Exception handling strategy implemented
- [x] Multi-tenancy query filtering
- [x] Test infrastructure (H2, TestNG, Mockito)
- [x] Spring profiles for environments (dev, staging, production)
- [x] Actuator health checks
- [x] Caching configuration (Caffeine/EhCache)
- [x] Resilience4j circuit breaker for TALOS Core
- [x] OpenAPI/Swagger documentation
- [x] Logging configuration (Logback)

---

## Related Documents

- **BUILD.md** - Detailed build instructions
- **DEPLOYMENT.md** - Production deployment guide
- **API.md** - SOAP/REST endpoint documentation
- **INTEGRATION.md** - Integration with TALOS Core
- **TESTING.md** - Test execution and coverage
- **PERFORMANCE.md** - Caching and optimization strategies
- **CLAUDE.md** - Development guidelines

---

**Document Version**: 1.0
**Last Updated**: 2025-12-22
**Status**: Complete and Ready for Review
