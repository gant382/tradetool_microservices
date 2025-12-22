# CallCard Microservice - Dependencies Quick Reference Guide

## Dependency Hierarchy Diagram

```
                          CallCard_Server_WS (WAR)
                         /       |       |       \
                        /        |       |        \
                       /         |       |         \
              callcard-entity    |       |    callcard-service
                       \         |       |         /
                        \        |       |        /
                         \       |       |       /
                     callcard-ws-api    |      /
                              \         |     /
                               \        |    /
                            callcard-components

Foundation: callcard-entity (bottom, no dependencies on other CallCard modules)
```

## Module Details

### Level 1: callcard-entity (Foundation JAR)
**Purpose:** JPA entity definitions

**Dependencies:**
- spring-boot-starter-data-jpa (Spring Boot parent)
- hibernate-core (Spring Boot parent)
- javax.validation:validation-api (Spring Boot parent)
- commons-lang3 (Spring Boot parent)

**Exports to:** callcard-ws-api, callcard-components

---

### Level 2: callcard-ws-api (API Layer JAR)
**Purpose:** SOAP/REST API interfaces and DTOs

**Internal Dependencies:**
- callcard-entity (compile)

**External Dependencies:**
- JAX-WS APIs: jaxws-api, javax.jws-api
- JAX-RS API: javax.ws.rs-api
- Jackson: jackson-databind, jackson-annotations
- CXF Core: cxf-core (for annotations)
- Swagger: swagger-annotations
- Validation API

**Exports to:** callcard-components, callcard-service, CallCard_Server_WS

---

### Level 3: callcard-components (Business Logic JAR)
**Purpose:** DAOs, repositories, business components

**Internal Dependencies:**
- callcard-entity (compile)
- callcard-ws-api (compile)

**External Dependencies:**
- spring-boot-starter-data-jpa
- spring-tx
- caffeine (caching)
- commons-lang3

**Testing:** spring-boot-starter-test, testng (test scope)

**Exports to:** callcard-service, CallCard_Server_WS

---

### Level 4: callcard-service (Service Orchestration JAR)
**Purpose:** Business logic, service layer, external service clients

**Internal Dependencies:**
- callcard-components (compile)
- callcard-ws-api (compile)
- callcard-entity (transitive via callcard-components)

**External Dependencies:**
- spring-boot-starter
- spring-tx
- cxf-spring-boot-starter-jaxws (TALOS Core SOAP client)
- resilience4j-spring-boot2 (circuit breaker)

**Testing:** spring-boot-starter-test (test scope)

**Exports to:** CallCard_Server_WS

---

### Level 5: CallCard_Server_WS (Deployment WAR)
**Purpose:** Web application container, endpoint exposure

**Internal Dependencies:**
- callcard-entity (compile)
- callcard-components (compile)
- callcard-service (compile)
- callcard-ws-api (compile)

**External Dependencies:**

**Spring Boot Core:**
- spring-boot-starter-web (Tomcat, Spring MVC)
- spring-boot-starter-data-jpa (JPA/Hibernate)
- spring-boot-starter-actuator (monitoring)
- spring-boot-starter-cache (caching)
- spring-boot-starter-validation (bean validation)

**SOAP Framework:**
- cxf-spring-boot-starter-jaxws
- cxf-rt-features-logging

**REST Framework:**
- jersey-container-servlet
- jersey-hk2 (dependency injection)
- jersey-media-json-jackson (JSON support)
- jersey-spring5 (Spring integration)

**Database:**
- mssql-jdbc (SQL Server driver)

**Caching:**
- caffeine
- ehcache

**Monitoring & Resilience:**
- resilience4j-spring-boot2
- springdoc-openapi-ui (OpenAPI documentation)

**Testing:**
- spring-boot-starter-test (test scope)
- testng (test scope)

**Plugins:**
- spring-boot-maven-plugin (repackage)
- maven-war-plugin (3.4.0)

---

## Version Management Strategy

### Centralized in Parent POM

