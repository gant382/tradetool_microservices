# CallCard Microservice - Quick Test Reference

## Quick Commands

### Run Tests

```bash
# Run all tests
mvn clean test

# Run tests in callcard-components only
cd callcard-components
mvn test

# Run tests in callcard-service only
cd callcard-service
mvn test

# Run specific test class
mvn test -Dtest=CallCardManagementTest

# Run specific test method
mvn test -Dtest=CallCardManagementTest#contextLoads

# Skip tests during build
mvn clean install -DskipTests

# Run tests with coverage
mvn clean test jacoco:report

# Run tests with verbose output
mvn test -X
```

## Directory Paths

### callcard-components
```
Location: C:\Users\dimit\tradetool_middleware\callcard-components
Test Source: src/test/java/com/saicon/games/callcard/components/
Test Resources: src/test/resources/

Key Files:
- src/test/java/.../impl/CallCardManagementTest.java
- src/test/java/.../TestConfiguration.java
- src/test/resources/application-test.yml
```

### callcard-service
```
Location: C:\Users\dimit\tradetool_middleware\callcard-service
Test Source: src/test/java/com/saicon/games/callcard/service/
Test Resources: src/test/resources/

Key Files:
- src/test/java/.../CallCardServiceIntegrationTest.java
- src/test/java/.../TestConfiguration.java
- src/test/resources/application-test.yml
```

## File Locations (Absolute Paths)

```
Test Files Created:
C:\Users\dimit\tradetool_middleware\callcard-components\src\test\java\com\saicon\games\callcard\components\TestConfiguration.java
C:\Users\dimit\tradetool_middleware\callcard-components\src\test\java\com\saicon\games\callcard\components\impl\CallCardManagementTest.java
C:\Users\dimit\tradetool_middleware\callcard-components\src\test\resources\application-test.yml

C:\Users\dimit\tradetool_middleware\callcard-service\src\test\java\com\saicon\games\callcard\service\TestConfiguration.java
C:\Users\dimit\tradetool_middleware\callcard-service\src\test\java\com\saicon\games\callcard\service\CallCardServiceIntegrationTest.java
C:\Users\dimit\tradetool_middleware\callcard-service\src\test\resources\application-test.yml

Documentation:
C:\Users\dimit\tradetool_middleware\TEST_INFRASTRUCTURE_SETUP.md
C:\Users\dimit\tradetool_middleware\CALLCARD_TEST_INFRASTRUCTURE_SUMMARY.txt
C:\Users\dimit\tradetool_middleware\DEVELOPER_TEST_CHECKLIST.md
C:\Users\dimit\tradetool_middleware\QUICK_TEST_REFERENCE.md (this file)
```

## Test Annotations Quick Reference

```java
// Mark class as test
@SpringBootTest
public class MyTest {

    // Activate test profile
    @ActiveProfiles("test")

    // Setup before each test
    @BeforeEach
    public void setUp() { }

    // Setup once before all tests
    @BeforeAll
    public static void setUpAll() { }

    // Cleanup after each test
    @AfterEach
    public void tearDown() { }

    // Mark method as test
    @Test
    public void testSomething() { }

    // Inject Spring bean
    @Autowired
    private SomeService service;

    // Mock object
    @Mock
    private SomeDependency dependency;

    // Inject mocks into class
    @InjectMocks
    private ClassUnderTest classUnderTest;

    // Auto-rollback transaction
    @Transactional
}
```

## Common Test Patterns

### Basic Unit Test
```java
@SpringBootTest
@ActiveProfiles("test")
public class ComponentTest {

    @Autowired
    private MyComponent component;

    @Test
    public void testComponent() {
        assertNotNull(component);
    }
}
```

### Integration Test with Database
```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ServiceTest {

    @Autowired
    private MyService service;

    @Autowired
    private MyRepository repository;

    @Test
    public void testServicePersistence() {
        MyEntity entity = service.create(data);
        assertNotNull(entity.getId());
    }
}
```

### Test with Mocks
```java
@SpringBootTest
@ActiveProfiles("test")
public class MockTest {

    @Mock
    private ExternalService externalService;

    @InjectMocks
    private MyService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testWithMock() {
        when(externalService.call()).thenReturn(expected);
        // Test code
    }
}
```

