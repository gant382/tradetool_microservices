# CallCard Microservice Integration Test Suite

Comprehensive integration test suite for the CallCard microservice (callcard-ws-api). This test suite covers REST API endpoints, SOAP web services, and business logic validation.

## Overview

The test suite is organized into multiple test classes, each focusing on different aspects of the CallCard microservice:

### Test Classes

#### 1. **CallCardIntegrationTest**
- **Purpose**: Tests REST API endpoints exposed by the microservice
- **Scope**: HTTP request/response handling, header validation, request parameter validation
- **Key Tests**:
  - Health check endpoint
  - GET /callcard/template/{userId} - Retrieve empty CallCards
  - GET /callcard/pending/{userId} - Retrieve pending CallCard
  - GET /callcard/{userId} - Get new or pending CallCard
  - GET /callcard/{userId}/callcard - List pending CallCards
  - GET /callcard/{userId}/statistics/{propertyId} - Get statistics
  - POST /callcard/update/{userId} - Add single CallCard record
  - POST /callcard/update/{userId}/multiple - Add multiple CallCard records
  - POST /callcard/transactions/{userId} - Submit transactions
  - POST /callcard/update/simplified/{userId} - Add simplified CallCard
  - GET /callcard/list/simplified - List simplified CallCards
  - Input validation tests (invalid UUIDs, missing parameters)

#### 2. **CallCardServiceIntegrationTest**
- **Purpose**: Tests SOAP web service layer (business logic)
- **Scope**: Service method execution, response handling, error scenarios
- **Key Tests**:
  - `addCallCardRecords()` - Add new call card records
  - `addOrUpdateSimplifiedCallCard()` - Create/update simplified cards
  - `getCallCardsFromTemplate()` - Retrieve template-based cards
  - `getPendingCallCard()` - Get pending records
  - `getNewOrPendingCallCard()` - Retrieve or create new cards
  - `listPendingCallCard()` - List all pending records
  - `submitTransactions()` - Process transaction submission
  - `getCallCardStatistics()` - Calculate statistics
  - `listSimplifiedCallCards()` - Retrieve simplified records

#### 3. **CallCardResourcesIntegrationTest**
- **Purpose**: Tests REST resource layer (JAX-RS endpoints)
- **Scope**: Resource method invocation, request validation, response building
- **Key Tests**:
  - Template-based CallCard retrieval
  - Pending CallCard retrieval
  - New or pending CallCard retrieval
  - Automatic filter property application (Egypt/Senegal game types)
  - Pending CallCard listing
  - Statistics retrieval
  - Single and multiple CallCard record addition
  - Transaction submission
  - Simplified CallCard operations
  - Input validation (invalid UUID, null parameters)

#### 4. **CallCardValidationIntegrationTest**
- **Purpose**: Tests data validation and business rule enforcement
- **Scope**: DTO validation, date range validation, UUID validation, test data factory
- **Key Tests**:
  - CallCardDTO validation
  - SimplifiedCallCardDTO validation
  - Date range validation (end date after start date)
  - UUID format validation
  - CallCard group count validation
  - List validation (multiple records)
  - Test data factory functionality
  - Date calculation utilities

## Test Data

### Test Data Fixtures

#### JSON Fixtures (`src/test/resources/fixtures/callcard-test-data.json`)

Provides pre-defined test data including:

- **CallCards**: Sample call card records with various states (submitted/pending)
- **SimplifiedCallCards**: Simplified card representations
- **Statistics**: Sample statistical data
- **Users**: Test user accounts
- **GameTypes**: Game type definitions (Egypt, Senegal)
- **Templates**: CallCard template definitions
- **Test Scenarios**: Pre-configured test scenarios (valid, invalid, missing parameters)

#### Test Data Factory (`CallCardTestDataFactory`)

Utility class for programmatic test data generation:

```java
// Create single CallCard
CallCardDTO card = CallCardTestDataFactory.createCallCard();

// Create multiple CallCards
List<CallCardDTO> cards = CallCardTestDataFactory.createCallCards(5);

// Create with specific parameters
CallCardDTO card = CallCardTestDataFactory.createCallCard(
    callCardId, startDate, endDate
);

// Create simplified CallCard
SimplifiedCallCardDTO simplified = CallCardTestDataFactory.createSimplifiedCallCard();

// Utility constants
String userId = CallCardTestDataFactory.UUIDs.VALID_USER_ID;
Date yesterday = CallCardTestDataFactory.Dates.yesterday();
List<Integer> types = CallCardTestDataFactory.Parameters.commonStatisticsTypes();
```

