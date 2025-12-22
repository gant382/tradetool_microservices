# CallCard Microservice - Code Review Checklist

**Project:** CallCard Microservice
**Location:** `C:\Users\dimit\tradetool_middleware\`
**Technology Stack:** Java 1.8+, Spring Boot 2.7.x, Apache CXF 3.5.x, Hibernate 5.6.x
**Last Updated:** 2025-12-22

---

## 1. Code Quality

### 1.1 Naming Conventions
- [ ] Class names follow PascalCase (e.g., `CallCardService`, `GenericDAO`)
- [ ] Method names follow camelCase (e.g., `getCallCardsFromTemplate`)
- [ ] Variable names are descriptive and avoid single letters (except for loop counters: `i`, `j`)
- [ ] Constants use UPPER_SNAKE_CASE (e.g., `SESSION_HEADER_NAME`, `LOGGER`)
- [ ] Package names follow reverse domain convention (e.g., `com.saicon.games.callcard`)

### 1.2 Code Organization
- [ ] Classes are focused on single responsibility (SRP)
- [ ] Methods have clear, single purposes
- [ ] Related functionality is grouped logically
- [ ] Imports are organized and clean (no unused imports)
- [ ] Classes are not unnecessarily long (max ~500 lines recommended)

### 1.3 Code Style
- [ ] Consistent indentation (4 spaces, no tabs)
- [ ] Line length under 120 characters
- [ ] No trailing whitespace
- [ ] Proper spacing around operators and method parameters
- [ ] Consistent brace placement (Java style: same line)

### 1.4 Documentation
- [ ] Classes have JavaDoc comments describing purpose
- [ ] Public methods have JavaDoc with `@param`, `@return`, `@throws` tags
- [ ] Complex logic has inline comments explaining "why", not "what"
- [ ] TODO comments reference JIRA tickets or issues
- [ ] Deprecated methods are marked with `@Deprecated` annotation

---

## 2. Exception Handling

### 2.1 Custom Exception Usage
- [ ] `BusinessLayerException` is used for business logic errors (not `RuntimeException`)
- [ ] `ExceptionTypeTO` enums are properly defined with error codes
- [ ] Exception constructors initialize `errorCode` and `exceptionType`
- [ ] Checked exceptions are properly declared in method signatures

### 2.2 Exception Catching
- [ ] Specific exceptions are caught (not bare `Exception`)
- [ ] Exception messages are logged with appropriate log level (WARN/ERROR)
- [ ] Stack traces are logged for unexpected errors
- [ ] Exceptions contain context (user ID, operation, parameters)
- [ ] No silent exception swallowing (empty catch blocks)

### 2.3 Exception Propagation
- [ ] Business exceptions bubble up to service layer
- [ ] Service layer wraps with response objects (`ResponseListCallCard`, `WSResponse`)
- [ ] Error codes are consistent across layers
- [ ] HTTP status codes match error severity

### 2.4 Exception Response Format
- [ ] All error responses include error code
- [ ] Error messages are user-friendly (no internal stack traces)
- [ ] Response objects use consistent structure:
  ```java
  new ResponseListCallCard(errorCode, message, ResponseStatus.ERROR, null, 0)
  ```
- [ ] Status enum is used (`ResponseStatus.OK`, `ResponseStatus.ERROR`)

---

## 3. Null Safety

### 3.1 Null Checks
- [ ] Input parameters are validated for null at method entry
- [ ] Database query results are checked before use
- [ ] Collections are checked for null and size > 0 before iteration
- [ ] Optional is used for methods that may return null (Java 8+)
- [ ] NullPointerException is prevented with proper null checks

### 3.2 List/Collection Handling
- [ ] Empty lists are returned instead of null
  ```java
  return new ArrayList<>();  // Not: return null;
  ```
- [ ] Size checks precede iteration:
  ```java
  if (list != null && list.size() > 0) { ... }
  ```
- [ ] `.stream()` includes null checks for safety
- [ ] Generic types are preserved in collections

### 3.3 DAO/Entity Handling
- [ ] Entity methods check for null references
- [ ] Lazy-loaded collections are accessed within transaction context
- [ ] Join fetches are used to prevent N+1 queries
- [ ] `getReference()` is preferred over `read()` for FK lookups

### 3.4 Assert Utility
- [ ] `Assert.notNull()` validates required parameters
- [ ] Custom `Assert` class provides clear error messages
- [ ] Assertions fail fast with meaningful messages

---

## 4. Performance Considerations

### 4.1 Database Queries
- [ ] Named queries are parameterized (no string concatenation)
- [ ] Pagination is used for large result sets (`setFirstResult()`, `setMaxResults()`)
- [ ] Database indices are considered for frequently queried columns
- [ ] N+1 query problems are avoided with proper joins
- [ ] Unnecessary queries are eliminated (cache frequently accessed data)

### 4.2 Caching Strategy
- [ ] Cache configuration is defined (Caffeine, EhCache)
- [ ] Cache eviction strategy matches data freshness requirements
- [ ] `@Cacheable` annotations are properly scoped
- [ ] Cache keys are unique and collision-proof
- [ ] Cache warming strategies are implemented for critical data

### 4.3 Connection Management
- [ ] Database connection pooling is configured (recommended: 20-100 connections)
- [ ] Connections are released properly (no resource leaks)
- [ ] Connection timeouts are set appropriately
- [ ] DataSource is configured for the database type (SQL Server)

### 4.4 Entity Manager
- [ ] EntityManager is injected, not created manually
- [ ] Flush/clear operations are minimized
- [ ] Batch processing is used for bulk operations
- [ ] Read-only queries use `readOnly` flag in transaction configuration

### 4.5 REST/SOAP Performance
- [ ] Response payload size is reasonable (compress large responses)
- [ ] Lazy loading is configured appropriately
- [ ] DTOs are minimal (only necessary fields)
- [ ] Pagination parameters are enforced (max limit = 1000)

---

## 5. Security Best Practices

### 5.1 Authentication & Authorization
- [ ] Session authentication is implemented (SessionAuthenticationInterceptor)
- [ ] Session tokens are validated before processing requests
- [ ] User ID is extracted and authenticated from headers
- [ ] SOAP headers include security tokens
- [ ] IGameInternalService is called for session validation (not hardcoded)

### 5.2 Input Validation
- [ ] All input parameters are validated
- [ ] String inputs are checked for length limits
- [ ] Date parameters are validated for reasonable ranges
- [ ] Numeric parameters check for valid ranges
- [ ] Collection size limits are enforced

### 5.3 SQL Injection Prevention
- [ ] Named queries are used for parameterized queries
- [ ] No string concatenation in HQL/JPQL
- [ ] Native queries use `setParameter()` for all values
- [ ] User-provided input never goes directly into queries

### 5.4 SOAP/REST Security
- [ ] HTTPS is enforced in production (`https://` URLs)
- [ ] CORS headers are properly configured
- [ ] Content-Type validation is enforced
- [ ] Request size limits are configured
- [ ] Rate limiting is implemented for API endpoints