All versions managed via properties in `tradetool_middleware/pom.xml`:

```xml
<properties>
    <java.version>1.8</java.version>
    <spring-boot.version>2.7.18</spring-boot.version>
    <cxf.version>3.5.9</cxf.version>
    <jersey.version>2.39.1</jersey.version>
    <hibernate.version>5.6.15.Final</hibernate.version>
    <mssql-jdbc.version>12.4.2.jre8</mssql-jdbc.version>
    <testng.version>7.8.0</testng.version>
    <caffeine.version>2.9.3</caffeine.version>
    <resilience4j.version>1.7.1</resilience4j.version>
    <springdoc.version>1.7.0</springdoc.version>
</properties>
```

### Spring Boot Versions (inherited from parent)
- Jackson: 2.14.2
- Hibernate: 5.6.15.Final
- Spring Framework: 5.3.34
- Logback: 1.2.13

---

## Dependency Usage Matrix

| Dependency | Entity | WS-API | Components | Service | Server-WS |
|------------|--------|--------|------------|---------|-----------|
| callcard-entity | - | C | C | T | C |
| callcard-ws-api | - | - | C | C | C |
| callcard-components | - | - | - | C | C |
| callcard-service | - | - | - | - | C |
| Spring Boot Web | - | - | - | - | C |
| Spring JPA | C | - | C | - | C |
| Hibernate | C | - | - | - | - |
| Jackson | - | C | - | - | C |
| CXF | - | C | - | C | C |
| Jersey | - | - | - | - | C |
| Caching | - | - | C | - | C |
| Resilience4j | - | - | - | C | C |
| SQL Server | - | - | - | - | C |

**C = Compile Scope | T = Transitive**

---

## Build Order & Dependency Resolution

When building the parent POM, Maven processes modules in this order:

1. **callcard-entity** (no internal dependencies, builds first)
   ```bash
   mvn clean install
   ```

2. **callcard-ws-api** (depends on: callcard-entity)
   ```bash
   mvn clean install
   ```

3. **callcard-components** (depends on: callcard-entity, callcard-ws-api)
   ```bash
   mvn clean install
   ```

4. **callcard-service** (depends on: callcard-components, callcard-ws-api)
   ```bash
   mvn clean install
   ```

5. **CallCard_Server_WS** (depends on: all above)
   ```bash
   mvn clean install
   ```

**From parent directory:**
```bash
cd tradetool_middleware
mvn clean package -Ppmi-production-v3-1
```

---

## Transitive Dependency Examples

### Example 1: callcard-service using JPA
- Declares: cxf-spring-boot-starter-jaxws, resilience4j-spring-boot2
- Gets transitively: spring-boot-starter (has JPA), callcard-components (has JPA)
- Can use: EntityManager, Spring Data repositories via transitivity

### Example 2: callcard-components using Jackson
- Declares: spring-boot-starter-data-jpa, callcard-ws-api
- Gets transitively: Jackson via callcard-ws-api
- Can use: ObjectMapper via transitivity

### Example 3: CallCard_Server_WS using all layers
- Declares: all 4 internal modules
- Gets transitively: All their dependencies
- Result: Complete Spring Boot application with all frameworks

---

## Scope Guide

### Compile Scope
Used for:
- Inter-module dependencies (e.g., callcard-entity in callcard-ws-api)
- Framework dependencies needed at runtime (e.g., spring-boot-starter-web)
- Libraries needed in production (e.g., mssql-jdbc)

**Default scope - can be omitted**

### Test Scope
Used for:
- Testing frameworks (spring-boot-starter-test, testng)
- Only included in test classpath
- NOT included in packaged WAR/JAR

**Must be explicitly declared**

---

## Framework Integration Points

### SOAP Web Services (CXF)
- **API Layer:** callcard-ws-api
  - Declares: javax.xml.ws:jaxws-api, javax.jws:javax.jws-api
  - Contains: @WebService, @WebMethod interfaces
- **Service Layer:** callcard-service
  - Declares: cxf-spring-boot-starter-jaxws
  - Can: Call TALOS Core SOAP services