### Available Test Data

#### Valid UUIDs (Pre-configured)
```
VALID_USER_ID = "550e8400-e29b-41d4-a716-446655440000"
VALID_USER_GROUP_ID = "550e8400-e29b-41d4-a716-446655440001"
VALID_APPLICATION_ID = "550e8400-e29b-41d4-a716-446655440002"
VALID_GAME_TYPE_ID = "550e8400-e29b-41d4-a716-446655440003"
VALID_SESSION_ID = "550e8400-e29b-41d4-a716-446655440004"
```

#### Date Utilities
```
CallCardTestDataFactory.Dates.now()
CallCardTestDataFactory.Dates.tomorrow()
CallCardTestDataFactory.Dates.yesterday()
CallCardTestDataFactory.Dates.daysFromNow(int days)
CallCardTestDataFactory.Dates.daysAgo(int days)
CallCardTestDataFactory.Dates.thirtyDaysAgo()
CallCardTestDataFactory.Dates.sevenDaysAgo()
```

## Test Configuration

### Test Profile: `test`

Application configuration for tests is defined in `application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb  # Embedded H2 database
    driver-class-name: org.h2.Driver
  jpa:
    hibernate.ddl-auto: create-drop
  logging:
    level:
      com.saicon.games: DEBUG
```

### TestApplicationConfiguration

Spring Boot test configuration providing:
- ObjectMapper bean with ISO-8601 date formatting
- Component scanning for CallCard packages
- H2 embedded database setup
- JPA/Hibernate configuration

## Running the Tests

### Run All Tests
```bash
cd callcard-ws-api
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=CallCardIntegrationTest
mvn test -Dtest=CallCardServiceIntegrationTest
mvn test -Dtest=CallCardResourcesIntegrationTest
mvn test -Dtest=CallCardValidationIntegrationTest
```

### Run Tests Matching Pattern
```bash
mvn test -Dtest=CallCard*
```

### Run Single Test Method
```bash
mvn test -Dtest=CallCardIntegrationTest#testGetCallCardsFromTemplate
```

### Run Tests with Coverage
```bash
mvn clean test jacoco:report
# Coverage report: target/site/jacoco/index.html
```

### Run Tests with Logging
```bash
mvn test -X
```

### Run Tests in CI/CD
```bash
mvn clean verify  # Includes compilation, tests, and code quality checks
```

## Test Assertions

Custom assertion utilities available in `TestAssertions` class:

### CallCard Assertions
```java
TestAssertions.assertValidCallCard(callCard);           // Validate entire DTO
TestAssertions.assertValidSimplifiedCallCard(card);     // Validate simplified DTO
TestAssertions.assertValidCallCardList(cards);          // Validate list of cards
TestAssertions.assertCallCardGroupCount(card, 2);       // Verify group count
TestAssertions.assertCallCardTemplate(card, templateId); // Verify template
TestAssertions.assertHasCallCardGroups(card);           // Check groups exist
TestAssertions.assertCallCardsEqual(expected, actual);  // Compare cards
```

### Response Assertions
```java
TestAssertions.assertSuccessResponse(response);         // Check OK status
TestAssertions.assertErrorResponse(response);           // Check ERROR status
TestAssertions.assertHasErrorCode(response, code);      // Verify error code
TestAssertions.assertHasResultMessage(response, msg);   // Verify message
```

### HTTP Assertions
```java
TestAssertions.assertHttpSuccess(response);             // 2xx status
TestAssertions.assertHttpClientError(response);         // 4xx status
TestAssertions.assertHttpServerError(response);         // 5xx status
TestAssertions.assertHttpStatus(response, HttpStatus.OK); // Specific status
```

### UUID and Date Assertions
```java
TestAssertions.assertValidUUID(uuid);                   // UUID format
TestAssertions.assertInvalidUUID(uuid);                 // Invalid UUID
TestAssertions.assertValidDateRange(start, end);        // Date ordering
```