### 5.5 Data Security
- [ ] Sensitive data (passwords, tokens) is never logged
- [ ] User data is filtered per organization (multi-tenant isolation)
- [ ] DateTime values use UTC/timezone-aware handling
- [ ] Encryption is used for sensitive stored data
- [ ] PII is masked in logs

### 5.6 Configuration Security
- [ ] Credentials are not hardcoded in source code
- [ ] `application.properties` uses environment variables for secrets
- [ ] Configuration files have restricted file permissions (600)
- [ ] No default credentials are left in code

---

## 6. Test Coverage

### 6.1 Unit Tests
- [ ] Service layer methods have unit tests
- [ ] DAO methods have tests with mock EntityManager
- [ ] Exception scenarios are tested
- [ ] Edge cases are covered (empty lists, null values, boundaries)
- [ ] Test class names follow pattern: `[ClassUnderTest]Test`

### 6.2 Integration Tests
- [ ] Database integration tests use `@DataJpaTest`
- [ ] Full Spring context tests use `@SpringBootTest`
- [ ] Service-to-service communication is tested
- [ ] Transaction boundaries are tested
- [ ] Multi-tenant filtering is verified

### 6.3 Test Data
- [ ] Test fixtures are properly set up and torn down
- [ ] Database is reset between tests
- [ ] Test data matches production scenarios
- [ ] Sensitive data in tests is minimal

### 6.4 Code Coverage
- [ ] Target coverage >= 80% for critical paths
- [ ] Coverage includes exception handlers
- [ ] Coverage includes conditional branches
- [ ] Coverage reports are generated with CI/CD

### 6.5 SOAP/REST Endpoint Tests
- [ ] Endpoint tests verify request/response format
- [ ] Error responses are tested
- [ ] Authentication failures are tested
- [ ] Payload validation is tested

---

## 7. Documentation

