# CallCard Microservice - Developer Test Checklist

## Pre-Development Setup

### Prerequisites
- [ ] Java 1.8+ installed
- [ ] Maven 3.6+ installed
- [ ] IDE configured (IntelliJ IDEA or Eclipse)
- [ ] Clone/update callcard modules from repository

### Dependency Verification
- [ ] Verify pom.xml includes JUnit 5 (Jupiter) dependency
- [ ] Verify spring-boot-starter-test is in dependencies
- [ ] Verify H2 database dependency with `<scope>test</scope>`
- [ ] Run `mvn clean install` to verify dependencies resolve

## Module Setup

### callcard-components Module
- [ ] Navigate to `callcard-components` directory
- [ ] Verify test directory structure exists:
  ```
  src/test/
  ├── java/com/saicon/games/callcard/components/
  │   ├── TestConfiguration.java
  │   └── impl/CallCardManagementTest.java
  └── resources/application-test.yml
  ```
- [ ] Open `src/test/resources/application-test.yml` and verify H2 config
- [ ] Open `CallCardManagementTest.java` and verify Spring annotations

### callcard-service Module
- [ ] Navigate to `callcard-service` directory
- [ ] Verify test directory structure exists:
  ```
  src/test/
  ├── java/com/saicon/games/callcard/service/
  │   ├── TestConfiguration.java
  │   └── CallCardServiceIntegrationTest.java
  └── resources/application-test.yml
  ```
- [ ] Open `src/test/resources/application-test.yml` and verify H2 config
- [ ] Open `CallCardServiceIntegrationTest.java` and verify Spring annotations

## Initial Test Run

### Execute Basic Tests
- [ ] In `callcard-components`: `mvn clean test`
- [ ] Verify tests pass (should pass placeholder tests)
- [ ] Check for any dependency resolution errors
- [ ] Review test execution output for warnings

- [ ] In `callcard-service`: `mvn clean test`
- [ ] Verify tests pass (should pass placeholder tests)
- [ ] Check for any dependency resolution errors
- [ ] Review test execution output for warnings

## Test Development

### callcard-components Unit Tests

#### CallCardManagementTest.java Development
- [ ] Open file in IDE
- [ ] Understand the placeholder test methods
- [ ] Identify what CallCardManagement does (review implementation)
- [ ] Plan test scenarios:
  - [ ] Happy path tests
  - [ ] Error/exception scenarios
  - [ ] Edge cases
  - [ ] Boundary conditions

#### Implementation Steps
1. [ ] Add @Autowired for CallCardManagement service
2. [ ] Create test data in @BeforeEach method
3. [ ] Replace `contextLoads()` with real management test
4. [ ] Replace `testCallCardManagementExists()` with operation test
5. [ ] Add additional test methods as needed
6. [ ] Run tests: `mvn test`
7. [ ] Fix any failing tests
8. [ ] Verify all tests pass

#### Additional Component Tests
- [ ] Create CallCardDAOTest.java for DAO layer
- [ ] Create CallCardMapperTest.java for data mapping
- [ ] Create CallCardUtilTest.java for utilities
- [ ] Add integration tests for component interactions

### callcard-service Integration Tests

#### CallCardServiceIntegrationTest.java Development
- [ ] Open file in IDE
- [ ] Understand the placeholder test methods
- [ ] Identify what CallCardService does (review implementation)
- [ ] Plan test scenarios:
  - [ ] Service method execution
  - [ ] Database persistence
  - [ ] Transaction boundaries
  - [ ] Error handling
  - [ ] Service dependencies

#### Implementation Steps
1. [ ] Add @Autowired for CallCardService and dependencies
2. [ ] Initialize test data in @BeforeEach method
3. [ ] Replace `contextLoads()` with real service test
4. [ ] Replace `testCallCardServiceExists()` with operation test
5. [ ] Add additional test methods as needed
6. [ ] Use @Transactional for automatic rollback
7. [ ] Run tests: `mvn test`
8. [ ] Fix any failing tests
9. [ ] Verify all tests pass

#### Additional Service Tests
- [ ] Create CallCardServiceErrorTest.java for error scenarios
- [ ] Create CallCardServiceWorkflowTest.java for workflows
- [ ] Create CallCardServicePerformanceTest.java for performance
- [ ] Add mocks for external dependencies

## Testing Best Practices

### Naming Conventions
- [ ] Use `test<Method><Scenario>` naming pattern
- [ ] Examples:
  - [ ] `testCreateCallCard_WithValidData_Success`
  - [ ] `testCreateCallCard_WithInvalidData_ThrowsException`
  - [ ] `testGetCallCardById_WhenExists_ReturnsCard`
  - [ ] `testGetCallCardById_WhenNotExists_ReturnsNull`

### Test Structure
- [ ] Arrange - Set up test data and mocks
- [ ] Act - Execute the method being tested
- [ ] Assert - Verify expected results

Example:
```java
@Test
public void testCreateCallCard_WithValidData_Success() {
    // Arrange
    CallCardDTO dto = new CallCardDTO(/* valid data */);

    // Act
    CallCard result = service.create(dto);

    // Assert
    assertNotNull(result);
    assertEquals(dto.getName(), result.getName());
}
```