## Test Scenarios

### Happy Path Scenarios

1. **Template Retrieval**: User retrieves CallCards from template
   - Validates required headers (userGroupId, applicationId)
   - Returns CallCard list or 204 No Content

2. **Pending CallCard**: User retrieves pending CallCard
   - Returns existing pending card or creates new one
   - Validates date range

3. **Statistics**: Retrieve usage statistics
   - Filters by user, property, type, date range
   - Returns aggregated statistics

4. **Transaction Submission**: Submit CallCard transactions
   - Validates session and authorization
   - Updates call card status
   - Records transaction history

### Error Scenarios

1. **Invalid User ID**: Non-UUID format string
   - Expected: 400 Bad Request
   - Error message: Validation failure

2. **Missing Required Headers**: Missing userGroupId or applicationId
   - Expected: 400 Bad Request
   - Error message: Header validation failure

3. **Invalid Session**: Non-existent or expired session ID
   - Expected: 401 Unauthorized
   - Error message: Session validation failure

4. **Invalid Date Range**: End date before start date
   - Expected: 400 Bad Request
   - Error message: Date range validation failure

5. **Unauthorized Access**: User accessing other user's CallCards
   - Expected: 403 Forbidden
   - Error message: Authorization failure

## Test Coverage

### API Endpoints Covered
- 11 REST endpoints (GET, POST)
- 8 SOAP service methods
- 10 JAX-RS resource methods

### Validation Coverage
- Input parameter validation (UUIDs, dates, ranges)
- Header validation (required/optional)
- Response status validation (success/error)
- DTO field validation

### Business Logic Coverage
- CallCard creation and retrieval
- Template-based card generation
- Pending card management
- Transaction submission
- Statistics calculation
- Simplified card operations

## Dependencies

### Test Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

## Troubleshooting

### Tests Failing with ClassNotFoundException
- Ensure all dependencies are in pom.xml
- Run `mvn clean install` to rebuild

### Tests Failing with Database Errors
- H2 embedded database is created on-the-fly
- Check database configuration in application-test.yml
- Verify JPA entity mappings

### Tests Failing with Spring Context Issues
- Check component scan packages in TestApplicationConfiguration
- Ensure test beans are properly defined
- Verify @SpringBootTest annotations

### Tests Timing Out
- Increase timeout in test configuration
- Check for blocking operations in test code
- Verify external service mocks are working

## Best Practices

### Test Organization
1. **Arrange-Act-Assert Pattern**: Structure each test method
2. **Descriptive Names**: Use @DisplayName for clear test purpose
3. **Single Assertion**: Test one behavior per method
4. **Setup/Teardown**: Use @BeforeEach for test initialization

### Test Data
1. **Factory Pattern**: Use CallCardTestDataFactory for consistent data
2. **Fixture Files**: Load complex data from JSON fixtures
3. **Builder Pattern**: Create custom builders for complex objects
4. **Random Data**: Generate UUIDs for isolation

### Assertions
1. **Custom Assertions**: Use TestAssertions for domain-specific checks
2. **Meaningful Messages**: Include context in assertion messages
3. **Null Checks**: Always verify objects before assertions
4. **Type Safety**: Use proper assertion methods for data types

### Test Isolation
1. **Independent Tests**: No test should depend on another
2. **Random Port**: Use random port in @SpringBootTest
3. **Embedded Database**: Use H2 for test database
4. **Mock External Services**: Mock calls to external systems

## Continuous Integration

### Maven Build Profile
```bash
mvn clean verify -Ptest
```

### GitHub Actions Example
```yaml
- name: Run Tests
  run: mvn clean test

- name: Code Coverage
  run: mvn jacoco:report
```

## Contributing

When adding new tests:

1. Follow naming convention: `CallCard[Feature]IntegrationTest`
2. Add @DisplayName for test method
3. Use TestAssertions for custom assertions
4. Add test data to CallCardTestDataFactory
5. Document complex test scenarios
6. Ensure tests pass locally before committing

## License

See parent project LICENSE file.

## Support

For issues or questions:
- Check test documentation comments
- Review existing test implementations
- Consult CallCard API documentation
- Contact development team