### 7.1 Code Documentation
- [ ] Class-level JavaDoc explains purpose and responsibilities
- [ ] Method JavaDoc includes:
  - [ ] Description of what the method does
  - [ ] `@param` tags for all parameters
  - [ ] `@return` tag describing return value
  - [ ] `@throws` tags for checked exceptions
- [ ] Complex algorithms have inline comments

### 7.2 API Documentation
- [ ] SOAP services have WSDL descriptions
- [ ] REST endpoints have Swagger/OpenAPI documentation
- [ ] Request/response examples are provided
- [ ] Error codes are documented with descriptions
- [ ] Authentication requirements are documented

### 7.3 Configuration Documentation
- [ ] `application.properties` has comments for all settings
- [ ] Environment-specific configurations are explained
- [ ] Database connection pooling settings are documented
- [ ] Cache configuration is documented
- [ ] Spring profiles are documented

### 7.4 Architectural Documentation
- [ ] Module dependency diagram exists
- [ ] Data flow diagrams show request processing
- [ ] Database schema documentation exists
- [ ] Entity relationships are documented
- [ ] Integration points are documented

---

## 8. Logging

### 8.1 Logging Configuration
- [ ] Logger is defined as static final:
  ```java
  private static final Logger LOGGER = LoggerFactory.getLogger(ClassName.class);
  ```
- [ ] Logback configuration exists for environment-specific settings
- [ ] Log levels are: DEBUG, INFO, WARN, ERROR (not TRACE)
- [ ] Production uses INFO level (not DEBUG)

### 8.2 Logging Coverage
- [ ] Service entry/exit is logged (DEBUG level)
- [ ] Database operations are logged (DEBUG level)
- [ ] Authentication failures are logged (WARN level)
- [ ] Business exceptions are logged (WARN/ERROR level)
- [ ] Unexpected errors are logged with stack traces (ERROR level)

### 8.3 Logging Best Practices
- [ ] Log messages use string interpolation (not concatenation):
  ```java
  LOGGER.debug("User {} accessed resource {}", userId, resourceId);  // Good
  LOGGER.debug("User " + userId + " accessed resource " + resourceId);  // Bad
  ```
- [ ] Sensitive data is never logged (passwords, tokens, SSNs)
- [ ] Personal data is masked in logs
- [ ] Log messages are concise and actionable
- [ ] Contextual information is included (user ID, session ID, request ID)

### 8.4 Performance Logging
- [ ] Long-running operations log elapsed time
- [ ] Database query performance is monitored (slow query logs)
- [ ] Memory usage is tracked
- [ ] Thread information is included for concurrent operations

---

## 9. Transaction Management

### 9.1 Transaction Configuration
- [ ] Service methods are annotated with `@Transactional`
- [ ] Read-only operations use `@Transactional(readOnly = true)`
- [ ] Transaction isolation level is appropriate for operation
- [ ] Transaction timeout is set for long-running operations
- [ ] Propagation level is correct (default: REQUIRED)

### 9.2 Transaction Boundaries
- [ ] Transactions span the minimal necessary scope
- [ ] Multiple service calls use single transaction when appropriate
- [ ] External service calls are outside transaction scope
- [ ] Exception handling does not prevent rollback
- [ ] Nested transactions use appropriate propagation

### 9.3 Lazy Loading
- [ ] Collections accessed in transaction context
- [ ] Join fetches are used for eagerly loaded associations
- [ ] OpenEntityManagerInView filter handles controller-level lazy loading
- [ ] No "lazy initialization exception" in logs

### 9.4 Deadlock Prevention
- [ ] Table access order is consistent across operations
- [ ] Lock timeouts are configured
- [ ] Optimistic locking (version field) is used where appropriate
- [ ] Long transactions are avoided

### 9.5 Data Consistency
- [ ] Database constraints are leveraged
- [ ] Optimistic locking prevents concurrent modifications
- [ ] Cascade operations are carefully controlled
- [ ] Foreign key relationships are enforced

---

## 10. Resource Cleanup

### 10.1 EntityManager Management
- [ ] EntityManager is injected via `@Autowired`, not created manually
- [ ] EntityManager is not closed manually (container manages lifecycle)
- [ ] Session cleanup is handled by Spring
- [ ] Cached entities are evicted when necessary

### 10.2 Stream/Reader Resources
- [ ] All streams are closed in finally block or try-with-resources:
  ```java
  try (InputStream is = new FileInputStream(file)) {
      // Use stream
  }
  ```
- [ ] InputStreamReaders are closed properly
- [ ] BufferedReaders are flushed and closed

