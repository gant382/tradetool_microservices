# CallCard Microservice Test Infrastructure Setup

## Overview
Test infrastructure has been successfully prepared for the CallCard microservice modules. Both `callcard-components` and `callcard-service` now have complete test configurations ready for development.

## Directory Structure Created

### callcard-components Module
```
callcard-components/
└── src/test/
    ├── java/
    │   └── com/saicon/games/callcard/components/
    │       ├── TestConfiguration.java
    │       └── impl/
    │           └── CallCardManagementTest.java
    └── resources/
        └── application-test.yml
```

### callcard-service Module
```
callcard-service/
└── src/test/
    ├── java/
    │   └── com/saicon/games/callcard/service/
    │       ├── TestConfiguration.java
    │       └── CallCardServiceIntegrationTest.java
    └── resources/
        └── application-test.yml
```

## Files Created

### 1. Test Configuration (application-test.yml)
- **Location**: `[module]/src/test/resources/application-test.yml`
- **Purpose**: Provides Spring Boot test configuration with H2 in-memory database
- **Key Settings**:
  - Uses H2 in-memory database for fast, isolated tests
  - Hibernate DDL auto set to `create-drop` for schema auto-generation
  - Logging configured for debugging (root: WARN, com.saicon: DEBUG)
  - SQL logging enabled for troubleshooting database operations

### 2. Test Classes

#### CallCardManagementTest.java (callcard-components)
- **Location**: `callcard-components/src/test/java/com/saicon/games/callcard/components/impl/CallCardManagementTest.java`
- **Purpose**: Unit tests for CallCard Management component
- **Features**:
  - Spring Boot test context loading verification
  - `@ActiveProfiles("test")` annotation for test profile activation
  - `@BeforeEach` setup method for test initialization
  - Placeholder tests ready for implementation

#### CallCardServiceIntegrationTest.java (callcard-service)
- **Location**: `callcard-service/src/test/java/com/saicon/games/callcard/service/CallCardServiceIntegrationTest.java`
- **Purpose**: Integration tests for CallCard Service
- **Features**:
  - Full Spring Boot context with database
  - `@Transactional` annotation for automatic rollback after each test
  - Database and service integration testing
  - Placeholder tests ready for implementation

### 3. Test Configuration Classes
- **TestConfiguration.java** (both modules)
- **Location**: `[module]/src/test/java/com/saicon/games/callcard/[module]/TestConfiguration.java`
- **Purpose**: Provides reusable test beans and configurations
- **Includes**: Spring `@TestConfiguration` for test-specific bean definitions

## Running Tests

### Run all tests in a module
```bash
cd callcard-components
mvn test

# or

cd callcard-service
mvn test
```

### Run specific test class
```bash
mvn test -Dtest=CallCardManagementTest
mvn test -Dtest=CallCardServiceIntegrationTest
```

### Run with coverage
```bash
mvn test jacoco:report
```

## Test Profile Activation

Tests automatically use the `test` profile which:
- Activates `application-test.yml` configuration
- Uses H2 in-memory database (no external database required)
- Configures debug-level logging for troubleshooting
- Enables SQL logging for query inspection

## Next Steps for Test Development

1. **Unit Tests (callcard-components)**
   - Add tests for CallCardManagement implementation
   - Add DAO layer tests
   - Add mapper/transformer tests
   - Add utility class tests

2. **Integration Tests (callcard-service)**
   - Add service layer integration tests
   - Add end-to-end workflow tests
   - Add error handling and exception scenarios
   - Add external service mocking (if needed)

3. **Additional Test Fixtures**
   - Create test data builders in `src/test/java`
   - Add test utilities for common operations
   - Create mock objects for external dependencies

## Dependencies Required

Ensure your pom.xml includes these test dependencies:
```xml
<!-- JUnit 5 (Jupiter) -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Boot Test Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- H2 Database (for in-memory testing) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

## Test Configuration Details

### H2 In-Memory Database
- **URL**: `jdbc:h2:mem:testdb`
- **Driver**: `org.h2.Driver`
- **Username**: `sa` (default)
- **Password**: (empty)
- **Behavior**:
  - Fresh database created for each test execution
  - Automatically cleaned up after tests complete
  - No persistence between test runs

### Hibernate Configuration
- **DDL Auto**: `create-drop` - schema created and dropped per test
- **Dialect**: H2Dialect
- **SQL Formatting**: Enabled for readable logs
- **SQL Comments**: Enabled for query traceability

### Logging Configuration
```yaml
root: WARN                          # Only warnings and errors
com.saicon: DEBUG                   # Debug logs for application code
org.hibernate.SQL: DEBUG            # SQL statements
org.hibernate.type: TRACE           # SQL parameter binding
```

## Best Practices

1. Keep tests independent and isolated
2. Use `@Transactional` to rollback after each test
3. Create test data in `@BeforeEach` methods
4. Mock external services and dependencies
5. Test both happy paths and error scenarios
6. Use descriptive test method names
7. Keep test methods focused and atomic

## Troubleshooting

### Tests fail to find application-test.yml
- Ensure file is in `src/test/resources/`
- Verify `@ActiveProfiles("test")` annotation is present
- Check that profile matches filename pattern

### Database connection issues
- H2 is in-memory, connection should always work
- If issues persist, check Hibernate configuration in application-test.yml
- Review Hibernate logs for schema creation errors

### Test context not loading
- Check for missing dependencies (spring-boot-starter-test, H2)
- Verify @SpringBootTest annotation is present
- Check application properties for typos

## Summary

The test infrastructure is now ready for development. Both modules have:
- ✓ Proper directory structure
- ✓ Test configuration with H2 database
- ✓ Base test classes ready for extension
- ✓ TestConfiguration classes for bean management
- ✓ Logging configured for debugging

Developers can now add actual test logic to the placeholder test methods and create additional tests as needed.