- **Server Layer:** CallCard_Server_WS
  - Declares: cxf-spring-boot-starter-jaxws, cxf-rt-features-logging
  - Exposes: WSDL endpoints

### REST Web Services (Jersey)
- **API Layer:** callcard-ws-api
  - Declares: javax.ws.rs:javax.ws.rs-api
  - Contains: @Path, @GET, @POST, etc.
- **Server Layer:** CallCard_Server_WS
  - Declares: jersey-container-servlet, jersey-media-json-jackson
  - Exposes: REST endpoints with JSON

### Data Access (JPA/Hibernate)
- **Entity Layer:** callcard-entity
  - Declares: spring-boot-starter-data-jpa, hibernate-core
  - Contains: @Entity, @Id, relationship mappings
- **Component Layer:** callcard-components
  - Declares: spring-boot-starter-data-jpa, spring-tx
  - Implements: DAOs, repositories
- **Server Layer:** CallCard_Server_WS
  - Declares: spring-boot-starter-data-jpa
  - Provides: DataSource, transaction management

### Caching
- **Component Layer:** callcard-components
  - Declares: caffeine
  - Uses: Caching strategies for DAOs
- **Server Layer:** CallCard_Server_WS
  - Declares: caffeine, ehcache, spring-boot-starter-cache
  - Enables: @Cacheable, @CacheEvict annotations

### Resilience & Circuit Breaking
- **Service Layer:** callcard-service
  - Declares: resilience4j-spring-boot2
  - Uses: Circuit breaker for external calls
- **Server Layer:** CallCard_Server_WS
  - Gets transitively: resilience4j-spring-boot2
  - Monitors: Circuit breaker metrics

---

## Packaging Types

| Module | Type | Packaging | Deployment | Execution |
|--------|------|-----------|-----------|-----------|
| callcard-entity | JAR | .jar | classpath | Library only |
| callcard-ws-api | JAR | .jar | classpath | Library only |
| callcard-components | JAR | .jar | classpath | Library only |
| callcard-service | JAR | .jar | classpath | Library only |
| CallCard_Server_WS | WAR | .war | Tomcat | Executable web app |

---

## Recommended Build Commands

### Full Build
```bash
cd C:\Users\dimit\tradetool_middleware
mvn clean package
```

### Production Build
```bash
mvn clean package -Ppmi-production-v3-1
```

### Staging Build
```bash
mvn clean package -Ppmi-staging-v3-1
```

### Development Build
```bash
mvn clean package -Pdev
```

### Skip Tests
```bash
mvn clean package -DskipTests
```

### Compile Only
```bash
mvn clean compile
```

### Dependency Tree
```bash
mvn dependency:tree
```

### Check for Updates
```bash
mvn versions:display-dependency-updates
```

---

## Verification Commands

### Test Compilation
```bash
mvn clean compile
```

### Run Unit Tests
```bash
mvn test
```

### Create WAR
```bash
mvn package -Ppmi-production-v3-1
```

### Deploy to Local Repository
```bash
mvn install
```

---

## Quick Troubleshooting

### Issue: Missing Dependency
**Solution:** Check that parent POM is in dependencyManagement

### Issue: Circular Dependency
**Solution:** Should never occur - dependency hierarchy is linear

### Issue: Version Conflict
**Solution:** All versions centralized in parent - check property values

### Issue: Test Failures
**Solution:** Check test scope declarations - test dependencies marked as <scope>test</scope>

### Issue: WAR Build Fails
**Solution:** Verify spring-boot-maven-plugin and maven-war-plugin configurations

---

## Useful References

- Spring Boot 2.7.18 docs: https://spring.io/projects/spring-boot
- Maven docs: https://maven.apache.org/
- Apache CXF 3.5.9: https://cxf.apache.org/
- Jersey 2.39.1: https://jersey.github.io/
- Hibernate 5.6.15: https://hibernate.org/

---

Generated: 2025-12-22
Status: All dependencies verified and correctly configured