## H2 Database Configuration

**Connection Details for Tests:**
```
URL: jdbc:h2:mem:testdb
Driver: org.h2.Driver
Username: sa
Password: (empty)
```

**Schema Handling:**
- `ddl-auto: create-drop` - Creates schema before test, drops after
- Fresh database for each test run
- No data persists between tests

**Accessing Test Database (if needed):**
```bash
# In application-test.yml, add web console
spring:
  h2:
    console:
      enabled: true
```

Then access: http://localhost:8080/h2-console

## Test Configuration File (application-test.yml)

Located in: `src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
  test:
    database:
      replace: none

logging:
  level:
    root: WARN
    com.saicon: DEBUG
```

## Troubleshooting Commands

```bash
# Verify dependencies
mvn dependency:tree

# Check test compilation
mvn clean test-compile

# Run with debug output
mvn test -X

# Run specific module
cd callcard-components && mvn test

# Clean and rebuild
mvn clean install

# Generate coverage report
mvn jacoco:report

# View coverage (after running jacoco:report)
# Open: target/site/jacoco/index.html
```

## IDE Integration

### IntelliJ IDEA
1. Right-click test file → Run
2. Right-click test class → Run all tests
3. View → Tool Windows → Test Results

### Eclipse
1. Right-click test file → Run As → JUnit Test
2. View → Show View → JUnit

### VS Code with Java Extension
1. Test Explorer plugin recommended
2. Click on test gutter icons to run tests
3. View test results in Test Explorer panel

## Git Workflow for Tests

```bash
# Before committing
mvn clean test

# Verify coverage
mvn jacoco:report

# Commit with message
git add .
git commit -m "Add tests for: [feature]"

# Push to branch
git push origin feature/your-feature
```

## Performance Benchmarks

```
Expected Test Execution Times:
- Context loading: 2-5 seconds (first test only)
- Unit test: <100ms each
- Integration test: <500ms each
- Full suite (callcard-components): ~30 seconds
- Full suite (callcard-service): ~30 seconds
- Full coverage report: ~1-2 minutes
```

## Debugging Tips

### View SQL Statements
```yaml
# In application-test.yml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Enable Spring Debug Logging
```yaml
logging:
  level:
    org.springframework: DEBUG
    org.springframework.boot: DEBUG
```

### Inspect Test Database
```yaml
# Add to application-test.yml
spring:
  h2:
    console:
      enabled: true
```
Access: http://localhost:8080/h2-console during test run

### Print Debug Info
```java
@Test
public void testDebug() {
    System.out.println("Debug: " + variable);
    log.debug("Debug message: {}", value);
}
```

## Expected Output

### Successful Test Run
```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.saicon.games.callcard.components.impl.CallCardManagementTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.123 s
-------------------------------------------------------
Results :
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

### Test Failure
```
[ERROR] testCreateCallCard_WithInvalidData_ThrowsException
Expected exception but none thrown
```

## Next Actions

1. Run all tests: `mvn clean test`
2. Verify they pass
3. Add real test logic to placeholder tests
4. Run tests again: `mvn test`
5. Generate coverage: `mvn jacoco:report`
6. Review coverage report: `open target/site/jacoco/index.html`
7. Add more tests until coverage threshold met
8. Commit and push

## Documentation Links

- **Full Setup Guide**: TEST_INFRASTRUCTURE_SETUP.md
- **Infrastructure Summary**: CALLCARD_TEST_INFRASTRUCTURE_SUMMARY.txt
- **Developer Checklist**: DEVELOPER_TEST_CHECKLIST.md
- **Project Standards**: CLAUDE.md
- **This Reference**: QUICK_TEST_REFERENCE.md

## Support

For issues or questions:
1. Check DEVELOPER_TEST_CHECKLIST.md troubleshooting section
2. Review TEST_INFRASTRUCTURE_SETUP.md for detailed configuration
3. Check project CLAUDE.md for standards
4. Review IDE debugging tools
5. Check Maven debug output: `mvn -X test`