### Assertion Guidelines
- [ ] Use meaningful assertion messages
- [ ] Test both positive and negative cases
- [ ] Verify state changes are persisted
- [ ] Check boundary conditions
- [ ] Validate exception messages

### Mock and Stub
- [ ] Use @Mock for external dependencies
- [ ] Use @InjectMocks for class under test
- [ ] Configure mocks in @BeforeEach or @BeforeAll
- [ ] Verify mock interactions when needed

## Code Coverage

### Coverage Goals
- [ ] Aim for >80% overall coverage
- [ ] Prioritize critical business logic
- [ ] Cover error paths and exceptions
- [ ] Include edge cases and boundary conditions

### Coverage Measurement
- [ ] Run: `mvn clean test jacoco:report`
- [ ] Open: `target/site/jacoco/index.html`
- [ ] Review coverage by class
- [ ] Identify gaps and add tests

### Coverage Exclusions
- [ ] Auto-generated code (may skip)
- [ ] Getters/setters (may skip)
- [ ] Configuration classes (low priority)
- [ ] Third-party framework code (not applicable)

## Test Data Management

### Test Data Strategies
- [ ] Use builders for complex objects:
  ```java
  CallCardDTO.builder()
      .name("Test Card")
      .amount(100.0)
      .build();
  ```
- [ ] Create test fixtures for common scenarios
- [ ] Use constants for standard test values
- [ ] Generate unique test data when needed

### Database Test Isolation
- [ ] Tests use fresh H2 database each run
- [ ] @Transactional automatically rolls back
- [ ] No test data persists between runs
- [ ] Tests are independent and isolated

## Continuous Integration

### Before Committing
- [ ] Run full test suite: `mvn clean test`
- [ ] Verify all tests pass
- [ ] Check code coverage hasn't decreased
- [ ] Review test output for warnings

### Build Pipeline Readiness
- [ ] All tests pass locally
- [ ] Coverage meets minimum threshold
- [ ] No compilation errors or warnings
- [ ] Code follows project standards

## Documentation

### Test Documentation
- [ ] Add JavaDoc comments to test classes
- [ ] Document complex test logic
- [ ] Explain non-obvious assertions
- [ ] Link to related requirements/issues

Example:
```java
/**
 * Tests that creating a callcard with valid data succeeds.
 * Verifies that:
 * - The callcard is saved to database
 * - The returned object has correct attributes
 * - The ID is auto-generated
 */
@Test
public void testCreateCallCard_WithValidData_Success() {
    // ...
}
```

## Troubleshooting

### Test Compilation Issues
- [ ] Check for import statement errors
- [ ] Verify class names match file names
- [ ] Check package structure
- [ ] Run: `mvn clean compile`

### Test Execution Issues
- [ ] Check H2 database configuration
- [ ] Verify @ActiveProfiles("test") present
- [ ] Check application-test.yml syntax
- [ ] Review Hibernate schema creation logs

### Assertion Failures
- [ ] Review expected vs actual values
- [ ] Check test data setup
- [ ] Verify mock configurations
- [ ] Review business logic implementation

### Dependency Injection Issues
- [ ] Ensure @Autowired fields are present
- [ ] Verify beans are properly configured
- [ ] Check @SpringBootTest annotation
- [ ] Review TestConfiguration.java

## Performance Considerations

### Test Optimization
- [ ] Keep tests fast (target <100ms per test)
- [ ] Use H2 in-memory database (already configured)
- [ ] Avoid unnecessary database operations
- [ ] Mock external services
- [ ] Run slow tests separately if needed

### Slow Test Identification
- [ ] Run: `mvn test -X` for verbose output
- [ ] Identify tests taking >1 second
- [ ] Optimize database queries
- [ ] Remove unnecessary setups
- [ ] Consider async testing for I/O operations

## Final Verification

### Pre-Release Checklist
- [ ] All tests pass locally
- [ ] Coverage meets threshold
- [ ] No TODOs in test files
- [ ] Documentation is complete
- [ ] Code review completed
- [ ] CI/CD pipeline passes
- [ ] Performance is acceptable
- [ ] Error messages are clear

## Resources

### Documentation Files
- `TEST_INFRASTRUCTURE_SETUP.md` - Complete setup guide
- `CALLCARD_TEST_INFRASTRUCTURE_SUMMARY.txt` - Quick summary
- `CLAUDE.md` - Project standards and guidelines

### External Resources
- JUnit 5 Documentation: https://junit.org/junit5/
- Spring Boot Testing: https://spring.io/guides/gs/testing-web/
- Mockito Documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- H2 Database: https://www.h2database.com/

## Approval Sign-Off

Once all items are complete:

- [ ] All tests implemented and passing
- [ ] Code coverage acceptable (>80%)
- [ ] Code reviewed and approved
- [ ] Ready for merge/release

Completed Date: ______________
Developer Name: ______________
Reviewer Name: ______________