### 10.3 Connection Pool Management
- [ ] Connection pooling configuration limits connections
- [ ] Connections are returned to pool after use
- [ ] Connection leak detection is enabled
- [ ] Idle connection cleanup is configured

### 10.4 Cache Cleanup
- [ ] Cache eviction policies are configured
- [ ] Manual cache clearing is done appropriately
- [ ] Memory leaks in caches are prevented
- [ ] Cache statistics are monitored

### 10.5 File Handling
- [ ] Temporary files are deleted after use
- [ ] File uploads have size limits
- [ ] Disk space is monitored
- [ ] Old files are archived/deleted per retention policy

### 10.6 External Service Connections
- [ ] HTTP client connections are properly closed
- [ ] REST client timeout is configured
- [ ] SOAP client connections are managed by CXF
- [ ] Circuit breaker pattern is used for external calls
- [ ] Fallback mechanisms handle service unavailability

---

## Pre-Commit Checklist

### Code Changes
- [ ] Code compiles without warnings
- [ ] No hardcoded values (use properties/constants)
- [ ] No commented-out code blocks
- [ ] No System.out.println() calls (use Logger)

### Testing
- [ ] New features have unit tests
- [ ] Bug fixes have regression tests
- [ ] All tests pass locally
- [ ] Code coverage is maintained or improved

### Documentation
- [ ] JavaDoc is updated for public methods
- [ ] README is updated if needed
- [ ] CLAUDE.md is updated for architectural changes
- [ ] Commit message is clear and descriptive

### Security Review
- [ ] No secrets are hardcoded
- [ ] No SQL injection vulnerabilities
- [ ] Authentication/authorization is verified
- [ ] Input validation is present

### Performance Review
- [ ] No obvious performance bottlenecks
- [ ] Database queries are optimized
- [ ] Caching is used appropriately
- [ ] No memory leaks

---

## Review Workflow

### For Reviewers

1. **Read the description** - Understand the change purpose
2. **Review changes by category:**
   - Code quality (naming, organization, style)
   - Exception handling (proper exception types, logging)
   - Null safety (defensive programming)
   - Performance (queries, caching, connections)
   - Security (authentication, input validation, SQL injection)
   - Tests (coverage, edge cases)
   - Documentation (comments, JavaDoc)
   - Logging (appropriate levels, no sensitive data)
   - Transactions (proper boundaries, lazy loading)
   - Resource cleanup (connections, streams, caches)
3. **Ask questions** - If unclear, ask developer
4. **Suggest improvements** - Use tone of collaboration, not criticism
5. **Approve** - Once all concerns are addressed

### For Developers

1. **Self-review before submitting** - Use this checklist
2. **Run tests locally** - All tests must pass
3. **Check code coverage** - Aim for 80%+
4. **Prepare description** - Explain the "why"
5. **Respond to feedback** - Update code or explain decision
6. **Request re-review** - After making changes

---

## Critical Issues (Block Merge)

- [ ] Security vulnerabilities (SQL injection, unvalidated input, hardcoded secrets)
- [ ] NullPointerException risks (missing null checks)
- [ ] Transaction consistency issues (improper boundaries, missing @Transactional)
- [ ] Resource leaks (unclosed streams/connections)
- [ ] Test failures or reduced code coverage without justification
- [ ] Exception handling without proper logging
- [ ] Hardcoded database credentials or API keys

---

## Notes

- This checklist is based on the CallCard Microservice architecture (Spring Boot 2.7.x, Apache CXF 3.5.x, Hibernate 5.6.x)
- For legacy code migration from gameserver_v3, ensure compatibility with both old (Java 1.6) and new (Java 1.8+) features
- Multi-tenant filtering must be applied to all queries accessing user/organization data
- SQL Server-specific features (e.g., `TOP`, `NOLOCK`) must be used carefully or abstracted in database layer
- Session authentication via `IGameInternalService` must be implemented (currently a TODO in `SessionAuthenticationInterceptor`)

---

## Related Documentation

- **Architecture:** `TECHNICAL_ARCHITECTURE_TALOS_MAIND.md`
- **Project Setup:** `CLAUDE.md`
- **Integration Guide:** `MIDDLEWARE_INTEGRATION_COMPLETE.md`
- **Exception Mapping:** Review `ExceptionTypeTO` enum for error code standards
- **Configuration:** Review `application.properties` and environment-specific profiles

---

**Generated:** 2025-12-22
**Version:** 1.0
**For Project:** CallCard Microservice (Spring Boot Migration)
